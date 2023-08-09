package se.sundsvall.casestatus.integration.db.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DatabaseExceptionTest {

	@Test
	void testDatabaseException() {
		final var message = "message";

		assertThat(new DatabaseException(message)).hasMessage(message);
	}

	@Test
	void testDatabaseExceptionWithCause() {
		final var message = "message";
		final var cause = new RuntimeException();

		assertThat(new DatabaseException(message, cause)).hasMessage(message).hasCause(cause);
	}
}
