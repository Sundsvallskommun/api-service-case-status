package se.sundsvall.casestatus.integration.messaging;

import generated.client.messaging.SlackRequest;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.messaging.configuration.MessagingProperties;

@Component
public class MessagingIntegration {

	private final MessagingClient client;
	private final MessagingProperties properties;

	public MessagingIntegration(final MessagingClient client, final MessagingProperties properties) {
		this.client = client;
		this.properties = properties;
	}

	public void sendSlackMessage(final String municipalityId, final String message) {
		client.sendSlack(municipalityId, new SlackRequest()
			.token(properties.token())
			.channel(properties.channel())
			.message(message));
	}
}
