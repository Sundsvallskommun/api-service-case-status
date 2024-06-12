package se.sundsvall.casestatus.util.casestatuscache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.casestatus.util.casestatuscache.domain.FamilyId;

@SpringBootTest(properties = {
	"cache.scheduled.cron=* * * * * *", // Setup to execute every second
	"spring.flyway.enabled=true",
	"integration.db.case-status.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver",
	"integration.db.case-status.url=jdbc:tc:mariadb:10.6:////ms-casestatus",
	"server.shutdown=immediate",
	"spring.lifecycle.timeout-per-shutdown-phase=0s"
})
@ActiveProfiles("junit")
class CaseStatusCacheScheduledTest {

	@TestConfiguration
	public static class CaseStatusCacheWorkerConfiguration {
		@Bean
		@Primary
		public CaseStatusCacheWorker createMock() {
			return Mockito.mock(CaseStatusCacheWorker.class);
		}
	}

	@Autowired
	private CaseStatusCacheWorker caseStatusCacheWorkerMock;

	@Autowired
	@Qualifier("integrationDbCaseStatusJdbcTemplate")
	private NamedParameterJdbcTemplate jdbcTemplate;

	private LocalDateTime mockCalledTime;

	@Test
	void verifyShedLock() {

		// Let mock hang
		doAnswer(invocation -> {
			mockCalledTime = LocalDateTime.now();
			await()
				.forever()
				.until(() -> false);
			return null;
		}).when(caseStatusCacheWorkerMock).cacheStatusesForFamilyID(any(FamilyId.class));

		// Make sure scheduling occurs multiple times
		await().until(() -> mockCalledTime != null && LocalDateTime.now().isAfter(mockCalledTime.plusSeconds(2)));

		// Verify lock
		await()
			.atMost(5, TimeUnit.SECONDS)
			.untilAsserted(() ->
				assertThat(getLockedAt("cache_job"))
					.isCloseTo(LocalDateTime.now(Clock.systemUTC()), within(10, ChronoUnit.SECONDS)));

		// Only one call should be made as long as scheduledCacheJob() is locked and mock is waiting
		// for first call to finish
		verify(caseStatusCacheWorkerMock, times(1)).cacheStatusesForFamilyID(any());

	}

	private LocalDateTime getLockedAt(final String name) {
		return jdbcTemplate.query(
			"SELECT locked_at FROM shedlock WHERE name = :name",
			Map.of("name", name),
			this::mapTimestamp);
	}

	private LocalDateTime mapTimestamp(final ResultSet rs) throws SQLException {
		if (rs.next()) {
			return LocalDateTime.parse(rs.getString("locked_at"), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
		}
		return null;
	}

}
