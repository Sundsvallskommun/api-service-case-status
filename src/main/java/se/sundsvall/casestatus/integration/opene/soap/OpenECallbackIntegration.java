package se.sundsvall.casestatus.integration.opene.soap;

import static se.sundsvall.casestatus.utility.Constants.EXTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.casestatus.utility.Constants.INTERNAL_CHANNEL_E_SERVICE;

import generated.se.sundsvall.opene.SetStatus;
import generated.se.sundsvall.opene.SetStatusResponse;
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

	public SetStatusResponse setStatus(final String channel, final SetStatus setStatus) {
		if (INTERNAL_CHANNEL_E_SERVICE.equalsIgnoreCase(channel)) {
			return openECallbackInternalClient.updateStatus(setStatus);
		}
		if (EXTERNAL_CHANNEL_E_SERVICE.equalsIgnoreCase(channel)) {
			return oepExternalClient.updateStatus(setStatus);
		}
		return null;
	}

}
