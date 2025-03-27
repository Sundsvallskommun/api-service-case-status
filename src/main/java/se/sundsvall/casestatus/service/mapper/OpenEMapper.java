package se.sundsvall.casestatus.service.mapper;

import static se.sundsvall.casestatus.util.Constants.OPEN_E_PLATFORM;

import java.util.Optional;
import se.sundsvall.casestatus.api.model.CasePdfResponse;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.api.model.OepStatusResponse;
import se.sundsvall.casestatus.integration.db.model.CaseEntity;

public final class OpenEMapper {

	private OpenEMapper() {}

	public static CaseStatusResponse toCaseStatusResponse(final CaseEntity caseEntity) {
		return Optional.ofNullable(caseEntity).map(entity -> CaseStatusResponse.builder()
			.withCaseId(caseEntity.getFlowInstanceId())
			.withCaseType(caseEntity.getErrandType())
			.withStatus(caseEntity.getStatus())
			.withLastStatusChange(caseEntity.getLastStatusChange())
			.withFirstSubmitted(caseEntity.getFirstSubmitted())
			.withSystem(OPEN_E_PLATFORM)
			.withExternalCaseId(caseEntity.getFlowInstanceId())
			.withErrandNumber(null)
			.withNamespace(null)
			.build())
			.orElse(null);
	}

	public static CasePdfResponse toCasePdfResponse(final String externalCaseId, final String pdf) {
		return CasePdfResponse.builder()
			.withExternalCaseId(externalCaseId)
			.withBase64(pdf)
			.build();
	}

	public static OepStatusResponse toOepStatusResponse(final String openEId) {
		return OepStatusResponse.builder()
			.withKey("status")
			.withValue(openEId)
			.build();
	}

}
