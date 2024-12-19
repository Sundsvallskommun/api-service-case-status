package se.sundsvall.casestatus.service;

import static se.sundsvall.casestatus.service.CaseStatusService.MISSING;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import se.sundsvall.casestatus.api.model.CasePdfResponse;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.api.model.OepStatusResponse;
import se.sundsvall.casestatus.integration.db.model.CaseEntity;

public final class Mapper {

	private Mapper() {
		// To prevent instantiation
	}

	static CaseStatusResponse mapToCaseStatusResponse(final CaseEntity cachedCaseStatus) {
		return CaseStatusResponse.builder()
			.withExternalCaseId(cachedCaseStatus.getFlowInstanceId())
			.withCaseType(cachedCaseStatus.getErrandType())
			.withStatus(cachedCaseStatus.getStatus())
			.withFirstSubmitted(cachedCaseStatus.getFirstSubmitted())
			.withLastStatusChange(cachedCaseStatus.getLastStatusChange())
			.withIsOpenEErrand(true)
			.build();
	}

	static CaseStatusResponse toCaseStatusResponse(final CaseStatusDTO caseStatus, final String serviceName, final String status, final String timestamp) {
		return CaseStatusResponse.builder()
			.withId(caseStatus.getCaseId())
			.withExternalCaseId(caseStatus.getExternalCaseId())
			.withCaseType(serviceName)
			.withStatus(status)
			.withLastStatusChange(timestamp)
			.withFirstSubmitted(MISSING)
			.withIsOpenEErrand(false)
			.build();
	}

	static CasePdfResponse toCasePdfResponse(final String externalCaseId, final String pdf) {
		return CasePdfResponse.builder()
			.withExternalCaseId(externalCaseId)
			.withBase64(pdf)
			.build();
	}

	static OepStatusResponse toOepStatusResponse(final String openEId) {
		return OepStatusResponse.builder()
			.withKey("status")
			.withValue(openEId)
			.build();
	}
}
