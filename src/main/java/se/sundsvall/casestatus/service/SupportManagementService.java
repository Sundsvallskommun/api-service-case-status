package se.sundsvall.casestatus.service;

import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ErrandLabel;
import generated.se.sundsvall.supportmanagement.Label;
import generated.se.sundsvall.supportmanagement.Labels;
import generated.se.sundsvall.supportmanagement.NamespaceConfig;
import generated.se.sundsvall.supportmanagement.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import se.sundsvall.casestatus.integration.supportmanagement.SupportManagementIntegration;
import se.sundsvall.casestatus.util.PaginationUtil;
import se.sundsvall.casestatus.util.RoleSearchProperties;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Comparator.comparingInt;
import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static se.sundsvall.casestatus.util.FilterUtil.escapeFilterValue;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

@Service
public class SupportManagementService {

	private static final String ROLE_PRIMARY = "PRIMARY";
	private static final String EXTERNAL_ID_FILTER = "stakeholders.externalId:'%s' and stakeholders.role:'%s'";
	private static final char RESOURCE_PATH_SEPARATOR = '/';

	private final SupportManagementIntegration supportManagementIntegration;
	private final RoleSearchProperties searchRoleProperties;

	public SupportManagementService(final SupportManagementIntegration supportManagementIntegration, final RoleSearchProperties searchRoleProperties) {

		this.supportManagementIntegration = supportManagementIntegration;
		this.searchRoleProperties = searchRoleProperties;
	}

	public Map<String, List<Errand>> getSupportManagementCases(final String municipalityId, final String filter) {
		return findErrandsPerNamespace(municipalityId, _ -> filter);
	}

	public Map<String, List<Errand>> getSupportManagementCasesByExternalId(final String municipalityId, final String externalId) {
		return findErrandsPerNamespace(municipalityId,
			config -> EXTERNAL_ID_FILTER.formatted(escapeFilterValue(externalId), getSearchRole(config.getMunicipalityId(), config.getNamespace())));
	}

	/**
	 * Runs a paged search in every namespace configured for the municipality and returns the errands keyed by namespace.
	 * The filter is resolved per namespace since some searches need namespace specific values (e.g. stakeholder role).
	 */
	private Map<String, List<Errand>> findErrandsPerNamespace(final String municipalityId, final Function<NamespaceConfig, String> filterResolver) {
		final var errandMap = new HashMap<String, List<Errand>>();

		ofNullable(supportManagementIntegration.readAllNamespaceConfigs(municipalityId)).orElse(emptyList())
			.forEach(config -> errandMap.put(config.getNamespace(), findAllErrands(config, filterResolver.apply(config))));

		return errandMap;
	}

	private List<Errand> findAllErrands(final NamespaceConfig config, final String filter) {
		return PaginationUtil.fetchAllPages(
			pageNumber -> supportManagementIntegration.findErrands(config.getMunicipalityId(), config.getNamespace(), filter, PageRequest.of(pageNumber, PaginationUtil.PAGE_SIZE)),
			"errands for municipalityId: %s and namespace: %s".formatted(sanitizeForLogging(config.getMunicipalityId()), sanitizeForLogging(config.getNamespace())));
	}

	/**
	 * Resolves the display name to present as case type for an errand.
	 * <p>
	 * A namespace classifies its errands either with the legacy category/type metadata or with labels. The category/type
	 * metadata is therefore consulted first, which leaves namespaces using it untouched. Label based namespaces instead
	 * carry the label resource path (e.g. "BOU/FEE_CONTROL_CHILDCARE") as classification type, a technical name that must
	 * not reach the consumer, so the labels are resolved instead - primarily from the labels on the errand itself and
	 * secondarily from the namespace label structure, since an errand can hold the resource path in its classification
	 * without carrying the label. Only when nothing translates does the raw classification type remain as a last resort.
	 */
	public String getClassificationDisplayName(final String municipalityId, final String namespace, final Errand errand) {
		final var classification = errand.getClassification();

		return findTypeDisplayName(municipalityId, namespace, classification)
			.or(() -> findErrandLabelDisplayName(errand))
			.or(() -> findLabelDisplayName(municipalityId, namespace, classification))
			.orElseGet(() -> ofNullable(classification).map(Classification::getType).orElse(null));
	}

	private Optional<String> findTypeDisplayName(final String municipalityId, final String namespace, final Classification classification) {
		if (isNull(classification)) {
			return empty();
		}

		return ofNullable(supportManagementIntegration.findCategoriesForNamespace(municipalityId, namespace)).orElse(emptyList()).stream()
			.filter(category -> Strings.CI.equals(category.getName(), classification.getCategory()))
			.flatMap(category -> ofNullable(category.getTypes()).orElse(emptySet()).stream())
			.filter(type -> Strings.CI.equals(type.getName(), classification.getType()))
			.map(Type::getDisplayName)
			.filter(StringUtils::isNotBlank)
			.findFirst();
	}

	/**
	 * Picks the deepest of the errand's labels, i.e. the most specific one ("BOU/FEE_CONTROL_CHILDCARE" rather than
	 * "BOU"), whose display name SupportManagement has already resolved for us.
	 */
	private static Optional<String> findErrandLabelDisplayName(final Errand errand) {
		return ofNullable(errand.getLabels()).orElse(emptyList()).stream()
			.filter(Objects::nonNull)
			.filter(label -> StringUtils.isNotBlank(label.getDisplayName()))
			.max(comparingInt((ErrandLabel label) -> StringUtils.countMatches(label.getResourcePath(), RESOURCE_PATH_SEPARATOR)))
			.map(ErrandLabel::getDisplayName);
	}

	private Optional<String> findLabelDisplayName(final String municipalityId, final String namespace, final Classification classification) {
		final var resourcePath = ofNullable(classification).map(Classification::getType).orElse(null);

		if (StringUtils.isBlank(resourcePath)) {
			return empty();
		}

		return flatten(ofNullable(supportManagementIntegration.findLabelsForNamespace(municipalityId, namespace))
			.map(Labels::getLabelStructure)
			.orElse(emptyList()))
			.filter(label -> Strings.CI.equals(normalizeResourcePath(label.getResourcePath()), normalizeResourcePath(resourcePath)))
			.map(Label::getDisplayName)
			.filter(StringUtils::isNotBlank)
			.findFirst();
	}

	/**
	 * The resource path is written with a leading separator in some places (see the SupportManagement API examples) but
	 * without one in the classification type, so both sides are trimmed before being compared.
	 */
	private static String normalizeResourcePath(final String resourcePath) {
		return StringUtils.strip(resourcePath, String.valueOf(RESOURCE_PATH_SEPARATOR));
	}

	/**
	 * Flattens the label tree, as a label anywhere in the structure can be the one carried by the errand.
	 */
	private static Stream<Label> flatten(final List<Label> labels) {
		return ofNullable(labels).orElse(emptyList()).stream()
			.filter(Objects::nonNull)
			.flatMap(label -> Stream.concat(Stream.of(label), flatten(label.getLabels())));
	}

	private String getSearchRole(final String municipalityId, final String namespace) {
		return ofNullable(searchRoleProperties.getRoles())
			.map(roles -> roles.get(municipalityId))
			.map(namespaceRoles -> namespaceRoles.get(namespace))
			.orElse(ROLE_PRIMARY);
	}
}
