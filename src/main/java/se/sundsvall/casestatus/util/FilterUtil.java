package se.sundsvall.casestatus.util;

public final class FilterUtil {

	private FilterUtil() {}

	/**
	 * Escapes a value before it is embedded in a single quoted spring-filter literal. Without this an apostrophe in the
	 * value closes the literal and the remainder is parsed as filter syntax.
	 * <p>
	 * spring-filter escapes with a backslash - {@code '} becomes {@code \'} and {@code \} becomes {@code \\}. The
	 * backslash must be escaped first, otherwise the backslash added by the quote escaping is escaped a second time.
	 * <p>
	 * Note that this does not neutralise the wildcard {@code *} of the like operator, which is a valid character inside
	 * a literal. Wildcards are rejected by input validation instead.
	 *
	 * @param  value the raw value, may be null
	 * @return       the escaped value, or null if the value was null
	 */
	public static String escapeFilterValue(final String value) {
		return value == null ? null
			: value
				.replace("\\", "\\\\")
				.replace("'", "\\'");
	}
}
