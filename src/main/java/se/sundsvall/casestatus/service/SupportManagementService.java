package se.sundsvall.casestatus.service;

import generated.se.sundsvall.supportmanagement.Errand;
import java.util.ArrayList;
import java.util.List;
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

	public List<Errand> getSupportManagementCases(final String municipalityId, final String filter) {

		final var allResponses = new ArrayList<Errand>();

		supportManagementClient.readAllNamespaceConfigs().forEach(namespace -> {
			int pageNumber = 0;
			Page<Errand> response;

			do {
				response = supportManagementClient.findErrands(municipalityId, namespace.getNamespace(), filter, PageRequest.of(pageNumber, 100));
				allResponses.addAll(response.getContent());
				pageNumber++;
			} while (response.hasNext());
		});

		return allResponses.stream().distinct().toList();
	}
}
