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

		final var errandMap = new HashMap<String, List<Errand>>();

		supportManagementClient.readAllNamespaceConfigs().forEach(config -> {
			int pageNumber = 0;
			Page<Errand> response;
			final List<Errand> allErrands = new ArrayList<>();

			do {
				response = supportManagementClient.findErrands(municipalityId, config.getNamespace(), filter, PageRequest.of(pageNumber, 100));
				allErrands.addAll(response.getContent());
				pageNumber++;
			} while (response.hasNext());

			errandMap.put(config.getNamespace(), allErrands);
		});

		return errandMap;
	}

	public Errand getSupportManagementCaseById(final String municipalityId, final String namespace, final String errandId) {
		final var result = supportManagementClient.findErrandById(municipalityId, namespace, errandId);
		if (result.getStatusCode().is2xxSuccessful()) {
			return result.getBody();
		} else {
			return null;
		}
	}

}
