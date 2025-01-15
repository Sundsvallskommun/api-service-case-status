package se.sundsvall.casestatus.integration.opene.rest;

import static se.sundsvall.casestatus.integration.opene.rest.configuration.OpenEConfiguration.CLIENT_ID;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.casestatus.integration.opene.rest.configuration.OpenEConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.open-e.base-url}", configuration = OpenEConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface OpenEClient {

	String TEXT_XML_CHARSET_ISO_8859_1 = "text/xml; charset=ISO-8859-1";

	@GetMapping(path = "/api/instanceapi/getinstances/family/{familyid}", produces = TEXT_XML_CHARSET_ISO_8859_1)
	byte[] getErrandIds(@PathVariable(name = "familyid") final String familyId);

	@GetMapping(path = "/api/instanceapi/getinstance/{flowinstanceid}/xml", produces = TEXT_XML_CHARSET_ISO_8859_1)
	byte[] getErrand(@PathVariable(name = "flowinstanceid") final String id);

	@GetMapping(path = "/api/instanceapi/getstatus/{flowinstanceid}", produces = TEXT_XML_CHARSET_ISO_8859_1)
	byte[] getErrandStatus(@PathVariable(name = "flowinstanceid") final String id);

	@GetMapping(path = "/api/instanceapi/getinstance/{flowinstanceid}/pdf", produces = TEXT_XML_CHARSET_ISO_8859_1)
	byte[] getPDF(@PathVariable(name = "flowinstanceid") final String id);

	@GetMapping(path = "/api/instanceapi/getinstances/owner/citizenidentifier/{personnummer}", produces = TEXT_XML_CHARSET_ISO_8859_1)
	byte[] getErrands(@PathVariable(name = "personnummer") String personnummer);
}
