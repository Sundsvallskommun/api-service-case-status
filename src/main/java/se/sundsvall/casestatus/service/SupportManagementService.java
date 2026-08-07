package se.sundsvall.casestatus.service;

import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.NamespaceConfig;
import generated.se.sundsvall.supportmanagement.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.lang3.Strings;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import se.sundsvall.casestatus.integration.supportmanagement.SupportManagementIntegration;
import se.sundsvall.casestatus.util.PaginationUtil;
import se.sundsvall.casestatus.util.RoleSearchProperties;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static se.sundsvall.casestatus.util.FilterUtil.escapeFilterValue;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

@Service
public class SupportManagementService {

	private static final String ROLE_PRIMARY = "PRIMARY";
	private static final String EXTERNAL_ID_FILTER = "stakeholders.externalId:'%s' and stakeholders.role:'%s'";

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

	public String getClassificationDisplayName(String municipalityId, String namespace, Errand errand) {
		final var classification = errand.getClassification();
		if (isNull(classification)) {
			return null;
		}

		return ofNullable(supportManagementIntegration.findCategoriesForNamespace(municipalityId, namespace)).orElse(emptyList()).stream()
			.filter(category -> Strings.CI.equals(category.getName(), classification.getCategory()))
			.flatMap(category -> ofNullable(category.getTypes()).orElse(emptySet()).stream())
			.filter(type -> Strings.CI.equals(type.getName(), classification.getType()))
			.map(Type::getDisplayName)
			.findFirst()
			.orElse(classification.getType()); // fallback
	}

	private String getSearchRole(final String municipalityId, final String namespace) {
		return ofNullable(searchRoleProperties.getRoles())
			.map(roles -> roles.get(municipalityId))
			.map(namespaceRoles -> namespaceRoles.get(namespace))
			.orElse(ROLE_PRIMARY);
	}
}
