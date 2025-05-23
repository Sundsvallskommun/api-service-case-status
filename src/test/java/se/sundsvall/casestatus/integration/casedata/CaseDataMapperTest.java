package se.sundsvall.casestatus.integration.casedata;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.TestDataFactory.createCaseDataErrand;
import static se.sundsvall.casestatus.util.Constants.MISSING;

import generated.se.sundsvall.casedata.Status;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CaseDataMapperTest {

	@Test
	void toCaseStatusResponses() {
		var errand1 = createCaseDataErrand();
		var errand2 = createCaseDataErrand();
		var errands = List.of(errand1, errand2);

		var result = CaseDataMapper.toCaseStatusResponses(errands);

		assertThat(result).hasSize(2).satisfies(e -> {
			assertThat(e.getFirst()).usingRecursiveComparison().isEqualTo(CaseDataMapper.toCaseStatusResponse(errand1));
			assertThat(e.getLast()).usingRecursiveComparison().isEqualTo(CaseDataMapper.toCaseStatusResponse(errand2));
		});
	}

	@Test
	void toCaseStatusResponse() {
		var errand = createCaseDataErrand();
		var latestStatus = errand.getStatuses().stream()
			.max(Comparator.comparing(Status::getCreated))
			.orElse(null);

		var result = CaseDataMapper.toCaseStatusResponse(errand);

		assertThat(result.getCaseId()).isEqualTo(String.valueOf(errand.getId()));
		assertThat(result.getCaseType()).isEqualTo(errand.getCaseType());
		assertThat(result.getStatus()).isEqualTo(latestStatus.getStatusType());
		assertThat(result.getLastStatusChange()).isEqualTo(CaseDataMapper.getTimestamp(latestStatus.getCreated().atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()));
		assertThat(result.getFirstSubmitted()).isEqualTo(MISSING);
		assertThat(result.getSystem()).isEqualTo("CASE_DATA");
		assertThat(result.getExternalCaseId()).isEqualTo(errand.getExternalCaseId());
		assertThat(result.getErrandNumber()).isEqualTo(errand.getErrandNumber());
		assertThat(result.getNamespace()).isEqualTo(errand.getNamespace());
	}

	@ParameterizedTest
	@MethodSource("getTimestampArgumentProvider")
	void getTimestamp(LocalDateTime timestamp, String expected) {
		final var result = CaseDataMapper.getTimestamp(timestamp);
		assertThat(result).isEqualTo(expected);
	}

	private static Stream<Arguments> getTimestampArgumentProvider() {
		return Stream.of(
			Arguments.of(LocalDateTime.of(2025, 3, 4, 0, 7, 12), "2025-03-04 00:07"),
			Arguments.of(LocalDateTime.of(2026, 5, 12, 0, 8, 32), "2026-05-12 00:08"));
	}

}
