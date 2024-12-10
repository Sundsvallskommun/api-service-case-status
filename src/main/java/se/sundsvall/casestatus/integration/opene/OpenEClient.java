package se.sundsvall.casestatus.integration.opene;

import static se.sundsvall.casestatus.integration.opene.configuration.OpenEConfiguration.CLIENT_ID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.casestatus.integration.opene.configuration.OpenEConfiguration;

@FeignClient(name = CLIENT_ID, url = "${integration.open-e.base-url}", configuration = OpenEConfiguration.class)
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

}
