package se.sundsvall.casestatus.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casestatus.api.model.SourceStatus.STATUS_UNAVAILABLE;

class SourceStatusTests {

	@Test
	void builderAndGettersWorkAsExpected() {
		final var sourceStatus = SourceStatus.builder()
			.withSource("OPEN_E_PLATFORM")
			.withStatus(STATUS_UNAVAILABLE)
			.build();

		assertThat(sourceStatus.getSource()).isEqualTo("OPEN_E_PLATFORM");
		assertThat(sourceStatus.getStatus()).isEqualTo(STATUS_UNAVAILABLE);
	}
}
