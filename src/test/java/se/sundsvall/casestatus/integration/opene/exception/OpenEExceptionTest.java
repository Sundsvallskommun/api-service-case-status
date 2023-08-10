package se.sundsvall.casestatus.integration.opene.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OpenEExceptionTest {

	@Test
	void testOpenEException() {
		final var message = "message";

		assertThat(new OpenEException(message)).hasMessage(message);
	}

	@Test
	void testOpenEExceptionWithCause() {
		final var message = "message";
		final var cause = new RuntimeException();

		assertThat(new OpenEException(message, cause)).hasMessage(message).hasCause(cause);
	}
}
