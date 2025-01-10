package se.sundsvall.casestatus.integration.supportmanagement;

import static se.sundsvall.casestatus.integration.supportmanagement.configuration.SupportManagementConfiguration.CLIENT_ID;

import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.NamespaceConfig;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.casestatus.integration.supportmanagement.configuration.SupportManagementConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.support-management.base-url}", configuration = SupportManagementConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface SupportManagementClient {

	@GetMapping(path = "/namespace-configs")
	List<NamespaceConfig> readAllNamespaceConfigs();

	@GetMapping(path = "/{municipalityId}/{namespace}/errands")
	ResponseEntity<Page<Errand>> findErrands(
		@PathVariable("namespace") final String namespace,
		@PathVariable("municipalityId") final String municipalityId,
		@RequestParam("filter") final String filter);
}
