package se.sundsvall.casestatus.integration.incident;

import generated.se.sundsvall.incident.IncidentOepResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = IncidentIntegration.INTEGRATION_NAME,
        url = "${integration.incident.base-url}",
        configuration = IncidentIntegrationConfiguration.class
)
interface IncidentClient {

    @GetMapping("/internal/oep/{externalCaseId}/status")
    IncidentOepResponse getIncidentStatusForExternalCaseId(@PathVariable("externalCaseId") final String externalCaseId);
}
