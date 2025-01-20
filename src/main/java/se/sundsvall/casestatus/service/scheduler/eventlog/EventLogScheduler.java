package se.sundsvall.casestatus.service.scheduler.eventlog;

import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.db.ExecutionInformationRepository;
import se.sundsvall.casestatus.integration.db.model.ExecutionInformationEntity;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

@Component
public class EventLogScheduler {

	private final ExecutionInformationRepository executionInformationRepository;

	private final EventLogWorker eventLogWorker;
	@Value("${scheduler.eventlog.municipalityId}")
	String municipalityId;

	public EventLogScheduler(final ExecutionInformationRepository executionInformationRepository, final EventLogWorker eventLogWorker) {
		this.executionInformationRepository = executionInformationRepository;
		this.eventLogWorker = eventLogWorker;
	}

	@Dept44Scheduled(
		cron = "${scheduler.eventlog.cron}",
		name = "${scheduler.eventlog.name}")
	public void updateStatus() {

		final var executionInformation = executionInformationRepository.findById(municipalityId).orElse(initiateExecutionInfo(municipalityId));

		eventLogWorker.updateStatus(executionInformation);
	}

	private ExecutionInformationEntity initiateExecutionInfo(final String municipalityId) {
		return ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
	}

}
