package se.sundsvall.casestatus.integration.party;

import static se.sundsvall.casestatus.integration.party.configuration.PartyConfiguration.CLIENT_ID;

import generated.se.sundsvall.party.PartyType;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.casestatus.integration.party.configuration.PartyConfiguration;

@FeignClient(name = CLIENT_ID,
	url = "${integration.party.base-url}",
	configuration = PartyConfiguration.class,
	dismiss404 = true)
public interface PartyClient {

	@GetMapping("/{municipalityId}/{type}/{partyId}/legalId")
	Optional<String> getLegalIdByPartyId(
		@PathVariable String municipalityId,
		@PathVariable PartyType type,
		@PathVariable String partyId);

}
