package se.sundsvall.casestatus.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OepStatusResponseTests {

	@Test
	void builderAndGettersWorkAsExpected() {
		var response = OepStatusResponse.builder()
			.withKey("someKey")
			.withValue("someValue")
			.build();

		assertThat(response.getKey()).isEqualTo("someKey");
		assertThat(response.getValue()).isEqualTo("someValue");
	}
}
