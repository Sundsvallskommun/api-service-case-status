package se.sundsvall.casestatus.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CaseStatusResponseTests {

	@Test
	void builderAndGettersWorkAsExpected() {
		var response = CaseStatusResponse.builder()
			.withId("someId")
			.withExternalCaseId("someExternalCaseId")
			.withStatus("someStatus")
			.withCaseType("someCaseType")
			.withFirstSubmitted("someValue")
			.withLastStatusChange("someValue")
			.withIsOpenEErrand(true)
			.build();

		assertThat(response.getId()).isEqualTo("someId");
		assertThat(response.getExternalCaseId()).isEqualTo("someExternalCaseId");
		assertThat(response.getStatus()).isEqualTo("someStatus");
		assertThat(response.getCaseType()).isEqualTo("someCaseType");
		assertThat(response.getFirstSubmitted()).isEqualTo("someValue");
		assertThat(response.getLastStatusChange()).isEqualTo("someValue");
		assertThat(response.isOpenEErrand()).isTrue();
	}
}
