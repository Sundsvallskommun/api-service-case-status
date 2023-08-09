package se.sundsvall.casestatus.integration.incident;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.incident.IncidentOepResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncidentIntegrationTests {

    @Mock
    private IncidentClient mockIncidentClient;

    @InjectMocks
    private IncidentIntegration incidentIntegration;

    @Test
    void getIncidentStatus_ok() {
        var incidentStatus = new IncidentOepResponse()
            .incidentId("someIncidentId")
            .externalCaseId("someExternalCaseId")
            .statusId(567)
            .statusText("someStatusTxt");


        when(mockIncidentClient.getIncidentStatusForExternalCaseId(any(String.class))).thenReturn(incidentStatus);

        var result = incidentIntegration.getIncidentStatus("someExternalCaseId");

        assertThat(result).isNotNull().isPresent();
        assertThat(result.get().getIncidentId()).isEqualTo(incidentStatus.getIncidentId());
        assertThat(result.get().getExternalCaseId()).isEqualTo(incidentStatus.getExternalCaseId());
        assertThat(result.get().getStatusId()).isEqualTo(incidentStatus.getStatusId());
        assertThat(result.get().getStatusText()).isEqualTo(incidentStatus.getStatusText());

        verify(mockIncidentClient).getIncidentStatusForExternalCaseId(any(String.class));
        verifyNoMoreInteractions(mockIncidentClient);
    }

    @Test
    void getIncidentStatus_error(){
        when(mockIncidentClient.getIncidentStatusForExternalCaseId(any(String.class))).thenThrow(new RuntimeException());

        var result = incidentIntegration.getIncidentStatus("someExternalCaseId");

        assertThat(result).isEmpty();

        verify(mockIncidentClient).getIncidentStatusForExternalCaseId(any(String.class));
        verifyNoMoreInteractions(mockIncidentClient);
    }
}
