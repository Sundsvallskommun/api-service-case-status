package se.sundsvall.casestatus.integration.opene;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casestatus.util.casestatuscache.domain.FamilyId;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenEIntegrationTests {

    @Mock
    private OpenEClient mockOpenEClient;

    @Mock
    private FamilyId familyId = FamilyId.ROKKANALELDSTAD;

    @InjectMocks
    private OpenEIntegration openEIntegration;


    @Test
    void getPdf_ok() throws IOException {
        when(mockOpenEClient.getPDF(any(String.class))).thenReturn("someAnswer".getBytes(StandardCharsets.UTF_8));

        var response = openEIntegration.getPdf("someExternalCaseId");

        assertThat(response.isEmpty()).isFalse();

        verify(mockOpenEClient, times(1)).getPDF(any(String.class));
        verifyNoMoreInteractions(mockOpenEClient);
    }

    @Test
    void getPdf_error() throws IOException {

        when(mockOpenEClient.getPDF(any(String.class))).thenThrow(new NullPointerException());

        var response = openEIntegration.getPdf("someExternalCaseId");

        assertThat(response.isEmpty()).isTrue();

        verify(mockOpenEClient, times(1)).getPDF(any(String.class));
        verifyNoMoreInteractions(mockOpenEClient);
    }


    @Test
    void getErrandIds_ok() throws IOException {
        when(mockOpenEClient.getErrandIds(any(String.class))).thenReturn("someAnswer".getBytes());
        when(familyId.getValue()).thenReturn(123);
        var response = openEIntegration.getErrandIds(familyId);

        assertThat(response).isNotNull();
        verify(mockOpenEClient, times(1)).getErrandIds(any(String.class));
        verifyNoMoreInteractions(mockOpenEClient);

    }


    @Test
    void getErrandIds_error() throws IOException {

        when(mockOpenEClient.getErrandIds(any(String.class))).thenThrow(new NullPointerException());

        var response = openEIntegration.getErrandIds(familyId);

        assertThat(response.length == 0).isTrue();

        verify(mockOpenEClient, times(1)).getErrandIds(any(String.class));
        verifyNoMoreInteractions(mockOpenEClient);
    }

    @Test
    void getErrand_ok() throws IOException {
        when(mockOpenEClient.getErrand(any(String.class))).thenReturn("someAnswer".getBytes());
        var response = openEIntegration.getErrand("someFlowInstanceId");

        assertThat(response).isNotNull();
        verify(mockOpenEClient, times(1)).getErrand(any(String.class));
        verifyNoMoreInteractions(mockOpenEClient);
    }

    @Test
    void getErrand_error() throws IOException {

        when(mockOpenEClient.getErrand(any(String.class))).thenThrow(new NullPointerException());

        var response = openEIntegration.getErrand("someFlowInstanceId");

        assertThat(response.length == 0).isTrue();

        verify(mockOpenEClient, times(1)).getErrand(any(String.class));
        verifyNoMoreInteractions(mockOpenEClient);
    }

    @Test
    void getErrandStatus_ok() throws IOException {
        when(mockOpenEClient.getErrandStatus(any(String.class))).thenReturn("someAnswer".getBytes());

        var response = openEIntegration.getErrandStatus("someFlowInstanceId");

        assertThat(response).isNotNull();

        verify(mockOpenEClient, times(1)).getErrandStatus(any(String.class));
        verifyNoMoreInteractions(mockOpenEClient);
    }

    @Test
    void getErrandStatus_error() throws IOException {

        when(mockOpenEClient.getErrandStatus(any(String.class))).thenThrow(new NullPointerException());

        var response = openEIntegration.getErrandStatus("someFlowInstanceId");

        assertThat(response.length == 0).isTrue();

        verify(mockOpenEClient, times(1)).getErrandStatus(any(String.class));
        verifyNoMoreInteractions(mockOpenEClient);
    }
}
