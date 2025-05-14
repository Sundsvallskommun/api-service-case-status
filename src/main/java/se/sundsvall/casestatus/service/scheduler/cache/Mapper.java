package se.sundsvall.casestatus.service.scheduler.cache;

import static se.sundsvall.casestatus.util.FormattingUtil.formatDateTime;

import generated.client.oep_integrator.CaseStatus;
import generated.client.oep_integrator.ModelCase;
import se.sundsvall.casestatus.integration.db.model.CaseEntity;
import us.codecraft.xsoup.Xsoup;

public final class Mapper {

	private static final String XPATH_FLOW_INSTANCE_ID_VALUE = "//header/FlowInstanceID/text()";
	private static final String XPATH_FAMILY_ID_VALUE = "//header/flow/familyId/text()";
	private static final String XPATH_ERRAND_TYPE_VALUE = "//header/flow/name/text()";
	private static final String XPATH_FIRST_SUBMITTED_VALUE = "//header/FirstSubmitted/text()";
	private static final String XPATH_LAST_STATUS_CHANGE_VALUE = "//header/LastSubmitted/text()";

	private Mapper() {
		// To prevent instantiation
	}

	private static CaseEntity buildCaseEntity(final CaseStatus caseStatus, final ModelCase oepCase, final String id, final String municipalityId, final boolean isPrivate) {
		final CaseEntity.CaseEntityBuilder builder = CaseEntity.builder()
			.withStatus(caseStatus.getName())
			.withContentType(oepCase.getTitle())
			.withFlowInstanceId(Xsoup.select(oepCase.getPayload(), XPATH_FLOW_INSTANCE_ID_VALUE).get())
			.withFamilyId(Xsoup.select(oepCase.getPayload(), XPATH_FAMILY_ID_VALUE).get())
			.withErrandType(Xsoup.select(oepCase.getPayload(), XPATH_ERRAND_TYPE_VALUE).get().trim())
			.withFirstSubmitted(formatDateTime(Xsoup.select(oepCase.getPayload(), XPATH_FIRST_SUBMITTED_VALUE).get()))
			.withLastStatusChange(formatDateTime(Xsoup.select(oepCase.getPayload(), XPATH_LAST_STATUS_CHANGE_VALUE).get()))
			.withMunicipalityId(municipalityId);

		if (isPrivate) {
			builder.withPersonId(id.replace("\"", ""));
		} else {
			builder.withOrganisationNumber(id);
		}

		return builder.build();
	}

	public static CaseEntity toCompanyCaseEntity(final CaseStatus caseStatus, final ModelCase oepCase, final String organisationNumber, final String municipalityId) {
		return buildCaseEntity(caseStatus, oepCase, organisationNumber, municipalityId, false);
	}

	public static CaseEntity toPrivateCaseEntity(final CaseStatus caseStatus, final ModelCase oepCase, final String personId, final String municipalityId) {
		return buildCaseEntity(caseStatus, oepCase, personId, municipalityId, true);
	}

	public static CaseEntity toUnknownCaseEntity(final CaseStatus caseStatus, final ModelCase oepCase, final String municipalityId) {
		return buildCaseEntity(caseStatus, oepCase, null, municipalityId, false);
	}
}
