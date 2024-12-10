package se.sundsvall.casestatus.integration.incident;

import static se.sundsvall.casestatus.integration.incident.configuration.IncidentConfiguration.CLIENT_ID;

import generated.se.sundsvall.incident.IncidentOepResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.casestatus.integration.incident.configuration.IncidentConfiguration;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.incident.base-url}",
	configuration = IncidentConfiguration.class,
	dismiss404 = true)
public interface IncidentClient {

	@GetMapping("/{municipalityId}/incident/internal/oep/{externalCaseId}/status")
	IncidentOepResponse getIncidentStatusForExternalCaseId(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("externalCaseId") final String externalCaseId);

}
