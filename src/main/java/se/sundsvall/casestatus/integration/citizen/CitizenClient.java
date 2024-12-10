package se.sundsvall.casestatus.integration.citizen;

import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static se.sundsvall.casestatus.integration.citizen.configuration.CitizenConfiguration.CLIENT_ID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.casestatus.integration.citizen.configuration.CitizenConfiguration;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.citizen.base-url}",
	configuration = CitizenConfiguration.class)
public interface CitizenClient {

	/**
	 * Method for retrieving personId associated with the provided personal identity number.
	 *
	 * @param  personalNumber the personal identity number.
	 * @return                string containing personId for sent in personal identity number.
	 */
	@GetMapping(path = "/{personalNumber}/guid", produces = TEXT_PLAIN_VALUE)
	String getPersonId(@PathVariable("personalNumber") String personalNumber);
}
