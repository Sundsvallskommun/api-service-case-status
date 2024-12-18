package se.sundsvall.casestatus.service.scheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.db.model.CaseEntity;
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

	private String formatDateTime(final String dateString) {
		return dateString.isEmpty() ? null : LocalDateTime.parse(dateString).format(DATE_TIME_FORMAT);
	}

	private CaseEntity buildCaseEntity(final Document statusObject, final Document errandObject, final String id, final String municipalityId, final boolean isPrivate) {
		final CaseEntity.CaseEntityBuilder builder = CaseEntity.builder()
			.withStatus(Xsoup.select(statusObject, XPATH_STATUS_VALUE).get())
			.withContentType(Xsoup.select(statusObject, XPATH_CONTENT_TYPE_VALUE).get())
			.withFlowInstanceId(Xsoup.select(errandObject, XPATH_FLOW_INSTANCE_ID_VALUE).get())
			.withFamilyId(Xsoup.select(errandObject, XPATH_FAMILY_ID_VALUE).get())
			.withErrandType(Xsoup.select(errandObject, XPATH_ERRAND_TYPE_VALUE).get().trim())
			.withFirstSubmitted(formatDateTime(Xsoup.select(errandObject, XPATH_FIRST_SUBMITTED_VALUE).get()))
			.withLastStatusChange(formatDateTime(Xsoup.select(errandObject, XPATH_LAST_STATUS_CHANGE_VALUE).get()))
			.withMunicipalityId(municipalityId);

		if (isPrivate) {
			builder.withPersonId(id.replace("\"", ""));
		} else {
			builder.withOrganisationNumber(id);
		}

		return builder.build();
	}

	public CaseEntity toCacheCompanyCaseStatus(final Document statusObject, final Document errandObject, final String organisationNumber, final String municipalityId) {
		return buildCaseEntity(statusObject, errandObject, organisationNumber, municipalityId, false);
	}

	public CaseEntity toCachePrivateCaseStatus(final Document statusObject, final Document errandObject, final String personId, final String municipalityId) {
		return buildCaseEntity(statusObject, errandObject, personId, municipalityId, true);
	}

	public CaseEntity toCacheUnknownCaseStatus(final Document statusObject, final Document errandObject, final String municipalityId) {
		return buildCaseEntity(statusObject, errandObject, null, municipalityId, false);
	}
}
