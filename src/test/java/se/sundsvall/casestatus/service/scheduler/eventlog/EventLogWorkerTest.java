package se.sundsvall.casestatus.service.scheduler.eventlog;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casestatus.util.Constants.EXTERNAL_CHANNEL_E_SERVICE;

import generated.client.oep_integrator.CaseStatusChangeRequest;
import generated.client.oep_integrator.InstanceType;
import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.sundsvall.casestatus.Application;
import se.sundsvall.casestatus.integration.db.CaseManagementOpeneViewRepository;
import se.sundsvall.casestatus.integration.db.model.ExecutionInformationEntity;
import se.sundsvall.casestatus.integration.db.model.views.CaseManagementOpeneView;
import se.sundsvall.casestatus.integration.eventlog.EventlogClient;
import se.sundsvall.casestatus.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casestatus.service.SupportManagementService;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventLogWorkerTest {

	@MockitoBean
	private CaseManagementOpeneViewRepository caseManagementOpeneViewRepositoryMock;

	@MockitoBean
	private EventlogClient eventlogClientMock;

	@MockitoBean
	private SupportManagementService supportManagementServiceMock;

	@MockitoBean
	private OepIntegratorClient oepIntegratorClientMock;

	@Mock
	private Page<Event> eventPageMock;

	@Captor
	private ArgumentCaptor<String> filterArgumentCaptor;

	@Autowired
	private EventLogWorker eventLogWorker;

	@Mock
	private Consumer<String> consumerMock;

	@Test
	void testUpdateStatus() {
		// Arrange
		final String municipalityId = "testMunicipalityId";
		final var internalStatus = "SomeInternalStatus";
		final var logkey = "1";
		final var logkey2 = "2";
		final var namespaces = List.of("namespace");
		final var errand = new Errand()
			.status(internalStatus)
			.channel(EXTERNAL_CHANNEL_E_SERVICE)
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
			.addExternalTagsItem(new ExternalTag().key("caseId").value(logkey));

		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
		final var caseMapping = CaseManagementOpeneView.builder().withCaseManagementId(internalStatus).withOpenEId("someOpenEStatus").build();

		when(eventPageMock.getContent()).thenReturn(List.of(new Event().logKey(logkey), new Event().logKey(logkey2)));
		when(eventPageMock.hasNext()).thenReturn(false);
		when(caseManagementOpeneViewRepositoryMock.findByCaseManagementId(internalStatus)).thenReturn(Optional.of(caseMapping));

		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), filterArgumentCaptor.capture())).thenReturn(eventPageMock);
		when(supportManagementServiceMock.getSupportManagementNamespaces()).thenReturn(namespaces);
		when(supportManagementServiceMock.getSupportManagementCaseById(eq(municipalityId), any(), anyString())).thenReturn(errand);
		// Act
		eventLogWorker.updateStatus(executionInformationEntity, consumerMock);

		// Assert
		verify(eventlogClientMock).getEvents(eq(municipalityId), any(PageRequest.class), anyString());
		verify(supportManagementServiceMock, times(2)).getSupportManagementCaseById(eq(municipalityId), same(namespaces), anyString());
		verify(oepIntegratorClientMock, times(2)).setStatus(anyString(), eq(InstanceType.EXTERNAL), any(), any(CaseStatusChangeRequest.class));
		verify(caseManagementOpeneViewRepositoryMock, times(2)).findByCaseManagementId(internalStatus);
		assertThat(filterArgumentCaptor.getValue()).isEqualTo("message:'Ärendet har uppdaterats.' and created > '" + executionInformationEntity.getLastSuccessfulExecution().minus(Duration.parse("PT5S")) +
			"' and sourceType: 'Errand' and owner: 'SupportManagement' and type: 'UPDATE'");
	}

	@Test
	void testUpdateStatusWithEmptyLogKeys() {
		// Arrange
		final String municipalityId = "testMunicipalityId";
		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();

		when(eventPageMock.getContent()).thenReturn(emptyList());
		when(eventPageMock.hasNext()).thenReturn(false);
		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), anyString())).thenReturn(eventPageMock);

		// Act
		eventLogWorker.updateStatus(executionInformationEntity, consumerMock);

		// Assert
		verify(eventlogClientMock).getEvents(eq(municipalityId), any(PageRequest.class), filterArgumentCaptor.capture());
		verifyNoInteractions(supportManagementServiceMock, oepIntegratorClientMock);
		assertThat(filterArgumentCaptor.getValue()).isEqualTo("message:'Ärendet har uppdaterats.' and created > '" + executionInformationEntity.getLastSuccessfulExecution().minus(Duration.parse("PT5S")) +
			"' and sourceType: 'Errand' and owner: 'SupportManagement' and type: 'UPDATE'");
	}

}
