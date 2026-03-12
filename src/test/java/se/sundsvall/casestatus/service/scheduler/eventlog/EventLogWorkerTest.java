package se.sundsvall.casestatus.service.scheduler.eventlog;

import generated.client.oep_integrator.CaseStatusChangeRequest;
import generated.client.oep_integrator.InstanceType;
import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.eventlog.Metadata;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import se.sundsvall.casestatus.Application;
import se.sundsvall.casestatus.integration.db.StatusesRepository;
import se.sundsvall.casestatus.integration.db.model.ExecutionInformationEntity;
import se.sundsvall.casestatus.integration.db.model.StatusesEntity;
import se.sundsvall.casestatus.integration.eventlog.EventlogClient;
import se.sundsvall.casestatus.integration.messaging.MessagingIntegration;
import se.sundsvall.casestatus.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casestatus.service.SupportManagementService;
import se.sundsvall.casestatus.service.util.EnvironmentUtil;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casestatus.util.Constants.CASE_DATA;
import static se.sundsvall.casestatus.util.Constants.CASE_MANAGEMENT;
import static se.sundsvall.casestatus.util.Constants.EXTERNAL_CHANNEL_E_SERVICE;
import static se.sundsvall.casestatus.util.Constants.INTERNAL_CHANNEL_E_SERVICE;

