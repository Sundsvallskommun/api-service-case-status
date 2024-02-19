package se.sundsvall.casestatus.integration.incident;

import static se.sundsvall.casestatus.integration.incident.configuration.IncidentConfiguration.CLIENT_ID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import se.sundsvall.casestatus.integration.incident.configuration.IncidentConfiguration;

import generated.se.sundsvall.incident.IncidentOepResponse;

@FeignClient(
        name = CLIENT_ID,
        url = "${integration.incident.base-url}",
        configuration = IncidentConfiguration.class
)
public interface IncidentClient {

    @GetMapping("/incident/internal/oep/{externalCaseId}/status")
    IncidentOepResponse getIncidentStatusForExternalCaseId(@PathVariable("externalCaseId") final String externalCaseId);
}
