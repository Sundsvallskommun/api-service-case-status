package se.sundsvall.casestatus.integration.incident.domain;

import static org.assertj.core.api.Assertions.assertThat;

import generated.se.sundsvall.incident.IncidentOepResponse;
import org.junit.jupiter.api.Test;

class IncidentStatusTests {

	@Test
	void gettersAndSettersWorksAsExpected() {
		var status = new IncidentOepResponse();
		status.setIncidentId("someIncidentId");
		status.setExternalCaseId("someExternalCaseId");
		status.setStatusId(456);
		status.setStatusText("someStatusText");

		assertThat(status.getIncidentId()).isEqualTo("someIncidentId");
		assertThat(status.getExternalCaseId()).isEqualTo("someExternalCaseId");
		assertThat(status.getStatusId()).isEqualTo(456);
		assertThat(status.getStatusText()).isEqualTo("someStatusText");
	}
}
