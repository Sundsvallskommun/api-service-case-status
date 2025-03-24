package se.sundsvall.casestatus.util;

import java.util.stream.IntStream;

public final class FormattingUtil {

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
}
