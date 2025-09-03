package se.sundsvall.casestatus.integration.eventlog;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static se.sundsvall.casestatus.integration.eventlog.configuration.EventlogConfiguration.CLIENT_ID;

import feign.QueryMap;
import generated.se.sundsvall.eventlog.Event;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import se.sundsvall.casestatus.integration.eventlog.configuration.EventlogConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.eventlog.base-url}", configuration = EventlogConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface EventlogClient {
	/**
	 * Fetch created log events for a logKey.
	 *
	 * @param  municipalityId municipality id to fetch events for
	 * @param  pageable       information of page, size and sorting options for the request
	 * @param  filter         filter to apply to the search
	 * @return                response containing result of search based on the provided parameters
	 */
	@GetMapping(path = "/{municipalityId}", produces = APPLICATION_JSON_VALUE)
	Page<Event> getEvents(
		@PathVariable String municipalityId,
		@QueryMap Pageable pageable,
		@RequestParam String filter);

}
