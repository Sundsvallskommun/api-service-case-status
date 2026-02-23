package se.sundsvall.casestatus.service.scheduler.eventlog;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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

@ExtendWith(MockitoExtension.class)
class EventLogSchedulerTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private ExecutionInformationRepository executionInformationRepository;

	@Mock
	private EventLogWorker eventLogWorker;

	@Mock
	private Dept44HealthUtility dept44HealthUtility;

	private EventLogScheduler eventLogScheduler;

	@BeforeEach
	void setUp() {
		eventLogScheduler = new EventLogScheduler(executionInformationRepository, eventLogWorker, dept44HealthUtility, MUNICIPALITY_ID);
	}

	@Test
	void testUpdateSupportManagementStatusesSuccess() {
		// Arrange
		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
		when(executionInformationRepository.findByMunicipalityIdAndServiceName(MUNICIPALITY_ID, "SupportManagement")).thenReturn(Optional.of(executionInformationEntity));
		when(eventLogWorker.updateSupportManagementStatuses(eq(executionInformationEntity), any())).thenReturn(true);

		// Act
		eventLogScheduler.updateSupportManagementStatuses();

		// Assert
		verify(eventLogWorker).updateSupportManagementStatuses(eq(executionInformationEntity), any());
		verify(executionInformationRepository).save(executionInformationEntity);
	}

	@Test
	void testUpdateSupportManagementStatusesFailure() {
		// Arrange
		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
		when(executionInformationRepository.findByMunicipalityIdAndServiceName(MUNICIPALITY_ID, "SupportManagement")).thenReturn(Optional.of(executionInformationEntity));
		when(eventLogWorker.updateSupportManagementStatuses(eq(executionInformationEntity), any())).thenReturn(false);

		// Act
		eventLogScheduler.updateSupportManagementStatuses();

		// Assert
		verify(eventLogWorker).updateSupportManagementStatuses(eq(executionInformationEntity), any());
		verify(executionInformationRepository).findByMunicipalityIdAndServiceName(MUNICIPALITY_ID, "SupportManagement");
		verifyNoMoreInteractions(executionInformationRepository);
	}
}
