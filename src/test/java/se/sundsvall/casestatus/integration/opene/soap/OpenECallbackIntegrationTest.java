package se.sundsvall.casestatus.integration.opene.soap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static se.sundsvall.casestatus.utility.Constants.EXTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.casestatus.utility.Constants.INTERNAL_CHANNEL_E_SERVICE;

import generated.se.sundsvall.opene.SetStatus;
import generated.se.sundsvall.opene.SetStatusResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casestatus.integration.opene.soap.client.OpenECallbackExternalClient;
import se.sundsvall.casestatus.integration.opene.soap.client.OpenECallbackInternalClient;

@ExtendWith(MockitoExtension.class)
class OpenECallbackIntegrationTest {

	@Mock
	private OpenECallbackExternalClient oepExternalClient;

	@Mock
	private OpenECallbackInternalClient openECallbackInternalClient;

	@InjectMocks
	private OpenECallbackIntegration openECallbackIntegration;

	@Test
	void testSetStatusInternal() {
		// Arrange
		final var setStatus = new SetStatus();
		final var expectedResponse = new SetStatusResponse();
		when(openECallbackInternalClient.updateStatus(any(SetStatus.class))).thenReturn(expectedResponse);

		// Act
		final var actualResponse = openECallbackIntegration.setStatus(INTERNAL_CHANNEL_E_SERVICE, setStatus);

		// Assert
		assertThat(actualResponse).isEqualTo(expectedResponse);
	}

	@Test
	void testSetStatusExternal() {
		// Arrange
		final var setStatus = new SetStatus();
		final var expectedResponse = new SetStatusResponse();
		when(oepExternalClient.updateStatus(any(SetStatus.class))).thenReturn(expectedResponse);

		// Act
		final var actualResponse = openECallbackIntegration.setStatus(EXTERNAL_CHANNEL_E_SERVICE, setStatus);

		// Assert
		assertThat(actualResponse).isEqualTo(expectedResponse);
	}

	@Test
	void testSetStatusInvalidInstance() {
		// Arrange
		final var setStatus = new SetStatus();

		// Act
		final var actualResponse = openECallbackIntegration.setStatus("invalid", setStatus);

		// Assert
		assertThat(actualResponse).isNull();
	}
}
