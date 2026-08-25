package se.sundsvall.casestatus.util;

import java.util.Set;

public final class Constants {

	public static final String SUPPORT_MANAGEMENT = "SupportManagement";
	public static final String CASE_MANAGEMENT = "CaseManagement";
	public static final String CASE_DATA = "CaseData";
	public static final String SUPPORT_MANAGEMENT_JOB_NAME = "UpdateSupportManagementStatuses";
	public static final String CASE_MANAGEMENT_JOB_NAME = "UpdateCaseManagementStatuses";
	public static final String CASE_DATA_JOB_NAME = "UpdateCaseDataStatuses";

	public static final String EXTERNAL_CHANNEL_E_SERVICE = "ESERVICE";
	public static final String INTERNAL_CHANNEL_E_SERVICE = "ESERVICE_INTERNAL";
	public static final String UNKNOWN = "Okänd";
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
	public static final String SUPPORT_MANAGEMENT_SYSTEM = "SUPPORT_MANAGEMENT";
	public static final String OPEN_E_PLATFORM = "OPEN_E_PLATFORM";
	public static final String CASE_NOT_FOUND = "Case with id %s not found";
	public static final Set<String> VALID_CHANNELS = Set.of(EXTERNAL_CHANNEL_E_SERVICE, INTERNAL_CHANNEL_E_SERVICE);
	public static final String DEFAULT_EXTERNAL_STATUS = "Handläggning pågår";

	/**
	 * Names the sources that did not answer, comma separated. Sent only when the aggregated result is incomplete;
	 * absent means every source contributed. A response header rather than a body field so that adding the signal
	 * does not change the response shape for existing subscribers.
	 */
	public static final String UNAVAILABLE_SOURCES_HEADER = "X-Unavailable-Sources";

	// Source identifiers reported in the UNAVAILABLE_SOURCES_HEADER
	public static final String SOURCE_CASE_MANAGEMENT = "CASE_MANAGEMENT";
	public static final String SOURCE_OPEN_E_PLATFORM = "OPEN_E_PLATFORM";
	public static final String SOURCE_SUPPORT_MANAGEMENT = "SUPPORT_MANAGEMENT";

	private Constants() {}
}
