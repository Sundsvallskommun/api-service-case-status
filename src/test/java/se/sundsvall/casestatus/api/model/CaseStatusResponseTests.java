package se.sundsvall.casestatus.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CaseStatusResponseTests {

	@Test
	void builderAndGettersWorkAsExpected() {
		final var response = CaseStatusResponse.builder()
			.withCaseId("someId")
			.withExternalCaseId("someExternalCaseId")
			.withStatus("someStatus")
			.withCaseType("someCaseType")
			.withFirstSubmitted("someValue")
			.withLastStatusChange("someValue")
			.build();

		assertThat(response.getCaseId()).isEqualTo("someId");
		assertThat(response.getExternalCaseId()).isEqualTo("someExternalCaseId");
		assertThat(response.getStatus()).isEqualTo("someStatus");
		assertThat(response.getCaseType()).isEqualTo("someCaseType");
		assertThat(response.getFirstSubmitted()).isEqualTo("someValue");
		assertThat(response.getLastStatusChange()).isEqualTo("someValue");
	}
}
