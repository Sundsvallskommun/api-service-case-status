package se.sundsvall.casestatus.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FormattingUtil {

	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private FormattingUtil() {}

	public static String formatDateTime(final LocalDateTime dateString) {
		return dateString == null ? null : dateString.format(DATE_TIME_FORMAT);
	}

	public static String formatDateTime(final String dateString) {
		return dateString.isEmpty() ? null : LocalDateTime.parse(dateString).format(DATE_TIME_FORMAT);
	}
}
