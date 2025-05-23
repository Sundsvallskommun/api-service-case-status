package se.sundsvall.casestatus.integration.casedata;

import static se.sundsvall.casestatus.integration.casedata.configuration.CaseDataConfiguration.CLIENT_ID;

import generated.se.sundsvall.casedata.Errand;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.casestatus.integration.casedata.configuration.CaseDataConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.case-data.base-url}", configuration = CaseDataConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface CaseDataClient {

	@GetMapping(path = "/{municipalityId}/errands")
	Page<Errand> getErrandsWithoutNamespace(
		@PathVariable("municipalityId") final String municipalityId,
		@RequestParam("filter") final String filter,
		final PageRequest pageRequest);

}
