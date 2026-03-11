package se.sundsvall.casestatus.integration.messaging;

import generated.client.messaging.SlackRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.casestatus.integration.messaging.configuration.MessagingConfiguration;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casestatus.integration.messaging.configuration.MessagingConfiguration.CLIENT_ID;

@FeignClient(name = CLIENT_ID, url = "${integration.messaging.base-url}", configuration = MessagingConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface MessagingClient {

	@PostMapping(value = "/{municipalityId}/slack", consumes = APPLICATION_JSON_VALUE)
	ResponseEntity<Void> sendSlack(@PathVariable String municipalityId, @RequestBody SlackRequest request);
}
