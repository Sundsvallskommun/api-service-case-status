package se.sundsvall.casestatus.integration.casedata;

import generated.se.sundsvall.casedata.Address;
import generated.se.sundsvall.casedata.Errand;
import generated.se.sundsvall.casedata.Facility;
import generated.se.sundsvall.casedata.Status;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.service.StatusVocabulary;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

@Component
public class CaseDataMapper {

	static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final StatusVocabulary statusVocabulary;

	public CaseDataMapper(final StatusVocabulary statusVocabulary) {
		this.statusVocabulary = statusVocabulary;
	}

	public List<CaseStatusResponse> toCaseStatusResponses(final List<Errand> errands) {
		return Optional.ofNullable(errands).orElse(emptyList()).stream()
			.map(this::toCaseStatusResponse)
			.toList();
	}

	public CaseStatusResponse toCaseStatusResponse(final Errand errand) {
		final var latestStatus = errand.getStatuses().stream()
			.max(Comparator.comparing(Status::getCreated))
			.orElse(null);

		final var status = Optional.ofNullable(latestStatus)
			.map(Status::getStatusType)
			.orElse(null);

		return CaseStatusResponse.builder()
			.withCaseId(String.valueOf(errand.getId()))
			.withCaseType(errand.getCaseType())
			.withStatus(status)
			.withExternalStatus(statusVocabulary.translateCaseManagementStatus(status))
			.withLastStatusChange(Optional.ofNullable(latestStatus)
				.map(Status::getCreated)
				.map(dateTime -> dateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
				.map(CaseDataMapper::getTimestamp)
				.orElse(null))
			.withFirstSubmitted(getTimestamp(errand.getCreated()))
			.withSystem("CASE_DATA")
			.withExternalCaseId(errand.getExternalCaseId())
			.withErrandNumber(errand.getErrandNumber())
			.withNamespace(errand.getNamespace())
			.withPropertyDesignations(Optional.ofNullable(errand.getFacilities()).orElse(emptyList()).stream()
				.map(Facility::getAddress)
				.filter(Objects::nonNull)
				.map(Address::getPropertyDesignation)
				.toList())
			.build();
	}

	/**
	 * Formats the timestamp to a predetermined format or returns null if the timestamp is null.
	 */
	static String getTimestamp(final LocalDateTime originalTimestamp) {
		return ofNullable(originalTimestamp)
			.map(DATE_TIME_FORMATTER::format)
			.orElse(null);
	}

	/**
	 * Formats the timestamp to a predetermined format or returns null if the timestamp is null.
	 */
	static String getTimestamp(final OffsetDateTime originalTimestamp) {
		return ofNullable(originalTimestamp)
			.map(DATE_TIME_FORMATTER::format)
			.orElse(null);
	}
}
