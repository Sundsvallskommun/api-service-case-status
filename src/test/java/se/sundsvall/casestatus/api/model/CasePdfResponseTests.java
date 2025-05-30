package se.sundsvall.casestatus.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CasePdfResponseTests {

	@Test
	void builderAndGettersWorkAsExpected() {
		final var response = CasePdfResponse.builder()
			.withExternalCaseId("someExternalCaseId")
			.withBase64("someBase64String")
			.build();

		assertThat(response.getExternalCaseId()).isEqualTo("someExternalCaseId");
		assertThat(response.getBase64()).isEqualTo("someBase64String");
	}
}
