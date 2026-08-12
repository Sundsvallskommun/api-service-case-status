package se.sundsvall.casestatus.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.apache.commons.lang3.StringUtils.isBlank;

public final class FormattingUtil {

	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private FormattingUtil() {}

	public static String formatDateTime(final LocalDateTime dateTime) {
		return dateTime == null ? null : dateTime.format(DATE_TIME_FORMAT);
	}

	/**
	 * Formats an ISO-8601 local date time string. Returns null for missing or blank input.
	 */
	public static String formatDateTime(final String dateString) {
		return isBlank(dateString) ? null : LocalDateTime.parse(dateString.trim()).format(DATE_TIME_FORMAT);
	}
}
