package se.sundsvall.casestatus.service.util;

import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import se.sundsvall.casestatus.util.FormattingUtil;

import static org.assertj.core.api.Assertions.assertThat;

class FormattingUtilTest {

	@ParameterizedTest
	@CsvSource({
		"2023-10-01T12:00, 2023-10-01 12:00",
		"2023-10-01T00:00, 2023-10-01 00:00",
		"2023-12-31T23:59, 2023-12-31 23:59"
	})
	void formatDateTime_LocalDateTime(final LocalDateTime input, final String expected) {
		final String result = FormattingUtil.formatDateTime(input);
		assertThat(result).isEqualTo(expected);
	}

	@ParameterizedTest
	@CsvSource({
		"2023-10-01T12:00, 2023-10-01 12:00",
		"2023-10-01T00:00, 2023-10-01 00:00",
		"2023-12-31T23:59, 2023-12-31 23:59"
	})
	void formatDateTime_String(final String input, final String expected) {
		final String result = FormattingUtil.formatDateTime(input);
		assertThat(result).isEqualTo(expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"invalid-date-time"
	})
	void formatDateTime_String_Invalid(final String input) {

		Assertions.assertThatThrownBy(() -> FormattingUtil.formatDateTime(input))
			.isInstanceOf(java.time.format.DateTimeParseException.class)
			.hasMessageContaining("Text '" + input + "' could not be parsed at index 0");

	}

}
