package se.sundsvall.casestatus.service.mapper;

import static se.sundsvall.casestatus.util.Constants.DATE_TIME_FORMAT;
import static se.sundsvall.casestatus.util.Constants.MISSING;
import static se.sundsvall.casestatus.util.Constants.SUPPORT_MANAGEMENT;

import generated.se.sundsvall.opene.SetStatus;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;

public final class SupportManagementMapper {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

	private SupportManagementMapper() {}

	public static CaseStatusResponse toCaseStatusResponse(final Errand errand, final String namespace) {
		final var externalCaseId = getExternalCaseId(errand);

		final var modified = Optional.ofNullable(errand.getModified())
			.map(modifiedDate -> modifiedDate.format(DATE_TIME_FORMATTER))
			.orElse(MISSING);

		final var firstSubmitted = Optional.ofNullable(errand.getCreated())
			.map(createdDate -> createdDate.format(DATE_TIME_FORMATTER))
			.orElse(MISSING);

		return CaseStatusResponse.builder()
			.withCaseId(errand.getId())
			.withExternalCaseId(externalCaseId.orElse(null))
			.withCaseType(errand.getClassification().getType())
			.withStatus(errand.getStatus())
			.withLastStatusChange(modified)
			.withFirstSubmitted(firstSubmitted)
			.withSystem(SUPPORT_MANAGEMENT)
			.withErrandNumber(errand.getErrandNumber())
			.withNamespace(namespace)
			.build();
	}

	public static SetStatus toSetStatus(final Errand errand, final String status) {
		return getExternalCaseId(errand).map(externalCaseId -> new SetStatus()
			.withStatusAlias(status)
			.withFlowInstanceID(Integer.parseInt(externalCaseId)))
			.orElse(null);
	}

	static Optional<String> getExternalCaseId(final Errand errand) {
		final boolean familyIdExists = errand.getExternalTags().stream()
			.anyMatch(tag -> "familyId".equalsIgnoreCase(tag.getKey()));

		if (familyIdExists) {
			return errand.getExternalTags().stream()
				.filter(tag -> "caseId".equalsIgnoreCase(tag.getKey()))
				.findFirst()
				.map(ExternalTag::getValue);
		}

		return Optional.empty();
	}
}
