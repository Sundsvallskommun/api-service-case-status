package se.sundsvall.casestatus.service.scheduler.eventlog;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.casestatus.integration.db.model.ExecutionInformationEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static se.sundsvall.casestatus.util.Constants.CASE_MANAGEMENT_JOB_NAME;

@SpringBootTest(properties = {
	"scheduler.eventlog.case-management.cron=* * * * * *", // Setup to execute every second
	"scheduler.eventlog.name=eventlog",
	"server.shutdown=immediate",
	"spring.lifecycle.timeout-per-shutdown-phase=0s"
})
@ActiveProfiles("junit")
class EventLogSchedulerCaseManagementShedlockTest {

	@Autowired
	private EventLogWorker eventLogWorkerMock;

	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;

	private LocalDateTime mockCalledTime;

	@Test
	void verifyShedLockForCaseManagement() {

		// Let mock hang
		doAnswer(_ -> {
			mockCalledTime = LocalDateTime.now();
			await()
				.forever()
				.until(() -> false);
			return null;
		}).when(eventLogWorkerMock).updateOepCase(anyString(), any(ExecutionInformationEntity.class), any(Consumer.class));

		// Make sure scheduling occurs multiple times
		await().until(() -> mockCalledTime != null && LocalDateTime.now().isAfter(mockCalledTime.plusSeconds(2)));

		// Verify lock
		await()
			.atMost(5, TimeUnit.SECONDS)
			.untilAsserted(() -> assertThat(getLockedAt(CASE_MANAGEMENT_JOB_NAME))
				.isCloseTo(LocalDateTime.now(Clock.systemUTC()), within(10, ChronoUnit.SECONDS)));

		verify(eventLogWorkerMock, times(1)).updateOepCase(anyString(), any(ExecutionInformationEntity.class), any(Consumer.class));

	}

	private LocalDateTime getLockedAt(final String name) {
		return jdbcTemplate.query(
			"SELECT locked_at FROM shedlock WHERE name = :name",
			Map.of("name", name),
			this::mapTimestamp);
	}

	private LocalDateTime mapTimestamp(final ResultSet rs) throws SQLException {
		if (rs.next()) {
			return rs.getTimestamp("locked_at").toLocalDateTime();
		}
		return null;
	}

	@TestConfiguration
	public static class EventLogWorkerConfiguration {

		@Bean
		@Primary
		EventLogWorker createMock() {
			return Mockito.mock(EventLogWorker.class);
		}

	}

}
