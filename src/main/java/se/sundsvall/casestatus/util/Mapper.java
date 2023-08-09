package se.sundsvall.casestatus.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import se.sundsvall.casestatus.integration.db.domain.CacheCompanyCaseStatus;
import se.sundsvall.casestatus.integration.db.domain.CachePrivateCaseStatus;
import se.sundsvall.casestatus.integration.db.domain.CacheUnknownCaseStatus;
import us.codecraft.xsoup.Xsoup;

@Component
public class Mapper {

	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private static final String XPATH_STATUS_VALUE = "//status/name/text()";
	private static final String XPATH_CONTENT_TYPE_VALUE = "//status/contentType/text()";
	private static final String XPATH_FLOW_INSTANCE_ID_VALUE = "//header/FlowInstanceID/text()";
	private static final String XPATH_FAMILY_ID_VALUE = "//header/flow/familyId/text()";
	private static final String XPATH_ERRAND_TYPE_VALUE = "//header/flow/name/text()";
	private static final String XPATH_FIRST_SUBMITTED_VALUE = "//header/FirstSubmitted/text()";
	private static final String XPATH_LAST_STATUS_CHANGE_VALUE = "//header/LastSubmitted/text()";

	private String formatDateTime(String dateString) {
		return dateString.isEmpty() ? null : LocalDateTime.parse(dateString).format(DATE_TIME_FORMAT);
	}

	public CacheCompanyCaseStatus toCacheCompanyCaseStatus(Document statusObject, Document errandObject, String organisationNumber) {
		return CacheCompanyCaseStatus.builder()
			.withStatus(Xsoup.select(statusObject, XPATH_STATUS_VALUE).get())
			.withContentType(Xsoup.select(statusObject, XPATH_CONTENT_TYPE_VALUE).get())
			.withFlowInstanceID(Xsoup.select(errandObject, XPATH_FLOW_INSTANCE_ID_VALUE).get())
			.withFamilyID(Xsoup.select(errandObject, XPATH_FAMILY_ID_VALUE).get())
			.withErrandType(Xsoup.select(errandObject, XPATH_ERRAND_TYPE_VALUE).get().trim())
			.withFirstSubmitted(formatDateTime(Xsoup.select(errandObject, XPATH_FIRST_SUBMITTED_VALUE).get()))
			.withLastStatusChange(formatDateTime(Xsoup.select(errandObject, XPATH_LAST_STATUS_CHANGE_VALUE).get()))
			.withOrganisationNumber(organisationNumber)
			.build();

	}

	public CachePrivateCaseStatus toCachePrivateCaseStatus(Document statusObject, Document errandObject, String personId) {
		return CachePrivateCaseStatus.builder()
			.withStatus(Xsoup.select(statusObject, XPATH_STATUS_VALUE).get())
			.withContentType(Xsoup.select(statusObject, XPATH_CONTENT_TYPE_VALUE).get())
			.withFlowInstanceID(Xsoup.select(errandObject, XPATH_FLOW_INSTANCE_ID_VALUE).get())
			.withFamilyID(Xsoup.select(errandObject, XPATH_FAMILY_ID_VALUE).get())
			.withErrandType(Xsoup.select(errandObject, XPATH_ERRAND_TYPE_VALUE).get().trim())
			.withFirstSubmitted(formatDateTime(Xsoup.select(errandObject, XPATH_FIRST_SUBMITTED_VALUE).get()))
			.withLastStatusChange(formatDateTime(Xsoup.select(errandObject, XPATH_LAST_STATUS_CHANGE_VALUE).get()))
			.withPersonId(personId.replace("\"", ""))
			.build();
	}

	public CacheUnknownCaseStatus toCacheUnknowCaseStatus(Document statusObject, Document errandObject) {
		return CacheUnknownCaseStatus.builder()
			.withStatus(Xsoup.select(statusObject, XPATH_STATUS_VALUE).get())
			.withContentType(Xsoup.select(statusObject, XPATH_CONTENT_TYPE_VALUE).get())
			.withFlowInstanceID(Xsoup.select(errandObject, XPATH_FLOW_INSTANCE_ID_VALUE).get())
			.withFamilyID(Xsoup.select(errandObject, XPATH_FAMILY_ID_VALUE).get())
			.withErrandType(Xsoup.select(errandObject, XPATH_ERRAND_TYPE_VALUE).get().trim())
			.withFirstSubmitted(formatDateTime(Xsoup.select(errandObject, XPATH_FIRST_SUBMITTED_VALUE).get()))
			.withLastStatusChange(formatDateTime(Xsoup.select(errandObject, XPATH_LAST_STATUS_CHANGE_VALUE).get()))
			.build();
	}
}
