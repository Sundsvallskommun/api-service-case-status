package se.sundsvall.casestatus.integration.supportmanagement;

import generated.se.sundsvall.supportmanagement.Category;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.NamespaceConfig;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class SupportManagementIntegration {

	private final SupportManagementClient client;

	public SupportManagementIntegration(final SupportManagementClient client) {
		this.client = client;
	}

	@Cacheable("supportmanagement-namespace-configs")
	public List<NamespaceConfig> readAllNamespaceConfigs(final String municipalityId) {
		return client.readAllNamespaceConfigs(municipalityId);
	}

	public Page<Errand> findErrands(final String municipalityId, final String namespace, final String filter, PageRequest pageRequest) {
		return client.findErrands(municipalityId, namespace, filter, pageRequest);
	}

	public ResponseEntity<Errand> findErrandById(final String municipalityId, final String namespace, final String errandId) {
		return client.findErrandById(municipalityId, namespace, errandId);
	}

	@Cacheable("supportmanagement-categories")
	public List<Category> findCategoriesForNamespace(final String municipalityId, final String namespace) {
		return client.findCategoriesForNamespace(municipalityId, namespace);
	}
}
