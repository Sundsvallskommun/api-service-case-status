package se.sundsvall.casestatus.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OepStatusResponseTests {

	@Test
	void builderAndGettersWorkAsExpected() {
		final var response = OepStatusResponse.builder()
			.withKey("someKey")
			.withValue("someValue")
			.build();

		assertThat(response.getKey()).isEqualTo("someKey");
		assertThat(response.getValue()).isEqualTo("someValue");
	}
}
