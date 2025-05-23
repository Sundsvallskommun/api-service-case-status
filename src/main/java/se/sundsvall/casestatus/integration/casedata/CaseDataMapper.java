package se.sundsvall.casestatus.integration.casedata;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static se.sundsvall.casestatus.util.Constants.MISSING;

import generated.se.sundsvall.casedata.Errand;
import generated.se.sundsvall.casedata.Status;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;

public final class CaseDataMapper {

	static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private CaseDataMapper() {}

	public static List<CaseStatusResponse> toCaseStatusResponses(final List<Errand> errands) {

		return Optional.ofNullable(errands).orElse(emptyList()).stream()
			.map(CaseDataMapper::toCaseStatusResponse)
			.toList();
	}

	public static CaseStatusResponse toCaseStatusResponse(final Errand errand) {
		final var latestStatus = errand.getStatuses().stream()
			.max(Comparator.comparing(Status::getCreated))
			.orElse(null);

		return CaseStatusResponse.builder()
			.withCaseId(String.valueOf(errand.getId()))
			.withCaseType(errand.getCaseType())
			.withStatus(Optional.ofNullable(latestStatus)
				.map(Status::getStatusType)
				.orElse(null))
			.withLastStatusChange(Optional.ofNullable(latestStatus)
				.map(Status::getCreated)
				.map(dateTime -> dateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
				.map(CaseDataMapper::getTimestamp)
				.orElse(null))
			.withFirstSubmitted(MISSING)
			.withSystem("CASE_DATA")
			.withExternalCaseId(errand.getExternalCaseId())
			.withErrandNumber(errand.getErrandNumber())
			.withNamespace(errand.getNamespace())
			.build();
	}

	/**
	 * Formats the timestamp to a predetermined format or returns 'Saknas' if the timestamp is null.
	 *
	 * @param  originalTimestamp The original timestamp.
	 * @return                   The formatted timestamp or 'Saknas' if the timestamp is null.
	 */
	static String getTimestamp(final LocalDateTime originalTimestamp) {
		return ofNullable(originalTimestamp)
			.map(DATE_TIME_FORMATTER::format)
			.orElse(MISSING);
	}
}
