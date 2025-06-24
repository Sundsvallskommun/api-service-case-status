package se.sundsvall.casestatus.service.scheduler.eventlog;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.sundsvall.casestatus.integration.db.ExecutionInformationRepository;
import se.sundsvall.casestatus.integration.db.model.ExecutionInformationEntity;

@ExtendWith(MockitoExtension.class)
class EventLogSchedulerTest {

	@Mock
	private ExecutionInformationRepository executionInformationRepository;

	@Mock
	private EventLogWorker eventLogWorker;

	@InjectMocks
	private EventLogScheduler eventLogScheduler;

	@Mock
	private Consumer<String> consumerMock;

	@Test
	void testUpdateStatus() {
		// Arrange
		final var municipalityId = "testMunicipalityId";
		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
		ReflectionTestUtils.setField(eventLogScheduler, "municipalityId", municipalityId);
		when(executionInformationRepository.findById(any())).thenReturn(Optional.of(executionInformationEntity));

		// Act
		eventLogScheduler.updateStatus();
		// Assert
		verify(eventLogWorker).updateStatus(eq(executionInformationEntity), any());
		verify(executionInformationRepository).save(executionInformationEntity);
	}
}
