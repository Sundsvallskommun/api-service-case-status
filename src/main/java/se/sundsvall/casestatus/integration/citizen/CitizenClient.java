package se.sundsvall.casestatus.integration.citizen;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = CitizenIntegration.INTEGRATION_NAME,
        url = "${integration.citizen.base-url}",
        configuration = CitizenIntegrationConfiguration.class
)
interface CitizenClient {
    @GetMapping("/person/{personNumber}/guid")
    String getPersonID(@PathVariable("personNumber") final String personNumber);

}