@ActiveProfiles("junit")
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventLogWorkerTest {

	@MockitoBean
	private StatusesRepository statusesRepositoryMock;

	@MockitoBean
	private EventlogClient eventlogClientMock;

	@MockitoBean
	private SupportManagementService supportManagementServiceMock;

	@MockitoBean
	private OepIntegratorClient oepIntegratorClientMock;

	@MockitoBean
	private MessagingIntegration messagingIntegrationMock;

	@MockitoBean
	private EnvironmentUtil environmentUtilMock;

	@Mock
	private Page<Event> eventPageMock;

	@Captor
	private ArgumentCaptor<String> filterArgumentCaptor;

	@Autowired
	private EventLogWorker eventLogWorker;

	@Mock
	private Consumer<String> consumerMock;

	@Test
	void updateSupportManagementStatuses() {
		final String municipalityId = "testMunicipalityId";
		final var internalStatus = "SomeInternalStatus";
		final var logkey = "1";
		final var logkey2 = "2";
		final var namespace = "namespace";
		final var errand = new Errand()
			.status(internalStatus)
			.channel(EXTERNAL_CHANNEL_E_SERVICE)
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
			.addExternalTagsItem(new ExternalTag().key("caseId").value(logkey));

		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
		final var statuses = StatusesEntity.builder().withOepStatus("NewOepStatus").build();

		when(eventPageMock.getContent()).thenReturn(List.of(new Event().logKey(logkey).metadata(List.of(new Metadata().key("Namespace").value(namespace))), new Event().logKey(logkey2).metadata(List.of(new Metadata().key("Namespace").value(namespace)))));
		when(eventPageMock.hasNext()).thenReturn(false);
		when(statusesRepositoryMock.findBySupportManagementStatus(internalStatus)).thenReturn(Optional.of(statuses));

		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), filterArgumentCaptor.capture())).thenReturn(eventPageMock);
		when(supportManagementServiceMock.getSupportManagementCaseById(eq(municipalityId), any(), anyString())).thenReturn(errand);

		final var result = eventLogWorker.updateSupportManagementStatuses(executionInformationEntity, consumerMock);

		assertThat(result).isTrue();
		verify(eventlogClientMock).getEvents(eq(municipalityId), any(PageRequest.class), anyString());
		verify(supportManagementServiceMock, times(2)).getSupportManagementCaseById(eq(municipalityId), same(namespace), anyString());
		verify(oepIntegratorClientMock, times(2)).setStatus(anyString(), eq(InstanceType.EXTERNAL), any(), any(CaseStatusChangeRequest.class));
		verify(statusesRepositoryMock, times(2)).findBySupportManagementStatus(internalStatus);
		assertThat(filterArgumentCaptor.getValue()).isEqualTo("message:'Ärendet har uppdaterats.' and created > '" + executionInformationEntity.getLastSuccessfulExecution().minus(Duration.parse("PT5S")) +
			"' and sourceType: 'Errand' and owner: 'SupportManagement' and type: 'UPDATE'");
	}

	@Test
	void testUpdateSupportManagementStatusesWithEmptyLogKeys() {
		final String municipalityId = "testMunicipalityId";
		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();

		when(eventPageMock.getContent()).thenReturn(emptyList());
		when(eventPageMock.hasNext()).thenReturn(false);
		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), anyString())).thenReturn(eventPageMock);

		final var result = eventLogWorker.updateSupportManagementStatuses(executionInformationEntity, consumerMock);

		assertThat(result).isTrue();
		verify(eventlogClientMock).getEvents(eq(municipalityId), any(PageRequest.class), filterArgumentCaptor.capture());
		verifyNoInteractions(supportManagementServiceMock, oepIntegratorClientMock);
		assertThat(filterArgumentCaptor.getValue()).isEqualTo("message:'Ärendet har uppdaterats.' and created > '" + executionInformationEntity.getLastSuccessfulExecution().minus(Duration.parse("PT5S")) +
			"' and sourceType: 'Errand' and owner: 'SupportManagement' and type: 'UPDATE'");
	}

	@Test
	void updateSupportManagementStatusesWithoutFamilyId() {
		final String municipalityId = "testMunicipalityId";
		final var internalStatus = "SomeInternalStatus";
		final var logkey = "1";
		final var namespace = "namespace";
		final var errand = new Errand()
			.status(internalStatus)
			.channel(EXTERNAL_CHANNEL_E_SERVICE)
			.addExternalTagsItem(new ExternalTag().key("caseId").value(logkey));

		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();

		when(eventPageMock.getContent()).thenReturn(List.of(new Event().logKey(logkey).metadata(List.of(new generated.se.sundsvall.eventlog.Metadata().key("Namespace").value(namespace)))));
		when(eventPageMock.hasNext()).thenReturn(false);
		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), anyString())).thenReturn(eventPageMock);
		when(supportManagementServiceMock.getSupportManagementCaseById(eq(municipalityId), any(), anyString())).thenReturn(errand);

		final var result = eventLogWorker.updateSupportManagementStatuses(executionInformationEntity, consumerMock);

		assertThat(result).isTrue();
		verify(eventlogClientMock).getEvents(eq(municipalityId), any(PageRequest.class), anyString());
		verify(supportManagementServiceMock).getSupportManagementCaseById(eq(municipalityId), same(namespace), anyString());
		verifyNoInteractions(oepIntegratorClientMock);
	}

	@Test
	void updateStatusWithMismatchingSupportManagementStatuses() {
		final String municipalityId = "testMunicipalityId";
		final var internalStatus = "SomeInternalStatus";
		final var logkey = "1";
		final var namespace = "namespace";
		final var errand = new Errand()
			.status(internalStatus)
			.channel(EXTERNAL_CHANNEL_E_SERVICE)
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
			.addExternalTagsItem(new ExternalTag().key("caseId").value(logkey));

		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();

		when(eventPageMock.getContent()).thenReturn(List.of(new Event().logKey(logkey).metadata(List.of(new Metadata().key("Namespace").value(namespace)))));
		when(eventPageMock.hasNext()).thenReturn(false);
		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), anyString())).thenReturn(eventPageMock);
		when(supportManagementServiceMock.getSupportManagementCaseById(eq(municipalityId), any(), anyString())).thenReturn(errand);
		when(statusesRepositoryMock.findBySupportManagementStatus(internalStatus)).thenReturn(Optional.empty());

		final var result = eventLogWorker.updateSupportManagementStatuses(executionInformationEntity, consumerMock);

		assertThat(result).isFalse();
		verify(eventlogClientMock).getEvents(eq(municipalityId), any(PageRequest.class), anyString());
		verify(supportManagementServiceMock).getSupportManagementCaseById(eq(municipalityId), eq(namespace), anyString());
		verifyNoInteractions(oepIntegratorClientMock);
		verify(consumerMock).accept(anyString());
	}

	@Test
	void updateSupportManagementStatusesWithMissingNamespace() {
		final String municipalityId = "testMunicipalityId";
		final var logkey = "1";

		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();

		// Event without Namespace metadata
		when(eventPageMock.getContent()).thenReturn(List.of(new Event().logKey(logkey).metadata(List.of(new Metadata().key("OtherKey").value("otherValue")))));
		when(eventPageMock.hasNext()).thenReturn(false);
		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), anyString())).thenReturn(eventPageMock);

		final var result = eventLogWorker.updateSupportManagementStatuses(executionInformationEntity, consumerMock);

		assertThat(result).isTrue();
		verify(eventlogClientMock).getEvents(eq(municipalityId), any(PageRequest.class), anyString());
		verifyNoInteractions(supportManagementServiceMock, oepIntegratorClientMock, statusesRepositoryMock);
	}

	@Test
	void updateSupportManagementStatusesWithOepIntegratorFailure() {
		final String municipalityId = "testMunicipalityId";
		final var internalStatus = "SomeInternalStatus";
		final var logkey = "1";
		final var namespace = "namespace";
		final var errand = new Errand()
			.status(internalStatus)
			.channel(EXTERNAL_CHANNEL_E_SERVICE)
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
			.addExternalTagsItem(new ExternalTag().key("caseId").value(logkey));

		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
		final var statuses = StatusesEntity.builder().withOepStatus("NewOepStatus").build();

		when(eventPageMock.getContent()).thenReturn(List.of(new Event().logKey(logkey).metadata(List.of(new Metadata().key("Namespace").value(namespace)))));
		when(eventPageMock.hasNext()).thenReturn(false);
		when(statusesRepositoryMock.findBySupportManagementStatus(internalStatus)).thenReturn(Optional.of(statuses));
		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), anyString())).thenReturn(eventPageMock);
		when(supportManagementServiceMock.getSupportManagementCaseById(eq(municipalityId), any(), anyString())).thenReturn(errand);
		when(oepIntegratorClientMock.setStatus(anyString(), any(), anyString(), any())).thenThrow(new RuntimeException("OEP error"));

		final var result = eventLogWorker.updateSupportManagementStatuses(executionInformationEntity, consumerMock);

		assertThat(result).isFalse();
		verify(oepIntegratorClientMock).setStatus(anyString(), eq(InstanceType.EXTERNAL), eq(logkey), any(CaseStatusChangeRequest.class));
		verify(consumerMock).accept(anyString());
	}

	@Test
	void updateOepCase_success() {
		final String municipalityId = "testMunicipalityId";
		final var statusValue = "SomeStatus";
		final var externalCaseId = "ext-123";
		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
		final var statuses = StatusesEntity.builder().withOepStatus("NewOepStatus").build();

		when(eventPageMock.getContent()).thenReturn(List.of(new Event().logKey("12345").metadata(List.of(
			new Metadata().key("Status").value(statusValue),
			new Metadata().key("ExternalCaseId").value(externalCaseId)))));
		when(eventPageMock.hasNext()).thenReturn(false);
		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), filterArgumentCaptor.capture())).thenReturn(eventPageMock);
		when(statusesRepositoryMock.findByCaseManagementStatus(statusValue)).thenReturn(Optional.of(statuses));

		final var result = eventLogWorker.updateOepCase(CASE_MANAGEMENT, executionInformationEntity, consumerMock);

		assertThat(result).isTrue();
		verify(oepIntegratorClientMock).setStatus(eq(municipalityId), eq(InstanceType.EXTERNAL), eq(externalCaseId), any(CaseStatusChangeRequest.class));
		verify(statusesRepositoryMock).findByCaseManagementStatus(statusValue);
		verifyNoInteractions(messagingIntegrationMock);
		assertThat(filterArgumentCaptor.getValue())
			.contains("message ~ 'Status updated to'")
			.contains("owner: 'CaseManagement'")
			.contains("sourceType: 'Errand'")
			.contains("type: 'UPDATE'")
			.contains("created > '" + executionInformationEntity.getLastSuccessfulExecution().minus(Duration.parse("PT5S")));
	}

	@Test
	void updateOepCase_noEvents() {
		final String municipalityId = "testMunicipalityId";
		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();

		when(eventPageMock.getContent()).thenReturn(emptyList());
		when(eventPageMock.hasNext()).thenReturn(false);
		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), anyString())).thenReturn(eventPageMock);

		final var result = eventLogWorker.updateOepCase(CASE_MANAGEMENT, executionInformationEntity, consumerMock);

		assertThat(result).isTrue();
		verify(eventlogClientMock).getEvents(eq(municipalityId), any(PageRequest.class), anyString());
		verifyNoInteractions(statusesRepositoryMock, oepIntegratorClientMock, messagingIntegrationMock);
	}

	@Test
	void updateOepCase_oepIntegratorFailure() {
		final String municipalityId = "testMunicipalityId";
		final var statusValue = "SomeStatus";
		final var externalCaseId = "ext-123";
		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
		final var statuses = StatusesEntity.builder().withOepStatus("NewOepStatus").build();

		when(eventPageMock.getContent()).thenReturn(List.of(new Event().logKey("12345").metadata(List.of(
			new Metadata().key("Status").value(statusValue),
			new Metadata().key("ExternalCaseId").value(externalCaseId)))));
		when(eventPageMock.hasNext()).thenReturn(false);
		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), anyString())).thenReturn(eventPageMock);
		when(statusesRepositoryMock.findByCaseManagementStatus(statusValue)).thenReturn(Optional.of(statuses));
		when(oepIntegratorClientMock.setStatus(anyString(), any(), anyString(), any())).thenThrow(new RuntimeException("OEP error"));
		when(environmentUtilMock.extractEnvironment()).thenReturn("junit");

		final var result = eventLogWorker.updateOepCase(CASE_MANAGEMENT, executionInformationEntity, consumerMock);

		assertThat(result).isTrue();
		verify(oepIntegratorClientMock).setStatus(eq(municipalityId), eq(InstanceType.EXTERNAL), eq(externalCaseId), any(CaseStatusChangeRequest.class));
		verify(messagingIntegrationMock).sendSlackMessage(eq(municipalityId), contains(externalCaseId));
		verify(consumerMock).accept("Failed to update openE status for case with external ID " + externalCaseId);
	}

	@Test
	void updateOepCase_statusNotFoundSkipsEvent() {
		final String municipalityId = "testMunicipalityId";
		final var statusValue = "UnknownStatus";
		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();

		when(eventPageMock.getContent()).thenReturn(List.of(new Event().logKey("12345").metadata(List.of(
			new Metadata().key("Status").value(statusValue),
			new Metadata().key("ExternalCaseId").value("ext-123")))));
		when(eventPageMock.hasNext()).thenReturn(false);
		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), anyString())).thenReturn(eventPageMock);
		when(statusesRepositoryMock.findByCaseManagementStatus(statusValue)).thenReturn(Optional.empty());

		final var result = eventLogWorker.updateOepCase(CASE_MANAGEMENT, executionInformationEntity, consumerMock);

		assertThat(result).isTrue();
		verify(statusesRepositoryMock).findByCaseManagementStatus(statusValue);
		verifyNoInteractions(oepIntegratorClientMock, messagingIntegrationMock);
	}

	@Test
	void updateOepCase_nullExternalCaseIdSkipsEvent() {
		final String municipalityId = "testMunicipalityId";
		final var statusValue = "SomeStatus";
		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
		final var statuses = StatusesEntity.builder().withOepStatus("NewOepStatus").build();

		when(eventPageMock.getContent()).thenReturn(List.of(new Event().logKey("12345").metadata(List.of(
			new Metadata().key("Status").value(statusValue)))));
		when(eventPageMock.hasNext()).thenReturn(false);
		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), anyString())).thenReturn(eventPageMock);
		when(statusesRepositoryMock.findByCaseManagementStatus(statusValue)).thenReturn(Optional.of(statuses));

		final var result = eventLogWorker.updateOepCase(CASE_MANAGEMENT, executionInformationEntity, consumerMock);

		assertThat(result).isTrue();
		verify(statusesRepositoryMock).findByCaseManagementStatus(statusValue);
		verifyNoInteractions(oepIntegratorClientMock, messagingIntegrationMock);
	}

	@Test
	void updateOepCase_multiplePages() {
		final String municipalityId = "testMunicipalityId";
		final var statusValue = "SomeStatus";
		final var externalCaseId = "ext-123";
		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
		final var statuses = StatusesEntity.builder().withOepStatus("NewOepStatus").build();

		@SuppressWarnings("unchecked")
		final Page<Event> secondPageMock = org.mockito.Mockito.mock(Page.class);

		when(eventPageMock.getContent()).thenReturn(List.of(new Event().logKey("12345").metadata(List.of(
			new Metadata().key("Status").value(statusValue),
			new Metadata().key("ExternalCaseId").value(externalCaseId)))));
		when(eventPageMock.hasNext()).thenReturn(true);

		when(secondPageMock.getContent()).thenReturn(emptyList());
		when(secondPageMock.hasNext()).thenReturn(false);

		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), anyString()))
			.thenReturn(eventPageMock)
			.thenReturn(secondPageMock);
		when(statusesRepositoryMock.findByCaseManagementStatus(statusValue)).thenReturn(Optional.of(statuses));

		final var result = eventLogWorker.updateOepCase(CASE_DATA, executionInformationEntity, consumerMock);

		assertThat(result).isTrue();
		verify(eventlogClientMock, times(2)).getEvents(eq(municipalityId), any(PageRequest.class), anyString());
		verify(oepIntegratorClientMock).setStatus(eq(municipalityId), eq(InstanceType.EXTERNAL), eq(externalCaseId), any(CaseStatusChangeRequest.class));
		verifyNoInteractions(messagingIntegrationMock);
	}

	@Test
	void updateOepCase_multipleEventsAllProcessed() {
		final String municipalityId = "testMunicipalityId";
		final var statusValue1 = "Status1";
		final var statusValue2 = "Status2";
		final var externalCaseId1 = "ext-1";
		final var externalCaseId2 = "ext-2";
		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
		final var statuses1 = StatusesEntity.builder().withOepStatus("OepStatus1").build();
		final var statuses2 = StatusesEntity.builder().withOepStatus("OepStatus2").build();

		when(eventPageMock.getContent()).thenReturn(List.of(
			new Event().logKey("1").metadata(List.of(
				new Metadata().key("Status").value(statusValue1),
				new Metadata().key("ExternalCaseId").value(externalCaseId1))),
			new Event().logKey("2").metadata(List.of(
				new Metadata().key("Status").value(statusValue2),
				new Metadata().key("ExternalCaseId").value(externalCaseId2)))));
		when(eventPageMock.hasNext()).thenReturn(false);
		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), anyString())).thenReturn(eventPageMock);
		when(statusesRepositoryMock.findByCaseManagementStatus(statusValue1)).thenReturn(Optional.of(statuses1));
		when(statusesRepositoryMock.findByCaseManagementStatus(statusValue2)).thenReturn(Optional.of(statuses2));

		final var result = eventLogWorker.updateOepCase(CASE_MANAGEMENT, executionInformationEntity, consumerMock);

		assertThat(result).isTrue();
		verify(oepIntegratorClientMock).setStatus(eq(municipalityId), eq(InstanceType.EXTERNAL), eq(externalCaseId1), any(CaseStatusChangeRequest.class));
		verify(oepIntegratorClientMock).setStatus(eq(municipalityId), eq(InstanceType.EXTERNAL), eq(externalCaseId2), any(CaseStatusChangeRequest.class));
		verify(statusesRepositoryMock).findByCaseManagementStatus(statusValue1);
		verify(statusesRepositoryMock).findByCaseManagementStatus(statusValue2);
		verifyNoInteractions(messagingIntegrationMock);
	}

	@Test
	void updateOepCase_failureContinuesProcessing() {
		final String municipalityId = "testMunicipalityId";
		final var statusValue1 = "Status1";
		final var statusValue2 = "Status2";
		final var externalCaseId1 = "ext-1";
		final var externalCaseId2 = "ext-2";
		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
		final var statuses1 = StatusesEntity.builder().withOepStatus("OepStatus1").build();
		final var statuses2 = StatusesEntity.builder().withOepStatus("OepStatus2").build();

		when(eventPageMock.getContent()).thenReturn(List.of(
			new Event().logKey("1").metadata(List.of(
				new Metadata().key("Status").value(statusValue1),
				new Metadata().key("ExternalCaseId").value(externalCaseId1))),
			new Event().logKey("2").metadata(List.of(
				new Metadata().key("Status").value(statusValue2),
				new Metadata().key("ExternalCaseId").value(externalCaseId2)))));
		when(eventPageMock.hasNext()).thenReturn(false);
		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), anyString())).thenReturn(eventPageMock);
		when(statusesRepositoryMock.findByCaseManagementStatus(statusValue1)).thenReturn(Optional.of(statuses1));
		when(statusesRepositoryMock.findByCaseManagementStatus(statusValue2)).thenReturn(Optional.of(statuses2));
		when(oepIntegratorClientMock.setStatus(eq(municipalityId), any(), eq(externalCaseId1), any()))
			.thenThrow(new RuntimeException("OEP error"));
		when(environmentUtilMock.extractEnvironment()).thenReturn("junit");

		final var result = eventLogWorker.updateOepCase(CASE_MANAGEMENT, executionInformationEntity, consumerMock);

		assertThat(result).isTrue();
		verify(oepIntegratorClientMock).setStatus(eq(municipalityId), eq(InstanceType.EXTERNAL), eq(externalCaseId1), any(CaseStatusChangeRequest.class));
		verify(oepIntegratorClientMock).setStatus(eq(municipalityId), eq(InstanceType.EXTERNAL), eq(externalCaseId2), any(CaseStatusChangeRequest.class));

		final var slackMessageCaptor = ArgumentCaptor.forClass(String.class);
		verify(messagingIntegrationMock, times(1)).sendSlackMessage(eq(municipalityId), slackMessageCaptor.capture());
		assertThat(slackMessageCaptor.getValue())
			.startsWith("CaseStatus [junit] - RequestID: ")
			.contains("Failed to set status 'OepStatus1' for case with external ID 'ext-1'. Error: 'OEP error'");
		verify(consumerMock, times(1)).accept("Failed to update openE status for case with external ID " + externalCaseId1);
	}

	@Test
	void updateSupportManagementStatusesWithInternalChannel() {
		final String municipalityId = "testMunicipalityId";
		final var internalStatus = "SomeInternalStatus";
		final var logkey = "1";
		final var namespace = "namespace";
		final var errand = new Errand()
			.status(internalStatus)
			.channel(INTERNAL_CHANNEL_E_SERVICE)
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
			.addExternalTagsItem(new ExternalTag().key("caseId").value(logkey));

		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();
		final var statuses = StatusesEntity.builder().withOepStatus("NewOepStatus").build();

		when(eventPageMock.getContent()).thenReturn(List.of(new Event().logKey(logkey).metadata(List.of(new Metadata().key("Namespace").value(namespace)))));
		when(eventPageMock.hasNext()).thenReturn(false);
		when(statusesRepositoryMock.findBySupportManagementStatus(internalStatus)).thenReturn(Optional.of(statuses));
		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), anyString())).thenReturn(eventPageMock);
		when(supportManagementServiceMock.getSupportManagementCaseById(eq(municipalityId), any(), anyString())).thenReturn(errand);

		final var result = eventLogWorker.updateSupportManagementStatuses(executionInformationEntity, consumerMock);

		assertThat(result).isTrue();
		verify(oepIntegratorClientMock).setStatus(eq(municipalityId), eq(InstanceType.INTERNAL), any(), any(CaseStatusChangeRequest.class));
	}

	@Test
	void updateSupportManagementStatusesWithInvalidChannel() {
		final String municipalityId = "testMunicipalityId";
		final var logkey = "1";
		final var namespace = "namespace";
		final var errand = new Errand()
			.status("SomeStatus")
			.channel("INVALID_CHANNEL")
			.addExternalTagsItem(new ExternalTag().key("caseId").value(logkey));

		final var executionInformationEntity = ExecutionInformationEntity.builder()
			.withMunicipalityId(municipalityId)
			.withLastSuccessfulExecution(OffsetDateTime.now())
			.build();

		when(eventPageMock.getContent()).thenReturn(List.of(new Event().logKey(logkey).metadata(List.of(new Metadata().key("Namespace").value(namespace)))));
		when(eventPageMock.hasNext()).thenReturn(false);
		when(eventlogClientMock.getEvents(eq(municipalityId), any(PageRequest.class), anyString())).thenReturn(eventPageMock);
		when(supportManagementServiceMock.getSupportManagementCaseById(eq(municipalityId), any(), anyString())).thenReturn(errand);

		final var result = eventLogWorker.updateSupportManagementStatuses(executionInformationEntity, consumerMock);

		assertThat(result).isTrue();
		verifyNoInteractions(oepIntegratorClientMock, statusesRepositoryMock);
	}
}
