package se.sundsvall.casestatus.service;

import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;

import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import se.sundsvall.casestatus.integration.supportmanagement.SupportManagementIntegration;
import se.sundsvall.casestatus.util.RoleSearchProperties;

@Service
public class SupportManagementService {

	private static final String ROLE_PRIMARY = "PRIMARY";
	private final SupportManagementIntegration supportManagementIntegration;
	private final RoleSearchProperties searchRoleProperties;

	public SupportManagementService(final SupportManagementIntegration supportManagementIntegration, final RoleSearchProperties searchRoleProperties) {

		this.supportManagementIntegration = supportManagementIntegration;
		this.searchRoleProperties = searchRoleProperties;
	}

	public Map<String, List<Errand>> getSupportManagementCases(final String municipalityId, final String filter) {

		final var errandMap = new HashMap<String, List<Errand>>();

		supportManagementIntegration.readAllNamespaceConfigs(municipalityId).forEach(config -> {
			int pageNumber = 0;
			Page<Errand> response;
			final List<Errand> allErrands = new ArrayList<>();

			do {
				response = supportManagementIntegration.findErrands(config.getMunicipalityId(), config.getNamespace(), filter, PageRequest.of(pageNumber, 100));
				allErrands.addAll(response.getContent());
				pageNumber++;
			} while (response.hasNext());

			errandMap.put(config.getNamespace(), allErrands);
		});

		return errandMap;
	}

	public Map<String, List<Errand>> getSupportManagementCasesByExternalId(final String municipalityId, final String externalId) {

		final var errandMap = new HashMap<String, List<Errand>>();

		final var filter = "stakeholders.externalId:'%s' and stakeholders.role:'%s'";

		supportManagementIntegration.readAllNamespaceConfigs(municipalityId).forEach(config -> {
			int pageNumber = 0;
			Page<Errand> response;
			final List<Errand> allErrands = new ArrayList<>();

			do {
				response = supportManagementIntegration.findErrands(config.getMunicipalityId(), config.getNamespace(),
					filter.formatted(externalId, getSearchRole(config.getMunicipalityId(), config.getNamespace())), PageRequest.of(pageNumber, 100));
				allErrands.addAll(response.getContent());
				pageNumber++;
			} while (response.hasNext());

			errandMap.put(config.getNamespace(), allErrands);
		});

		return errandMap;
	}

	public Errand getSupportManagementCaseById(final String municipalityId, final String namespace, final String errandId) {
		final var result = supportManagementIntegration.findErrandById(municipalityId, namespace, errandId);
		if (result.getStatusCode().is2xxSuccessful()) {
			return result.getBody();
		}

		return null;
	}

	public String getClassificationDisplayName(String municipalityId, String namespace, Errand errand) {
		final var classification = errand.getClassification();
		if (isNull(classification)) {
			return null;
		}

		return supportManagementIntegration.findCategoriesForNamespace(municipalityId, namespace).stream()
			.filter(category -> Strings.CI.equals(category.getName(), classification.getCategory()))
			.flatMap(category -> ofNullable(category.getTypes()).orElse(emptySet()).stream())
			.filter(type -> Strings.CI.equals(type.getName(), classification.getType()))
			.map(Type::getDisplayName)
			.findFirst()
			.orElse(classification.getType()); // fallback
	}

	private String getSearchRole(final String municipalityId, final String namespace) {
		final var roles = searchRoleProperties.getRoles();
		final var municipalityRoles = roles.get(municipalityId);
		final String role = municipalityRoles != null ? municipalityRoles.get(namespace) : null;
		return role != null ? role : ROLE_PRIMARY;
	}
}
