package se.sundsvall.casestatus.service;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.NamespaceConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.casestatus.integration.supportmanagement.SupportManagementClient;

@Service
public class SupportManagementService {

	private final SupportManagementClient supportManagementClient;

	public SupportManagementService(final SupportManagementClient supportManagementClient) {
		this.supportManagementClient = supportManagementClient;
	}

	public Map<String, List<Errand>> getSupportManagementCases(final String municipalityId, final String filter) {

		var errandMap = new HashMap<String, List<Errand>>();

		supportManagementClient.readAllNamespaceConfigs().forEach(config -> {
			int pageNumber = 0;
			Page<Errand> response;
			List<Errand> allErrands = new ArrayList<>();

			do {
				response = supportManagementClient.findErrands(municipalityId, config.getNamespace(), filter, PageRequest.of(pageNumber, 100));
				allErrands.addAll(response.getContent());
				pageNumber++;
			} while (response.hasNext());

			errandMap.put(config.getNamespace(), allErrands);
		});

		return errandMap;
	}

	public List<String> getSupportManagementNamespaces() {
		return supportManagementClient.readAllNamespaceConfigs().stream()
			.map(NamespaceConfig::getNamespace)
			.collect(Collectors.toList());
	}

	public Errand getSupportManagementCaseById(final String municipalityId, List<String> namespaces, final String errandId) {
		return namespaces.stream()
			.map(namespace -> supportManagementClient.findErrands(municipalityId, namespace, "id:'" + errandId + "'", PageRequest.of(0, 20)))
			.filter(Slice::hasContent)
			.flatMap(page -> page.getContent().stream())
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(INTERNAL_SERVER_ERROR, "Could not find errand with id:" + errandId));
	}

}
