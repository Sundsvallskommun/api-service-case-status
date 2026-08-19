package se.sundsvall.casestatus.integration.casedata;

import generated.se.sundsvall.casedata.Status;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casestatus.service.StatusVocabulary;

import static java.time.Month.MARCH;
import static java.time.Month.MAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static se.sundsvall.TestDataFactory.createCaseDataErrand;
import static se.sundsvall.casestatus.util.Constants.DEFAULT_EXTERNAL_STATUS;

@ExtendWith(MockitoExtension.class)
class CaseDataMapperTest {

	@Mock
	private StatusVocabulary statusVocabulary;

	@InjectMocks
	private CaseDataMapper caseDataMapper;

	private static Stream<Arguments> getTimestampArgumentProvider() {
		return Stream.of(
			Arguments.of(LocalDateTime.of(2025, MARCH, 4, 0, 7, 12), "2025-03-04 00:07"),
			Arguments.of(LocalDateTime.of(2026, MAY, 12, 0, 8, 32), "2026-05-12 00:08"));
	}

	@Test
	void toCaseStatusResponses() {
		final var errand1 = createCaseDataErrand();
		final var errand2 = createCaseDataErrand();
		final var errands = List.of(errand1, errand2);
		when(statusVocabulary.translateCaseManagementStatus(any())).thenReturn(DEFAULT_EXTERNAL_STATUS);

		final var result = caseDataMapper.toCaseStatusResponses(errands);

		assertThat(result).hasSize(2).satisfies(e -> {
			assertThat(e.getFirst()).usingRecursiveComparison().isEqualTo(caseDataMapper.toCaseStatusResponse(errand1));
			assertThat(e.getLast()).usingRecursiveComparison().isEqualTo(caseDataMapper.toCaseStatusResponse(errand2));
		});
	}

	@Test
	void toCaseStatusResponse() {
		final var errand = createCaseDataErrand();
		final var latestStatus = errand.getStatuses().stream()
			.max(Comparator.comparing(Status::getCreated))
			.orElse(null);
		when(statusVocabulary.translateCaseManagementStatus(latestStatus.getStatusType())).thenReturn("externalStatus");

		final var result = caseDataMapper.toCaseStatusResponse(errand);

		assertThat(result.getCaseId()).isEqualTo(String.valueOf(errand.getId()));
		assertThat(result.getCaseType()).isEqualTo(errand.getCaseType());
		assertThat(result.getStatus()).isEqualTo(latestStatus.getStatusType());
		assertThat(result.getExternalStatus()).isEqualTo("externalStatus");
		assertThat(result.getLastStatusChange()).isEqualTo(CaseDataMapper.getTimestamp(latestStatus.getCreated().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()));
		assertThat(result.getFirstSubmitted()).isNull();
		assertThat(result.getSystem()).isEqualTo("CASE_DATA");
		assertThat(result.getExternalCaseId()).isEqualTo(errand.getExternalCaseId());
		assertThat(result.getErrandNumber()).isEqualTo(errand.getErrandNumber());
		assertThat(result.getNamespace()).isEqualTo(errand.getNamespace());
	}

	@ParameterizedTest
	@MethodSource("getTimestampArgumentProvider")
	void getTimestamp(final LocalDateTime timestamp, final String expected) {
		final var result = CaseDataMapper.getTimestamp(timestamp);
		assertThat(result).isEqualTo(expected);
	}

	@Test
	void toCaseStatusResponseWithoutStatuses() {
		final var errand = createCaseDataErrand().statuses(null).facilities(null);

		final var result = caseDataMapper.toCaseStatusResponse(errand);

		assertThat(result.getStatus()).isNull();
		assertThat(result.getLastStatusChange()).isNull();
		assertThat(result.getPropertyDesignations()).isEmpty();
	}

	@Test
	void toCaseStatusResponseWithStatusMissingCreated() {
		final var statusWithoutCreated = new Status().statusType("Ärende inkommit");
		final var latestStatus = new Status().statusType("Beslutad").created(OffsetDateTime.parse("2025-03-04T00:07:12Z"));
		final var errand = createCaseDataErrand().statuses(List.of(statusWithoutCreated, latestStatus));
		when(statusVocabulary.translateCaseManagementStatus("Beslutad")).thenReturn("externalStatus");

		final var result = caseDataMapper.toCaseStatusResponse(errand);

		assertThat(result.getStatus()).isEqualTo("Beslutad");
		assertThat(result.getExternalStatus()).isEqualTo("externalStatus");
	}

	@Test
	void toCaseStatusResponseWhenNoStatusHasCreated() {
		final var errand = createCaseDataErrand().statuses(List.of(new Status().statusType("Ärende inkommit")));
		when(statusVocabulary.translateCaseManagementStatus("Ärende inkommit")).thenReturn("externalStatus");

		final var result = caseDataMapper.toCaseStatusResponse(errand);

		assertThat(result.getStatus()).isEqualTo("Ärende inkommit");
		assertThat(result.getLastStatusChange()).isNull();
	}

	@Test
	void toCaseStatusResponsesWithNullInput() {
		assertThat(caseDataMapper.toCaseStatusResponses(null)).isEmpty();
	}
}
