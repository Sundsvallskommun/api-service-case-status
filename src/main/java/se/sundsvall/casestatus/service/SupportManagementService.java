package se.sundsvall.casestatus.service;

import generated.se.sundsvall.supportmanagement.Errand;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
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

	public Errand getSupportManagementCaseById(final String municipalityId, final String errandId) {
		return supportManagementClient.readAllNamespaceConfigs().stream().map(namespace -> supportManagementClient.findErrandById(municipalityId, namespace.getNamespace(), errandId)).findFirst().orElse(null);
	}

}
