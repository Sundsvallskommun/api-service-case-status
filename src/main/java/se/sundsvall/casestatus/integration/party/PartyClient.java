package se.sundsvall.casestatus.integration.party;

import static se.sundsvall.casestatus.integration.party.configuration.PartyConfiguration.CLIENT_ID;

import generated.se.sundsvall.party.PartyType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.casestatus.integration.party.configuration.PartyConfiguration;

@FeignClient(name = CLIENT_ID,
	url = "${integration.party.base-url}",
	configuration = PartyConfiguration.class,
	dismiss404 = true)
@CircuitBreaker(name = CLIENT_ID)
public interface PartyClient {

	@GetMapping("/{municipalityId}/{type}/{partyId}/legalId")
	Optional<String> getLegalIdByPartyId(
		@PathVariable("municipalityId") String municipalityId,
		@PathVariable("type") PartyType type,
		@PathVariable("partyId") String partyId);

}
