package se.sundsvall.casestatus.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casestatus.util.FilterUtil.escapeFilterValue;

class FilterUtilTest {

	@Test
	void escapesSingleQuote() {
		assertThat(escapeFilterValue("O'Brien")).isEqualTo("O\\'Brien");
	}

	@Test
	void escapesBackslash() {
		assertThat(escapeFilterValue("a\\b")).isEqualTo("a\\\\b");
	}

	@Test
	void escapesBackslashBeforeQuote() {
		// The backslash must be escaped first, otherwise the backslash added by the quote escaping is escaped again and
		// the quote ends up unescaped
		assertThat(escapeFilterValue("a\\'b")).isEqualTo("a\\\\\\'b");
	}

	@Test
	void leavesOrdinaryValuesUntouched() {
		assertThat(escapeFilterValue("Körsbärsdalen 123")).isEqualTo("Körsbärsdalen 123");
	}

	@Test
	void handlesNullAndEmpty() {
		assertThat(escapeFilterValue(null)).isNull();
		assertThat(escapeFilterValue("")).isEmpty();
	}
}
