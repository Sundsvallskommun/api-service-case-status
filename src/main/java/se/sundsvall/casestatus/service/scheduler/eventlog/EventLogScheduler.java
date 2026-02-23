package se.sundsvall.casestatus.service.scheduler.eventlog;

import java.time.OffsetDateTime;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.integration.db.ExecutionInformationRepository;
import se.sundsvall.casestatus.integration.db.model.ExecutionInformationEntity;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;

import static se.sundsvall.casestatus.util.Constants.CASE_DATA;
import static se.sundsvall.casestatus.util.Constants.CASE_DATA_JOB_NAME;
import static se.sundsvall.casestatus.util.Constants.CASE_MANAGEMENT;
import static se.sundsvall.casestatus.util.Constants.CASE_MANAGEMENT_JOB_NAME;
import static se.sundsvall.casestatus.util.Constants.SUPPORT_MANAGEMENT;
import static se.sundsvall.casestatus.util.Constants.SUPPORT_MANAGEMENT_JOB_NAME;

@Component
public class EventLogScheduler {

	private final ExecutionInformationRepository executionInformationRepository;
	private final EventLogWorker eventLogWorker;
	private final Dept44HealthUtility dept44HealthUtility;
	private final String municipalityId;

	public EventLogScheduler(final ExecutionInformationRepository executionInformationRepository, final EventLogWorker eventLogWorker, final Dept44HealthUtility dept44HealthUtility,
		@Value("${scheduler.eventlog.municipalityId}") final String municipalityId) {
		this.dept44HealthUtility = dept44HealthUtility;
		this.executionInformationRepository = executionInformationRepository;
		this.eventLogWorker = eventLogWorker;
		this.municipalityId = municipalityId;
	}

	@Dept44Scheduled(
		cron = "${scheduler.eventlog.case-management.cron}",
		name = CASE_MANAGEMENT_JOB_NAME,
		lockAtMostFor = "${scheduler.eventlog.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.eventlog.maximum-execution-time}")
	public void updateCaseManagementStatuses() {
		final var unhealthyConsumer = createUnhealtyConsumer(CASE_MANAGEMENT_JOB_NAME);

		final var startTime = OffsetDateTime.now();
		final var executionInformation = executionInformationRepository.findByMunicipalityIdAndServiceName(municipalityId, CASE_MANAGEMENT).orElse(initiateExecutionInfo(municipalityId, CASE_MANAGEMENT));

		final var success = eventLogWorker.updateOepCase(CASE_MANAGEMENT, executionInformation, unhealthyConsumer);

		if (success) {
			executionInformation.setLastSuccessfulExecution(startTime);
			executionInformationRepository.save(executionInformation);
		}
	}

	@Dept44Scheduled(
		cron = "${scheduler.eventlog.case-data.cron}",
		name = CASE_DATA_JOB_NAME,
		lockAtMostFor = "${scheduler.eventlog.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.eventlog.maximum-execution-time}")
	public void updateCaseDataStatuses() {
		final var unhealthyConsumer = createUnhealtyConsumer(CASE_DATA_JOB_NAME);

		final var startTime = OffsetDateTime.now();
		final var executionInformation = executionInformationRepository.findByMunicipalityIdAndServiceName(municipalityId, CASE_DATA).orElse(initiateExecutionInfo(municipalityId, CASE_DATA));

		final var success = eventLogWorker.updateOepCase(CASE_DATA, executionInformation, unhealthyConsumer);

		if (success) {
			executionInformation.setLastSuccessfulExecution(startTime);
			executionInformationRepository.save(executionInformation);
		}
	}

	@Dept44Scheduled(
		cron = "${scheduler.eventlog.support-management.cron}",
		name = SUPPORT_MANAGEMENT_JOB_NAME,
		lockAtMostFor = "${scheduler.eventlog.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.eventlog.maximum-execution-time}")
	public void updateSupportManagementStatuses() {
		final var unhealthyConsumer = createUnhealtyConsumer(SUPPORT_MANAGEMENT_JOB_NAME);

		final var startTime = OffsetDateTime.now();
		final var executionInformation = executionInformationRepository.findByMunicipalityIdAndServiceName(municipalityId, SUPPORT_MANAGEMENT).orElse(initiateExecutionInfo(municipalityId, SUPPORT_MANAGEMENT));

		final var success = eventLogWorker.updateSupportManagementStatuses(executionInformation, unhealthyConsumer);

		if (success) {
			executionInformation.setLastSuccessfulExecution(startTime);
			executionInformationRepository.save(executionInformation);
		}
	}

	private ExecutionInformationEntity initiateExecutionInfo(final String municipalityId, final String serviceName) {
		return ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withServiceName(serviceName)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
	}

	private Consumer<String> createUnhealtyConsumer(String jobName) {
		return msg -> dept44HealthUtility.setHealthIndicatorUnhealthy(jobName, String.format("Eventlog error: %s", msg));
	}

}
