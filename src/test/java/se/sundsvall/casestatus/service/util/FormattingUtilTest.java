package se.sundsvall.casestatus.service.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
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
		var result = FormattingUtil.getFormattedOrganizationNumber(organizationNumber);
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
		var result = FormattingUtil.getFormattedOrganizationNumber(organizationNumber);
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
		var result = FormattingUtil.getFormattedOrganizationNumber(organizationNumber);
		assertThat(result).isEqualTo(organizationNumber);
	}
}
