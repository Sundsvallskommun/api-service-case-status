package se.sundsvall.casestatus.service.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import se.sundsvall.casestatus.util.FormattingUtil;

class FormattingUtilTest {

	/**
	 * Test values with length 12.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
		"199001010101", "200001010101", "198001010101"
	})
	void getFormattedOrganizationNumber_1(final String organizationNumber) {
		final var result = FormattingUtil.getFormattedOrganizationNumber(organizationNumber);
		assertThat(result).isEqualTo(organizationNumber.substring(0, 8) + "-" + organizationNumber.substring(8));
	}

	/**
	 * Test values with length 10.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
		"9001010101", "0001010101", "8001010101"
	})
	void getFormattedOrganizationNumber_2(final String organizationNumber) {
		final var result = FormattingUtil.getFormattedOrganizationNumber(organizationNumber);
		assertThat(result).isEqualTo(organizationNumber.substring(0, 6) + "-" + organizationNumber.substring(6));
	}

	/**
	 * Test values with neither length 10 nor 12.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
		"123456789", "12345678901", "1234567890123"
	})
	void getFormattedOrganizationNumber_3(final String organizationNumber) {
		final var result = FormattingUtil.getFormattedOrganizationNumber(organizationNumber);
		assertThat(result).isEqualTo(organizationNumber);
	}

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
