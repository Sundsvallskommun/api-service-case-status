package se.sundsvall.casestatus.utility;

import java.util.Set;

public final class Constants {

	public static final String EXTERNAL_CHANNEL_E_SERVICE = "ESERVICE";
	public static final String INTERNAL_CHANNEL_E_SERVICE = "ESERVICE_INTERNAL";
	public static final Set<String> VALID_CHANNELS = Set.of(EXTERNAL_CHANNEL_E_SERVICE, INTERNAL_CHANNEL_E_SERVICE);

	private Constants() {
		// Private constructor to prevent instantiation
	}

}
