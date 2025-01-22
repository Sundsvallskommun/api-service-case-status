package se.sundsvall.casestatus.service.scheduler.eventlog;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casestatus.utility.Constants.EXTERNAL_CHANNEL_E_SERVICE;

import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.opene.SetStatus;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import se.sundsvall.casestatus.integration.db.CaseManagementOpeneViewRepository;
import se.sundsvall.casestatus.integration.db.model.ExecutionInformationEntity;
import se.sundsvall.casestatus.integration.db.model.views.CaseManagementOpeneView;
import se.sundsvall.casestatus.integration.eventlog.EventlogClient;
import se.sundsvall.casestatus.integration.opene.soap.OpenECallbackIntegration;
import se.sundsvall.casestatus.service.SupportManagementService;

@ExtendWith(MockitoExtension.class)
class EventLogWorkerTest {

	@Mock
	private CaseManagementOpeneViewRepository caseManagementOpeneViewRepository;

	@Mock
	private EventlogClient eventlogClient;

	@Mock
	private SupportManagementService supportManagementService;

	@Mock
	private OpenECallbackIntegration openECallbackIntegration;

	@Mock
	private Page<Event> eventPage;

	@InjectMocks
	private EventLogWorker eventLogWorker;

	@Test
	void testUpdateStatus() {
		// Arrange
		final String municipalityId = "testMunicipalityId";
		final var internalStatus = "SomeInternalStatus";
		final List<Errand> errands = List.of(new Errand()
			.status(internalStatus)
			.channel(EXTERNAL_CHANNEL_E_SERVICE)
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
			.addExternalTagsItem(new ExternalTag().key("caseId").value("1")),
			new Errand()
				.status(internalStatus)
				.channel(EXTERNAL_CHANNEL_E_SERVICE)
				.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
				.addExternalTagsItem(new ExternalTag().key("caseId").value("2")));

		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
		final var caseMapping = CaseManagementOpeneView.builder().withCaseManagementId(internalStatus).withOpenEId("someOpenEStatus").build();

		when(eventPage.getContent()).thenReturn(List.of(new Event(), new Event()));
		when(eventPage.hasNext()).thenReturn(false);
		when(caseManagementOpeneViewRepository.findByCaseManagementId(internalStatus)).thenReturn(Optional.of(caseMapping));

		when(eventlogClient.getEvents(eq(municipalityId), any(PageRequest.class), anyString())).thenReturn(eventPage);
		when(supportManagementService.getSupportManagementCases(eq(municipalityId), anyString())).thenReturn(errands);

		// Act
		eventLogWorker.updateStatus(executionInformationEntity);

		// Assert
		verify(eventlogClient, times(1)).getEvents(eq(municipalityId), any(PageRequest.class), anyString());
		verify(supportManagementService, times(1)).getSupportManagementCases(eq(municipalityId), anyString());
		verify(openECallbackIntegration, times(2)).setStatus(anyString(), any(SetStatus.class));
		verify(caseManagementOpeneViewRepository, times(2)).findByCaseManagementId(internalStatus);
	}

	@Test
	void testUpdateStatusWithEmptyLogKeys() {
		// Arrange
		final String municipalityId = "testMunicipalityId";
		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();

		when(eventPage.getContent()).thenReturn(emptyList());
		when(eventPage.hasNext()).thenReturn(false);
		when(eventlogClient.getEvents(eq(municipalityId), any(PageRequest.class), anyString())).thenReturn(eventPage);

		// Act
		eventLogWorker.updateStatus(executionInformationEntity);

		// Assert
		verify(eventlogClient).getEvents(eq(municipalityId), any(PageRequest.class), anyString());
		verifyNoInteractions(supportManagementService, openECallbackIntegration);
	}

}
