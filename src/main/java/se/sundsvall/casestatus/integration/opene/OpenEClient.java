package se.sundsvall.casestatus.integration.opene;

import static se.sundsvall.casestatus.integration.opene.configuration.OpenEIntegrationConfiguration.CLIENT_ID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import se.sundsvall.casestatus.integration.opene.configuration.OpenEIntegrationConfiguration;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@FeignClient(name = CLIENT_ID, url = "${integration.open-e.base-url}", configuration = OpenEIntegrationConfiguration.class)
@CircuitBreaker(name = CLIENT_ID)
public interface OpenEClient {
	String TEXT_XML_CHARSET_ISO_8859_1 = "text/xml; charset=ISO-8859-1";

	@GetMapping(path = "/api/instanceapi/getinstances/family/{familyid}", consumes = TEXT_XML_CHARSET_ISO_8859_1, produces = TEXT_XML_CHARSET_ISO_8859_1)
	byte[] getErrandIds(@PathVariable(name = "familyid") final String familyId);

	@GetMapping(path = "/api/instanceapi/getinstance/{flowinstanceid}/xml", consumes = TEXT_XML_CHARSET_ISO_8859_1, produces = TEXT_XML_CHARSET_ISO_8859_1)
	byte[] getErrand(@PathVariable(name = "flowinstanceid")final String id);

	@GetMapping(path = "/api/instanceapi/getstatus/{flowinstanceid}", consumes = TEXT_XML_CHARSET_ISO_8859_1, produces = TEXT_XML_CHARSET_ISO_8859_1)
	byte[] getErrandStatus(@PathVariable(name = "flowinstanceid") final String id);

	@GetMapping(path = "/api/instanceapi/getinstance/{flowinstanceid}/pdf", consumes = TEXT_XML_CHARSET_ISO_8859_1, produces = TEXT_XML_CHARSET_ISO_8859_1)
	byte[] getPDF(@PathVariable(name = "flowinstanceid")final String id);

}
