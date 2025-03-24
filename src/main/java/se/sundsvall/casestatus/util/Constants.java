package se.sundsvall.casestatus.util;

import java.util.Set;

public final class Constants {

	public static final String EXTERNAL_CHANNEL_E_SERVICE = "ESERVICE";
	public static final String INTERNAL_CHANNEL_E_SERVICE = "ESERVICE_INTERNAL";
	public static final String MISSING = "Saknas";
	public static final String UNKNOWN = "Okänd";
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
	public static final String SUPPORT_MANAGEMENT = "SUPPORT_MANAGEMENT";
	public static final String OPEN_E_PLATFORM = "OPEN_E_PLATFORM";
	public static final Set<String> VALID_CHANNELS = Set.of(EXTERNAL_CHANNEL_E_SERVICE, INTERNAL_CHANNEL_E_SERVICE);

	private Constants() {
		// Private constructor to prevent instantiation
	}

}
