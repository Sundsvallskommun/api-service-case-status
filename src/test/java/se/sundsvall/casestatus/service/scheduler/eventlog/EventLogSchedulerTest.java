package se.sundsvall.casestatus.service.scheduler.eventlog;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casestatus.integration.db.ExecutionInformationRepository;
import se.sundsvall.casestatus.integration.db.model.ExecutionInformationEntity;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventLogSchedulerTest {

	private static final String MUNICIPALITY_ID = "testMunicipalityId";
	private static final String JOB_NAME = "testJobName";

	@Mock
	private ExecutionInformationRepository executionInformationRepository;

	@Mock
	private EventLogWorker eventLogWorker;

	@Mock
	private Dept44HealthUtility dept44HealthUtility;

	private EventLogScheduler eventLogScheduler;

	@BeforeEach
	void setUp() {
		eventLogScheduler = new EventLogScheduler(
			executionInformationRepository,
			eventLogWorker,
			dept44HealthUtility,
			MUNICIPALITY_ID,
			JOB_NAME);
	}

	@Test
	void testUpdateStatusSuccess() {
		// Arrange
		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
		when(executionInformationRepository.findById(MUNICIPALITY_ID)).thenReturn(Optional.of(executionInformationEntity));
		when(eventLogWorker.updateStatus(eq(executionInformationEntity), any())).thenReturn(true);

		// Act
		eventLogScheduler.updateStatus();

		// Assert
		verify(eventLogWorker).updateStatus(eq(executionInformationEntity), any());
		verify(executionInformationRepository).save(executionInformationEntity);
	}

	@Test
	void testUpdateStatusFailure() {
		// Arrange
		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
		when(executionInformationRepository.findById(MUNICIPALITY_ID)).thenReturn(Optional.of(executionInformationEntity));
		when(eventLogWorker.updateStatus(eq(executionInformationEntity), any())).thenReturn(false);

		// Act
		eventLogScheduler.updateStatus();

		// Assert
		verify(eventLogWorker).updateStatus(eq(executionInformationEntity), any());
		verify(executionInformationRepository).findById(MUNICIPALITY_ID);
		verifyNoMoreInteractions(executionInformationRepository);
	}
}
