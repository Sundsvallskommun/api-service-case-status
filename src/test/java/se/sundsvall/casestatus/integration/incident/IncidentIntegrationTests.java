package se.sundsvall.casestatus.integration.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import generated.se.sundsvall.incident.IncidentOepResponse;

@ExtendWith(MockitoExtension.class)
class IncidentIntegrationTests {

	private static final String EXTERNAL_CASE_ID = "someExternalCaseId";

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private IncidentClient mockIncidentClient;

	@InjectMocks
	private IncidentIntegration incidentIntegration;

	@Test
	void getIncidentStatus_ok() {
		final var incidentStatus = new IncidentOepResponse()
			.incidentId("someIncidentId")
			.externalCaseId(EXTERNAL_CASE_ID)
			.statusId(567)
			.statusText("someStatusTxt");

		when(mockIncidentClient.getIncidentStatusForExternalCaseId(MUNICIPALITY_ID, EXTERNAL_CASE_ID)).thenReturn(incidentStatus);

		final var result = incidentIntegration.getIncidentStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID);

		assertThat(result).isNotNull().isPresent();
		assertThat(result.get().getIncidentId()).isEqualTo(incidentStatus.getIncidentId());
		assertThat(result.get().getExternalCaseId()).isEqualTo(incidentStatus.getExternalCaseId());
		assertThat(result.get().getStatusId()).isEqualTo(incidentStatus.getStatusId());
		assertThat(result.get().getStatusText()).isEqualTo(incidentStatus.getStatusText());

		verify(mockIncidentClient).getIncidentStatusForExternalCaseId(MUNICIPALITY_ID, EXTERNAL_CASE_ID);
		verifyNoMoreInteractions(mockIncidentClient);
	}

	@Test
	void getIncidentStatus_error() {
		when(mockIncidentClient.getIncidentStatusForExternalCaseId(MUNICIPALITY_ID, EXTERNAL_CASE_ID)).thenThrow(new RuntimeException());

		final var result = incidentIntegration.getIncidentStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID);

		assertThat(result).isEmpty();

		verify(mockIncidentClient).getIncidentStatusForExternalCaseId(MUNICIPALITY_ID, EXTERNAL_CASE_ID);
		verifyNoMoreInteractions(mockIncidentClient);
	}

}
