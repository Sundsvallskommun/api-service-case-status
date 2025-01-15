package se.sundsvall.casestatus.integration.opene.soap.client;

import generated.se.sundsvall.opene.AddMessage;
import generated.se.sundsvall.opene.AddMessageResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface OpenECallbackBaseClient {

	String TEXT_XML_UTF_8 = "text/xml; charset=UTF-8";

	@PostMapping(consumes = TEXT_XML_UTF_8, produces = TEXT_XML_UTF_8)
	AddMessageResponse addMessage(@RequestBody AddMessage addMessage);
}
