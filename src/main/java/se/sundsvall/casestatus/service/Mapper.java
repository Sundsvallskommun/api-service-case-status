package se.sundsvall.casestatus.service;

import static se.sundsvall.casestatus.service.CaseStatusService.DATE_TIME_FORMATTER;
import static se.sundsvall.casestatus.service.CaseStatusService.MISSING;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import java.util.Optional;
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

	static CaseStatusResponse toCaseStatusResponse(final Errand errand) {
		final var externalCaseId = getExternalCaseId(errand);

		final var modified = Optional.ofNullable(errand.getModified())
			.map(modifiedDate -> modifiedDate.format(DATE_TIME_FORMATTER))
			.orElse(null);

		return CaseStatusResponse.builder()
			.withId(errand.getId())
			.withExternalCaseId(externalCaseId.orElse(null))
			.withCaseType(errand.getClassification().getType())
			.withStatus(errand.getStatus())
			.withLastStatusChange(modified)
			.withFirstSubmitted(errand.getCreated().format(DATE_TIME_FORMATTER))
			.withIsOpenEErrand(externalCaseId.isPresent())
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

	private static Optional<String> getExternalCaseId(final Errand errand) {
		final boolean familyIdExists = errand.getExternalTags().stream()
			.anyMatch(tag -> tag.getKey().equals("familyId"));

		if (familyIdExists) {
			return errand.getExternalTags().stream()
				.filter(tag -> tag.getKey().equals("caseId"))
				.findFirst()
				.map(ExternalTag::getValue);
		}

		return Optional.empty();
	}
}
