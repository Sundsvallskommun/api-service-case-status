package se.sundsvall.casestatus.api.model;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casestatus.api.model.SourceStatus.STATUS_OK;

class CaseStatusesResponseTests {

	@Test
	void builderAndGettersWorkAsExpected() {
		final var caseStatus = CaseStatusResponse.builder().withCaseId("someId").build();
		final var sourceStatus = SourceStatus.builder().withSource("CASE_MANAGEMENT").withStatus(STATUS_OK).build();

		final var response = CaseStatusesResponse.builder()
			.withCases(List.of(caseStatus))
			.withSources(List.of(sourceStatus))
			.build();

		assertThat(response.getCases()).containsExactly(caseStatus);
		assertThat(response.getSources()).containsExactly(sourceStatus);
	}
}
