package se.sundsvall.casestatus.integration.opene;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casestatus.util.casestatuscache.domain.FamilyId;

@ExtendWith(MockitoExtension.class)
class OpenEIntegrationTests {

	@Mock
	private OpenEClient mockOpenEClient;

	@Mock
	private final FamilyId familyId = FamilyId.ROKKANALELDSTAD;

	@InjectMocks
	private OpenEIntegration openEIntegration;

	@Test
    void getPdf_ok() throws IOException {
        when(mockOpenEClient.getPDF(any(String.class))).thenReturn("someAnswer".getBytes(StandardCharsets.UTF_8));

        final var response = openEIntegration.getPdf("someExternalCaseId");

        assertThat(response).isPresent();

        verify(mockOpenEClient).getPDF(any(String.class));
        verifyNoMoreInteractions(mockOpenEClient);
    }

	@Test
    void getPdf_error() throws IOException {

        when(mockOpenEClient.getPDF(any(String.class))).thenThrow(new NullPointerException());

        final var response = openEIntegration.getPdf("someExternalCaseId");

        assertThat(response).isEmpty();

        verify(mockOpenEClient).getPDF(any(String.class));
        verifyNoMoreInteractions(mockOpenEClient);
    }

	@Test
    void getErrandIds_ok() throws IOException {
        when(mockOpenEClient.getErrandIds(any(String.class))).thenReturn("someAnswer".getBytes());
        when(familyId.getValue()).thenReturn(123);
        final var response = openEIntegration.getErrandIds(familyId);

        assertThat(response).isNotNull();
        verify(mockOpenEClient).getErrandIds(any(String.class));
        verifyNoMoreInteractions(mockOpenEClient);
    }

	@Test
    void getErrandIds_error() throws IOException {

        when(mockOpenEClient.getErrandIds(any(String.class))).thenThrow(new NullPointerException());

        final var response = openEIntegration.getErrandIds(familyId);

        assertThat(response).isEmpty();

        verify(mockOpenEClient).getErrandIds(any(String.class));
        verifyNoMoreInteractions(mockOpenEClient);
    }

	@Test
    void getErrand_ok() throws IOException {
        when(mockOpenEClient.getErrand(any(String.class))).thenReturn("someAnswer".getBytes());
        final var response = openEIntegration.getErrand("someFlowInstanceId");

        assertThat(response).isNotNull();
        verify(mockOpenEClient).getErrand(any(String.class));
        verifyNoMoreInteractions(mockOpenEClient);
    }

	@Test
    void getErrand_error() throws IOException {

        when(mockOpenEClient.getErrand(any(String.class))).thenThrow(new NullPointerException());

        final var response = openEIntegration.getErrand("someFlowInstanceId");

        assertThat(response).isEmpty();

        verify(mockOpenEClient).getErrand(any(String.class));
        verifyNoMoreInteractions(mockOpenEClient);
    }

	@Test
    void getErrandStatus_ok() throws IOException {
        when(mockOpenEClient.getErrandStatus(any(String.class))).thenReturn("someAnswer".getBytes());

        final var response = openEIntegration.getErrandStatus("someFlowInstanceId");

        assertThat(response).isNotNull();

        verify(mockOpenEClient).getErrandStatus(any(String.class));
        verifyNoMoreInteractions(mockOpenEClient);
    }

	@Test
    void getErrandStatus_error() throws IOException {

        when(mockOpenEClient.getErrandStatus(any(String.class))).thenThrow(new NullPointerException());

        final var response = openEIntegration.getErrandStatus("someFlowInstanceId");

        assertThat(response).isEmpty();

        verify(mockOpenEClient).getErrandStatus(any(String.class));
        verifyNoMoreInteractions(mockOpenEClient);
    }
}
