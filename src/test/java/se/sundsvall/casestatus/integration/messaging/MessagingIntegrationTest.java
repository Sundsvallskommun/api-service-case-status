package se.sundsvall.casestatus.integration.messaging;

import generated.client.messaging.SlackRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casestatus.integration.messaging.configuration.MessagingProperties;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessagingIntegrationTest {

	@Mock
	private MessagingClient clientMock;

	@Mock
	private MessagingProperties propertiesMock;

	@InjectMocks
	private MessagingIntegration messagingIntegration;

	@Test
	void sendSlackMessage() {
		final var municipalityId = "2281";
		final var message = "Test message";
		final var token = "slack-token";
		final var channel = "#alerts";

		when(propertiesMock.token()).thenReturn(token);
		when(propertiesMock.channel()).thenReturn(channel);

		messagingIntegration.sendSlackMessage(municipalityId, message);

		verify(clientMock).sendSlack(municipalityId, new SlackRequest()
			.token(token)
			.channel(channel)
			.message(message));
	}
}
