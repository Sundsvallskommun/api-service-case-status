package se.sundsvall.casestatus.service.mapper;

import static se.sundsvall.casestatus.util.Constants.DATE_TIME_FORMAT;
import static se.sundsvall.casestatus.util.Constants.MISSING;
import static se.sundsvall.casestatus.util.Constants.SUPPORT_MANAGEMENT;

import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.integration.db.SupportManagementStatusRepository;
import se.sundsvall.casestatus.integration.db.model.SupportManagementStatusEntity;

@Component
public class SupportManagementMapper {

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

	private final SupportManagementStatusRepository supportManagementStatusRepository;

	public SupportManagementMapper(final SupportManagementStatusRepository supportManagementStatusRepository) {
		this.supportManagementStatusRepository = supportManagementStatusRepository;
	}

	public static Optional<String> getExternalCaseId(final Errand errand) {
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

	public CaseStatusResponse toCaseStatusResponse(final Errand errand, final String namespace) {
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
			.withCaseType(errand.getTitle())
			.withStatus(getStatus(errand.getStatus()))
			.withLastStatusChange(modified)
			.withFirstSubmitted(firstSubmitted)
			.withSystem(SUPPORT_MANAGEMENT)
			.withErrandNumber(errand.getErrandNumber())
			.withNamespace(namespace)
			.build();
	}

	public String getStatus(final String systemStatus) {
		return supportManagementStatusRepository.findBySystemStatus(systemStatus)
			.map(SupportManagementStatusEntity::getGenericStatus)
			.orElse(systemStatus);
	}

}
