package se.sundsvall.casestatus.integration.eventlog;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casestatus.integration.eventlog.configuration.EventlogConfiguration.CLIENT_ID;

import feign.QueryMap;
import generated.se.sundsvall.supportmanagement.Event;
import generated.se.sundsvall.supportmanagement.PageEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import se.sundsvall.casestatus.integration.eventlog.configuration.EventlogConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.eventlog.base-url}", configuration = EventlogConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface EventlogClient {

	/**
	 * Create a log event under logKey.
	 *
	 * @param municipalityId municipality id to create event for
	 * @param logKey         containing UUID to create event for
	 * @param event          the event to create
	 */
	@PostMapping(path = "/{municipalityId}/{logKey}", consumes = APPLICATION_JSON_VALUE)
	ResponseEntity<Void> createEvent(
		@PathVariable("municipalityId") String municipalityId,
		@PathVariable("logKey") String logKey,
		@RequestBody Event event);

	/**
	 * Fetch created log events for a logKey.
	 *
	 * @param  municipalityId municipality id to fetch events for
	 * @param  logKey         containing UUID to fetch events for
	 * @param  pageable       information of page, size and sorting options for the request
	 * @return                response containing result of search based on the provided parameters
	 */
	@GetMapping(path = "/{municipalityId}/{logKey}", produces = APPLICATION_JSON_VALUE)
	PageEvent getEvents(
		@PathVariable("municipalityId") String municipalityId,
		@PathVariable("logKey") String logKey,
		@QueryMap Pageable pageable);

}
