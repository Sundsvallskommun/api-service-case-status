package se.sundsvall.casestatus.service.mapper;

import static se.sundsvall.casestatus.util.Constants.OPEN_E_PLATFORM;
import static se.sundsvall.casestatus.util.FormattingUtil.formatDateTime;

import generated.client.oep_integrator.CaseEnvelope;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;
import org.springframework.core.io.InputStreamResource;
import se.sundsvall.casestatus.api.model.CasePdfResponse;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.api.model.OepStatusResponse;
import se.sundsvall.casestatus.integration.db.model.CaseEntity;

public final class OpenEMapper {

	private OpenEMapper() {}

	public static CaseStatusResponse toCaseStatusResponse(final CaseEntity caseEntity) {
		return Optional.ofNullable(caseEntity)
			.map(entity -> CaseStatusResponse.builder()
				.withCaseId(entity.getFlowInstanceId())
				.withCaseType(entity.getErrandType())
				.withStatus(entity.getStatus())
				.withLastStatusChange(entity.getLastStatusChange())
				.withFirstSubmitted(entity.getFirstSubmitted())
				.withSystem(OPEN_E_PLATFORM)
				.withExternalCaseId(entity.getFlowInstanceId())
				.withErrandNumber(entity.getFlowInstanceId())
				.build())
			.orElse(null);
	}

	public static CasePdfResponse toCasePdfResponse(final String externalCaseId, final InputStreamResource pdf) throws IOException {
		return CasePdfResponse.builder()
			.withExternalCaseId(externalCaseId)
			.withBase64(Base64.getEncoder().encodeToString(pdf.getInputStream().readAllBytes()))
			.build();
	}

	public static OepStatusResponse toOepStatusResponse(final String openEId) {
		return OepStatusResponse.builder()
			.withKey("status")
			.withValue(openEId)
			.build();
	}

	public static CaseStatusResponse toCaseStatusResponse(final CaseEnvelope caseEnvelope) {

		return Optional.ofNullable(caseEnvelope)
			.map(CaseEnvelope::getStatus)
			.map(status -> CaseStatusResponse.builder()
				.withCaseId(caseEnvelope.getFlowInstanceId())
				.withCaseType(caseEnvelope.getDisplayName())
				.withStatus(status.getName())
				.withLastStatusChange(formatDateTime(caseEnvelope.getStatusUpdated()))
				.withFirstSubmitted(formatDateTime(caseEnvelope.getCreated()))
				.withSystem(OPEN_E_PLATFORM)
				.withExternalCaseId(caseEnvelope.getFlowInstanceId())
				.withErrandNumber(caseEnvelope.getFlowInstanceId())
				.build())
			.orElse(null);
	}
}
