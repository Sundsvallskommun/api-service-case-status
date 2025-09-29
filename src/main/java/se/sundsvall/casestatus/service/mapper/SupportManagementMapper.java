package se.sundsvall.casestatus.service.mapper;

import static java.util.Objects.isNull;
import static java.util.Optional.empty;
import static se.sundsvall.casestatus.util.Constants.DATE_TIME_FORMAT;
import static se.sundsvall.casestatus.util.Constants.DEFAULT_EXTERNAL_STATUS;
import static se.sundsvall.casestatus.util.Constants.SUPPORT_MANAGEMENT;

import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.integration.db.model.StatusesEntity;

@Component
public class SupportManagementMapper {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

	public static Optional<String> getExternalCaseId(final Errand errand) {
		final boolean familyIdExists = errand.getExternalTags().stream()
			.anyMatch(tag -> "familyId".equalsIgnoreCase(tag.getKey()));

		if (familyIdExists) {
			return errand.getExternalTags().stream()
				.filter(tag -> "caseId".equalsIgnoreCase(tag.getKey()))
				.findFirst()
				.map(ExternalTag::getValue);
		}

		return empty();
	}

	public CaseStatusResponse toCaseStatusResponse(final Errand errand, final String namespace, StatusesEntity statuses, String classificationName) {
		final var externalCaseId = getExternalCaseId(errand);

		final var modified = Optional.ofNullable(errand.getModified())
			.map(modifiedDate -> modifiedDate.format(DATE_TIME_FORMATTER))
			.orElse(null);

		final var firstSubmitted = Optional.ofNullable(errand.getCreated())
			.map(createdDate -> createdDate.format(DATE_TIME_FORMATTER))
			.orElse(null);

		return CaseStatusResponse.builder()
			.withCaseId(errand.getId())
			.withExternalCaseId(externalCaseId.orElse(null))
			.withCaseType(classificationName)
			.withStatus(Optional.ofNullable(statuses.getOepStatus()).orElse(statuses.getSupportManagementStatus()))
			.withExternalStatus(getExternalStatus(statuses))
			.withLastStatusChange(modified)
			.withFirstSubmitted(firstSubmitted)
			.withSystem(SUPPORT_MANAGEMENT)
			.withErrandNumber(errand.getErrandNumber())
			.withNamespace(namespace)
			.withPropertyDesignations(null)
			.build();
	}

	private String getExternalStatus(final StatusesEntity statuses) {
		return isNull(statuses.getSupportManagementStatus()) ? null : Optional.ofNullable(statuses.getExternalStatus()).orElse(DEFAULT_EXTERNAL_STATUS);
	}
}
