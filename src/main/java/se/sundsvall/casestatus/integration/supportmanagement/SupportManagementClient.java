package se.sundsvall.casestatus.integration.supportmanagement;

import generated.se.sundsvall.supportmanagement.Category;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.NamespaceConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.casestatus.integration.supportmanagement.configuration.SupportManagementConfiguration;

import static se.sundsvall.casestatus.integration.supportmanagement.configuration.SupportManagementConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID,
	url = "${integration.support-management.base-url}",
	dismiss404 = true,
	configuration = SupportManagementConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
interface SupportManagementClient {

	@GetMapping(path = "/namespace-configs")
	List<NamespaceConfig> readAllNamespaceConfigs(@RequestParam String municipalityId);

	@GetMapping(path = "/{municipalityId}/{namespace}/metadata/categories")
	List<Category> findCategoriesForNamespace(
		@PathVariable String municipalityId,
		@PathVariable String namespace);

	@GetMapping(path = "/{municipalityId}/{namespace}/errands")
	Page<Errand> findErrands(
		@PathVariable String municipalityId,
		@PathVariable String namespace,
		@RequestParam String filter,
		PageRequest pageRequest);

	@GetMapping(path = "/{municipalityId}/{namespace}/errands/{errandId}")
	Optional<Errand> findErrandById(
		@PathVariable String municipalityId,
		@PathVariable String namespace,
		@PathVariable String errandId);
}
