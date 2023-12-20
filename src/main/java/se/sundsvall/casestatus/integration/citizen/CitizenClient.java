package se.sundsvall.casestatus.integration.citizen;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
	name = CitizenIntegration.INTEGRATION_NAME,
	url = "${integration.citizen.base-url}",
	configuration = CitizenIntegrationConfiguration.class)
interface CitizenClient {

	/**
	 * Method for retrieving personId associated with the provided personal identity number.
	 *
	 * @param  personalNumber the personal identity number.
	 * @return                string containing personId for sent in personal identity number.
	 */
	@GetMapping(path = "/{personalNumber}/guid", produces = TEXT_PLAIN_VALUE)
	String getPersonId(@PathVariable("personalNumber") String personalNumber);
}
