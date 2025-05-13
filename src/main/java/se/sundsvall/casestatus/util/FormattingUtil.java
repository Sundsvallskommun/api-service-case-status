package se.sundsvall.casestatus.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;

public final class FormattingUtil {

	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private FormattingUtil() {}

	/**
	 * Returns the organization number in a formatted way.
	 *
	 * @param  organizationNumber The organization number to format
	 * @return                    The formatted organization number
	 */
	public static String getFormattedOrganizationNumber(final String organizationNumber) {

		// Control that the organizationNumber is not null and that it is a valid length
		if (IntStream.of(13, 12, 11, 10).anyMatch(i -> organizationNumber.length() == i)) {
			// Remove all non-digit characters
			final String cleanNumber = organizationNumber.replaceAll("\\D", "");

			if (cleanNumber.length() == 12) {
				// Insert the hyphen at the correct position
				return cleanNumber.substring(0, 8) + "-" + cleanNumber.substring(8);

			}
			if (cleanNumber.length() == 10) {
				return cleanNumber.substring(0, 6) + "-" + cleanNumber.substring(6);
			}
		}
		return organizationNumber;
	}

	public static String formatDateTime(final LocalDateTime dateString) {
		return dateString == null ? null : dateString.format(DATE_TIME_FORMAT);
	}

	public static String formatDateTime(final String dateString) {
		return dateString.isEmpty() ? null : LocalDateTime.parse(dateString).format(DATE_TIME_FORMAT);
	}
}
