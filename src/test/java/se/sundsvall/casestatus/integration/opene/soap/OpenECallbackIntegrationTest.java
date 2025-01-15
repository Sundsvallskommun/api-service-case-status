package se.sundsvall.casestatus.integration.opene.soap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.opene.AddMessage;
import generated.se.sundsvall.opene.AddMessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.sundsvall.casestatus.integration.opene.soap.client.OpenECallbackExternalClient;
import se.sundsvall.casestatus.integration.opene.soap.client.OpenECallbackInternalClient;

class OpenECallbackIntegrationTest {

	@Mock
	private OpenECallbackExternalClient oepExternalClient;

	@Mock
	private OpenECallbackInternalClient openECallbackInternalClient;

	@InjectMocks
	private OpenECallbackIntegration openECallbackIntegration;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testAddMessageInternal() {
		// Arrange
		final var addMessage = new AddMessage();
		final var expectedResponse = new AddMessageResponse();
		when(openECallbackInternalClient.addMessage(any(AddMessage.class))).thenReturn(expectedResponse);

		// Act
		final var actualResponse = openECallbackIntegration.addMessage("internal", addMessage);

		// Assert
		assertThat(actualResponse).isEqualTo(expectedResponse);
	}

	@Test
	void testAddMessageExternal() {
		// Arrange
		final var addMessage = new AddMessage();
		final var expectedResponse = new AddMessageResponse();
		when(oepExternalClient.addMessage(any(AddMessage.class))).thenReturn(expectedResponse);

		// Act
		final var actualResponse = openECallbackIntegration.addMessage("external", addMessage);

		// Assert
		assertThat(actualResponse).isEqualTo(expectedResponse);
	}

	@Test
	void testAddMessageInvalidInstance() {
		// Arrange
		final var addMessage = new AddMessage();

		// Act
		final var actualResponse = openECallbackIntegration.addMessage("invalid", addMessage);

		// Assert
		assertThat(actualResponse).isNull();
	}
}
