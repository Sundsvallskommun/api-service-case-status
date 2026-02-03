package se.sundsvall.casestatus.service.scheduler.eventlog;

import java.time.OffsetDateTime;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.db.ExecutionInformationRepository;
import se.sundsvall.casestatus.integration.db.model.ExecutionInformationEntity;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;

@Component
public class EventLogScheduler {

	private final ExecutionInformationRepository executionInformationRepository;
	private final EventLogWorker eventLogWorker;
	private final Consumer<String> eventSetUnHealthyConsumer;
	private final String municipalityId;

	public EventLogScheduler(
		final ExecutionInformationRepository executionInformationRepository,
		final EventLogWorker eventLogWorker,
		final Dept44HealthUtility dept44HealthUtility,
		@Value("${scheduler.eventlog.municipalityId}") final String municipalityId,
		@Value("${scheduler.eventlog.name}") final String jobName) {

		this.executionInformationRepository = executionInformationRepository;
		this.eventLogWorker = eventLogWorker;
		this.municipalityId = municipalityId;
		this.eventSetUnHealthyConsumer = msg -> dept44HealthUtility.setHealthIndicatorUnhealthy(jobName, String.format("Eventlog error: %s", msg));
	}

	@Dept44Scheduled(
		cron = "${scheduler.eventlog.cron}",
		name = "${scheduler.eventlog.name}",
		lockAtMostFor = "${scheduler.eventlog.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.eventlog.maximum-execution-time}")
	public void updateStatus() {

		final var startTime = OffsetDateTime.now();
		final var executionInformation = executionInformationRepository.findById(municipalityId).orElse(initiateExecutionInfo(municipalityId));

		final var success = eventLogWorker.updateStatus(executionInformation, eventSetUnHealthyConsumer);

		if (success) {
			executionInformation.setLastSuccessfulExecution(startTime);
			executionInformationRepository.save(executionInformation);
		}
	}

	private ExecutionInformationEntity initiateExecutionInfo(final String municipalityId) {
		return ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
	}
}
