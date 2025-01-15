package se.sundsvall.casestatus.integration.opene.soap;

import generated.se.sundsvall.opene.AddMessage;
import generated.se.sundsvall.opene.AddMessageResponse;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.opene.soap.client.OpenECallbackExternalClient;
import se.sundsvall.casestatus.integration.opene.soap.client.OpenECallbackInternalClient;

@Component
public class OpenECallbackIntegration {

	private final OpenECallbackExternalClient oepExternalClient;
	private final OpenECallbackInternalClient openECallbackInternalClient;

	public OpenECallbackIntegration(final OpenECallbackExternalClient oepExternalClient, final OpenECallbackInternalClient openECallbackInternalClient) {
		this.oepExternalClient = oepExternalClient;
		this.openECallbackInternalClient = openECallbackInternalClient;
	}

	public AddMessageResponse addMessage(final String instance, final AddMessage addMessage) {
		if (instance.equalsIgnoreCase("internal")) {
			return openECallbackInternalClient.addMessage(addMessage);
		}
		if (instance.equalsIgnoreCase("external")) {
			return oepExternalClient.addMessage(addMessage);
		}
		return null;
	}

}
