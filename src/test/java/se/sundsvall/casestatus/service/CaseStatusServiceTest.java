package se.sundsvall.casestatus.service;

import generated.client.oep_integrator.CaseEnvelope;
import generated.client.oep_integrator.CaseStatus;
import generated.client.oep_integrator.InstanceType;
import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.integration.casedata.CaseDataIntegration;
import se.sundsvall.casestatus.integration.casemanagement.CaseManagementIntegration;
import se.sundsvall.casestatus.integration.db.CaseRepository;
import se.sundsvall.casestatus.integration.db.model.CaseEntity;
import se.sundsvall.casestatus.integration.db.model.StatusesEntity;
import se.sundsvall.casestatus.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casestatus.integration.party.PartyIntegration;
import se.sundsvall.casestatus.service.mapper.CaseManagementMapper;
import se.sundsvall.casestatus.service.mapper.OpenEMapper;
import se.sundsvall.casestatus.service.mapper.SupportManagementMapper;
import se.sundsvall.dept44.async.MdcTaskDecoratorConfiguration;
import se.sundsvall.dept44.problem.Problem;

import static generated.se.sundsvall.casemanagement.CaseStatusDTO.SystemEnum.BYGGR;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static se.sundsvall.TestDataFactory.createCaseStatusDTO;
import static se.sundsvall.TestDataFactory.createCaseStatusResponse;
import static se.sundsvall.TestDataFactory.createErrand;
import static se.sundsvall.casestatus.util.Constants.SUPPORT_MANAGEMENT_SYSTEM;

@SpringBootTest(classes = {
	CaseStatusService.class, OpenEMapper.class, CaseAggregator.class
}, webEnvironment = NONE)
@ImportAutoConfiguration({
	TaskExecutionAutoConfiguration.class, MdcTaskDecoratorConfiguration.class
})
class CaseStatusServiceTest {

	private static final String EXTERNAL_CASE_ID = "someExternalCaseId";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String NAMESPACE_1 = "namespace1";
	private static final String NAMESPACE_2 = "namespace2";
	private static final InstanceType INSTANCE_TYPE = InstanceType.EXTERNAL;

	@MockitoBean
	private PartyIntegration partyIntegrationMock;

	@MockitoBean
	private CaseManagementIntegration caseManagementIntegrationMock;

	@MockitoBean
	private CaseDataIntegration caseDataIntegrationMock;

	@MockitoBean
	private OepIntegratorClient openEIntegrationMock;

	@MockitoBean
	private CaseRepository caseRepositoryMock;

	@MockitoBean
	private SupportManagementService supportManagementServiceMock;

	@MockitoBean
	private StatusVocabulary statusVocabularyMock;

	@MockitoBean
	private CaseManagementMapper caseManagementMapperMock;

	@MockitoBean
	private SupportManagementMapper supportManagementMapperMock;

	@MockitoSpyBean
	@Qualifier(APPLICATION_TASK_EXECUTOR_BEAN_NAME)
	private Executor taskExecutorSpy;

	@Autowired
	private CaseStatusService caseStatusService;

	@Autowired
	private CaseAggregator caseAggregator;

	@Test
	void getOepStatusCaseStatusFoundInCaseManagement() {
		when(caseManagementIntegrationMock.getCaseStatusForExternalId(any(String.class), any(String.class)))
			.thenReturn(Optional.of(new CaseStatusDTO().status("someStatus")));
		when(statusVocabularyMock.findOepStatusForCaseManagementStatus("someStatus"))
			.thenReturn(Optional.of("someStatus"));

		final var status = caseStatusService.getOepStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID);

		assertThat(status).isNotNull().satisfies(oepStatus -> {
			assertThat(oepStatus.getKey()).isEqualTo("status");
			assertThat(oepStatus.getValue()).isEqualTo("someStatus");
		});

		verify(caseManagementIntegrationMock).getCaseStatusForExternalId(any(String.class), any(String.class));
		verifyNoMoreInteractions(caseManagementIntegrationMock);
		verify(statusVocabularyMock).findOepStatusForCaseManagementStatus(any(String.class));
		verifyNoMoreInteractions(caseManagementIntegrationMock, statusVocabularyMock);
	}

	@Test
	void getOepStatusCaseStatusNotFoundInCaseManagement() {
		when(caseManagementIntegrationMock.getCaseStatusForExternalId(any(String.class), any(String.class)))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> caseStatusService.getOepStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Case with id someExternalCaseId not found");

		verify(caseManagementIntegrationMock).getCaseStatusForExternalId(any(String.class), any(String.class));
		verifyNoMoreInteractions(caseManagementIntegrationMock);
	}

	@Test
	void getOepStatusCaseStatusNullInCaseManagement() {
		when(caseManagementIntegrationMock.getCaseStatusForExternalId(any(String.class), any(String.class)))
			.thenReturn(Optional.of(new CaseStatusDTO()));

		assertThatThrownBy(() -> caseStatusService.getOepStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Case with id someExternalCaseId not found");

		verify(caseManagementIntegrationMock).getCaseStatusForExternalId(any(String.class), any(String.class));
		verifyNoMoreInteractions(caseManagementIntegrationMock);
		verifyNoInteractions(statusVocabularyMock);
	}

	@Test
	void getCaseStatusCaseStatusFoundInCaseManagement() {
		final var caseStatus = new CaseStatusDTO()
			.caseId("someCaseId")
			.externalCaseId(EXTERNAL_CASE_ID)
			.caseType("PARKING_PERMIT")
			.timestamp(LocalDateTime.now())
			.status("someStatus")
			.errandNumber("errandNumber")
			.namespace("namespace");

		when(caseManagementIntegrationMock.getCaseStatusForExternalId(any(String.class), any(String.class)))
			.thenReturn(Optional.of(caseStatus));

		when(caseManagementMapperMock.toCaseStatusResponse(caseStatus, MUNICIPALITY_ID))
			.thenReturn(CaseStatusResponse.builder().withCaseId("someCaseId").withExternalCaseId(EXTERNAL_CASE_ID).withCaseType("PARKING_PERMIT").withNamespace("namespace").withErrandNumber("errandNumber").build());

		final var result = caseStatusService.getCaseStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("someCaseId");
		assertThat(result.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
		assertThat(result.getCaseType()).isEqualTo("PARKING_PERMIT");
		assertThat(result.getNamespace()).isEqualTo("namespace");
		assertThat(result.getErrandNumber()).isEqualTo("errandNumber");

		verify(caseManagementIntegrationMock).getCaseStatusForExternalId(any(String.class), any(String.class));
		verify(caseManagementMapperMock).toCaseStatusResponse(caseStatus, MUNICIPALITY_ID);
		verifyNoMoreInteractions(caseManagementIntegrationMock);
	}

	@Test
	void getCaseStatusCaseStatusNotFoundInCaseManagement() {
		final var companyEntity = CaseEntity.builder()
			.withFlowInstanceId("someFlowInstanceId")
			.withErrandType("someErrandType")
			.withStatus("someStatus")
			.withFirstSubmitted("someFirstSubmittedValue")
			.withLastStatusChange("someLastStatusChangeValue")
			.build();

		when(caseManagementIntegrationMock.getCaseStatusForExternalId(any(String.class), any(String.class)))
			.thenReturn(Optional.empty());
		when(caseRepositoryMock.findByFlowInstanceIdAndMunicipalityId(any(String.class), any(String.class)))
			.thenReturn(Optional.ofNullable(companyEntity));

		final var result = caseStatusService.getCaseStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID);

		assertThat(result).isNotNull();
		assertThat(result.getExternalCaseId()).isEqualTo("someFlowInstanceId");
		assertThat(result.getCaseType()).isEqualTo("someErrandType");
		assertThat(result.getFirstSubmitted()).isEqualTo("someFirstSubmittedValue");
		assertThat(result.getLastStatusChange()).isEqualTo("someLastStatusChangeValue");

		verify(caseManagementIntegrationMock).getCaseStatusForExternalId(any(String.class), any(String.class));
		verify(caseRepositoryMock).findByFlowInstanceIdAndMunicipalityId(any(String.class), any(String.class));
		verifyNoMoreInteractions(caseManagementIntegrationMock, caseRepositoryMock);
	}

	@Test
	void getCasePdf() {

		final var municipalityId = "someMunicipalityId";

		when(openEIntegrationMock.getCasePdfByFlowInstanceId(any(), any(), any(String.class)))
			.thenReturn(ResponseEntity.of(Optional.of(new InputStreamResource(new ByteArrayInputStream("lol".getBytes())))));

		final var result = caseStatusService.getCasePdf(municipalityId, "someExternalCaseID");

		assertThat(result).isNotNull();

		verify(openEIntegrationMock).getCasePdfByFlowInstanceId(any(), any(), any(String.class));
		verifyNoMoreInteractions(openEIntegrationMock);
	}

	@Test
	void getCaseStatuses() {

		final var partyId = "somePartyId";
		final var smStatus = "smStatus";
		final var errand = createErrand();
		final var statuses = StatusesEntity.builder()
			.withSupportManagementStatus(smStatus)
			.withExternalStatus("Handläggning pågår")
			.build();

		final var classificationDisplayName = "classificationDisplayName";

		when(caseManagementIntegrationMock.getCaseStatusForOrganizationNumber(any(String.class), any(String.class)))
			.thenReturn(List.of(new CaseStatusDTO().status("someStatus"), new CaseStatusDTO().status("someOtherStatus")));

		when(caseManagementMapperMock.toCaseStatusResponse(any(CaseStatusDTO.class), eq(MUNICIPALITY_ID)))
			.thenReturn(CaseStatusResponse.builder().build());

		when(caseRepositoryMock.findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class)))
			.thenReturn(List.of(CaseEntity.builder().build()));

		when(partyIntegrationMock.getPartyIdByOrganizationNumber(MUNICIPALITY_ID, "someOrganizationId"))
			.thenReturn(Optional.of(partyId));

		when(supportManagementServiceMock.getSupportManagementCasesByExternalId(MUNICIPALITY_ID, partyId))
			.thenReturn(Map.of(NAMESPACE_1, List.of(errand.status(smStatus))));

		when(supportManagementServiceMock.getClassificationDisplayName(MUNICIPALITY_ID, NAMESPACE_1, errand)).thenReturn(classificationDisplayName);
		when(statusVocabularyMock.lookupBySupportManagementStatus(smStatus)).thenReturn(statuses);

		when(supportManagementMapperMock.toCaseStatusResponse(errand, NAMESPACE_1, statuses, classificationDisplayName)).thenReturn(createCaseStatusResponse(SUPPORT_MANAGEMENT_SYSTEM, "1234567890"));
		final var result = caseStatusService.getCaseStatuses("someOrganizationId", MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(4);

		verify(caseManagementIntegrationMock).getCaseStatusForOrganizationNumber(any(String.class), any(String.class));
		verify(caseManagementMapperMock, times(2)).toCaseStatusResponse(any(CaseStatusDTO.class), eq(MUNICIPALITY_ID));
		verify(caseRepositoryMock).findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class));
		verify(partyIntegrationMock).getPartyIdByOrganizationNumber(MUNICIPALITY_ID, "someOrganizationId");
		verify(supportManagementServiceMock).getSupportManagementCasesByExternalId(MUNICIPALITY_ID, partyId);
		verify(supportManagementServiceMock).getClassificationDisplayName(MUNICIPALITY_ID, NAMESPACE_1, errand);
		verify(supportManagementMapperMock).toCaseStatusResponse(errand, NAMESPACE_1, statuses, classificationDisplayName);
		verify(statusVocabularyMock).lookupBySupportManagementStatus(smStatus);
		verify(statusVocabularyMock).translateOepStatus(nullable(String.class));

		verifyNoMoreInteractions(caseManagementIntegrationMock, caseRepositoryMock, caseManagementMapperMock, partyIntegrationMock, supportManagementServiceMock, supportManagementMapperMock, statusVocabularyMock);
	}

	@Test
	void getCaseStatusesWhenNullStatusFromSM() {

		final var partyId = "somePartyId";
		final var errand = createErrand();
		final var statuses = StatusesEntity.builder()
			.build();

		final var classificationDisplayName = "classificationDisplayName";

		when(caseManagementIntegrationMock.getCaseStatusForOrganizationNumber(any(String.class), any(String.class)))
			.thenReturn(List.of(new CaseStatusDTO().status("someStatus"), new CaseStatusDTO().status("someOtherStatus")));

		when(caseManagementMapperMock.toCaseStatusResponse(any(CaseStatusDTO.class), eq(MUNICIPALITY_ID)))
			.thenReturn(CaseStatusResponse.builder().build());

		when(caseRepositoryMock.findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class)))
			.thenReturn(List.of(CaseEntity.builder().build()));

		when(partyIntegrationMock.getPartyIdByOrganizationNumber(MUNICIPALITY_ID, "someOrganizationId"))
			.thenReturn(Optional.of(partyId));

		when(supportManagementServiceMock.getSupportManagementCasesByExternalId(MUNICIPALITY_ID, partyId))
			.thenReturn(Map.of(NAMESPACE_1, List.of(errand.status(null))));

		when(supportManagementServiceMock.getClassificationDisplayName(MUNICIPALITY_ID, NAMESPACE_1, errand)).thenReturn(classificationDisplayName);
		when(statusVocabularyMock.lookupBySupportManagementStatus(null)).thenReturn(statuses);

		when(supportManagementMapperMock.toCaseStatusResponse(errand, NAMESPACE_1, statuses, classificationDisplayName)).thenReturn(createCaseStatusResponse(SUPPORT_MANAGEMENT_SYSTEM, "1234567890"));
		final var result = caseStatusService.getCaseStatuses("someOrganizationId", MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(4);

		verify(caseManagementIntegrationMock).getCaseStatusForOrganizationNumber(any(String.class), any(String.class));
		verify(caseManagementMapperMock, times(2)).toCaseStatusResponse(any(CaseStatusDTO.class), eq(MUNICIPALITY_ID));
		verify(caseRepositoryMock).findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class));
		verify(partyIntegrationMock).getPartyIdByOrganizationNumber(MUNICIPALITY_ID, "someOrganizationId");
		verify(supportManagementServiceMock).getSupportManagementCasesByExternalId(MUNICIPALITY_ID, partyId);
		verify(supportManagementServiceMock).getClassificationDisplayName(MUNICIPALITY_ID, NAMESPACE_1, errand);
		verify(supportManagementMapperMock).toCaseStatusResponse(errand, NAMESPACE_1, statuses, classificationDisplayName);
		verify(statusVocabularyMock).lookupBySupportManagementStatus(null);
		verify(statusVocabularyMock).translateOepStatus(nullable(String.class));

		verifyNoMoreInteractions(caseManagementIntegrationMock, caseRepositoryMock, caseManagementMapperMock, partyIntegrationMock, supportManagementServiceMock, supportManagementMapperMock, statusVocabularyMock);
	}

	@Test
	void getOepStatusCaseStatusNotFoundInOpenE() {
		when(caseManagementIntegrationMock.getCaseStatusForExternalId(any(String.class), any(String.class)))
			.thenReturn(Optional.of(new CaseStatusDTO().status("someStatus")));
		when(statusVocabularyMock.findOepStatusForCaseManagementStatus("someStatus"))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> caseStatusService.getOepStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Could not find matching open-E status for status someStatus");

		verify(caseManagementIntegrationMock).getCaseStatusForExternalId(any(String.class), any(String.class));
		verify(statusVocabularyMock).findOepStatusForCaseManagementStatus(any(String.class));
		verifyNoMoreInteractions(caseManagementIntegrationMock, statusVocabularyMock);
	}

	@Test
	void getCaseStatusCompanyRepositoryNotFound() {
		when(caseManagementIntegrationMock.getCaseStatusForExternalId(any(String.class), any(String.class)))
			.thenReturn(Optional.empty());
		when(caseRepositoryMock.findByFlowInstanceIdAndMunicipalityId(any(String.class), any(String.class)))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> caseStatusService.getCaseStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Case with id someExternalCaseId not found");

		verify(caseManagementIntegrationMock).getCaseStatusForExternalId(any(String.class), any(String.class));
		verify(caseRepositoryMock).findByFlowInstanceIdAndMunicipalityId(any(String.class), any(String.class));
		verifyNoMoreInteractions(caseManagementIntegrationMock, caseRepositoryMock);
	}

	@Test
	void getCaseStatuses_companyRepositoryNotFound() {
		when(caseManagementIntegrationMock.getCaseStatusForOrganizationNumber(any(String.class), any(String.class)))
			.thenReturn(List.of(new CaseStatusDTO().status("someStatus")));
		when(caseManagementMapperMock.toCaseStatusResponse(any(CaseStatusDTO.class), eq(MUNICIPALITY_ID)))
			.thenReturn(CaseStatusResponse.builder().build());
		when(caseRepositoryMock.findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class)))
			.thenReturn(List.of());
		when(partyIntegrationMock.getPartyIdByOrganizationNumber(MUNICIPALITY_ID, "someOrganizationId"))
			.thenReturn(Optional.of("somePartyId"));

		final var result = caseStatusService.getCaseStatuses("someOrganizationId", MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(1);

		verify(caseManagementIntegrationMock).getCaseStatusForOrganizationNumber(any(String.class), any(String.class));
		verify(caseManagementMapperMock).toCaseStatusResponse(any(CaseStatusDTO.class), eq(MUNICIPALITY_ID));
		verify(caseRepositoryMock).findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class));
		verify(partyIntegrationMock).getPartyIdByOrganizationNumber(MUNICIPALITY_ID, "someOrganizationId");
		verify(supportManagementServiceMock).getSupportManagementCasesByExternalId(MUNICIPALITY_ID, "somePartyId");
		verifyNoMoreInteractions(caseManagementIntegrationMock, caseRepositoryMock, caseManagementMapperMock, partyIntegrationMock, supportManagementServiceMock);
	}

	/**
	 * Test scenario where the organization number is unknown to Party. The SupportManagement search is skipped while the
	 * other sources still contribute to the result.
	 */
	@Test
	void getCaseStatuses_organizationNumberUnknownToParty() {
		when(caseManagementIntegrationMock.getCaseStatusForOrganizationNumber(any(String.class), any(String.class)))
			.thenReturn(List.of(new CaseStatusDTO().status("someStatus")));
		when(caseManagementMapperMock.toCaseStatusResponse(any(CaseStatusDTO.class), eq(MUNICIPALITY_ID)))
			.thenReturn(CaseStatusResponse.builder().build());
		when(caseRepositoryMock.findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class)))
			.thenReturn(List.of());
		when(partyIntegrationMock.getPartyIdByOrganizationNumber(MUNICIPALITY_ID, "someOrganizationId"))
			.thenReturn(Optional.empty());

		final var result = caseStatusService.getCaseStatuses("someOrganizationId", MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(1);

		verify(partyIntegrationMock).getPartyIdByOrganizationNumber(MUNICIPALITY_ID, "someOrganizationId");
		verifyNoInteractions(supportManagementServiceMock);
		verifyNoMoreInteractions(partyIntegrationMock);
	}

	/**
	 * Test scenario where one case is found in CaseManagement, one case is found in OpenE and one case is found in
	 * SupportManagement.
	 */
	@Test
	void getPrivateCaseStatuses() {
		final var partyId = "somePartyId";
		final var includeDrafts = true;

		final var caseStatus = createCaseStatusDTO(BYGGR);
		final var caseStatuses = List.of(caseStatus);

		final var oepStatus = "oepStatus";
		final var externalStatus = "externalStatus";
		final var smStatus = "smStatus";

		final var errand = createErrand().status(smStatus);
		final var errands = List.of(errand);
		final var errandMap = Map.of(NAMESPACE_1, errands);

		final var statuses = StatusesEntity.builder()
			.withSupportManagementStatus(smStatus)
			.withOepStatus(oepStatus)
			.withExternalStatus(externalStatus)
			.build();
		final var classificationDisplayName = "classificationDisplayName";

		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID)).thenReturn(caseStatuses);
		when(caseManagementMapperMock.toCaseStatusResponse(caseStatus, MUNICIPALITY_ID)).thenReturn(createCaseStatusResponse("CASEDATA", "1234567890"));

		when(openEIntegrationMock.getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true)).thenReturn(List.of(new CaseEnvelope().displayName("someTitle").status(new CaseStatus().status(oepStatus)).flowInstanceId("someFlowInstanceId")));

		when(statusVocabularyMock.lookupBySupportManagementStatus(smStatus)).thenReturn(statuses);
		when(supportManagementServiceMock.getClassificationDisplayName(MUNICIPALITY_ID, NAMESPACE_1, errand)).thenReturn(classificationDisplayName);

		when(supportManagementMapperMock.toCaseStatusResponse(errand, NAMESPACE_1, statuses, classificationDisplayName)).thenReturn(createCaseStatusResponse("BYGGR", "1234567890"));
		when(supportManagementServiceMock.getSupportManagementCasesByExternalId(MUNICIPALITY_ID, partyId)).thenReturn(errandMap);

		final var result = caseStatusService.getCaseStatusesForParty(partyId, MUNICIPALITY_ID, includeDrafts);

		assertThat(result).isNotNull().hasSize(3);

		verify(caseManagementIntegrationMock).getCaseStatusForPartyId(partyId, MUNICIPALITY_ID);

		verify(openEIntegrationMock).getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true);
		verify(openEIntegrationMock).getMultisignCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true);
		verify(openEIntegrationMock).getUnsubmittedCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true);
		verify(supportManagementServiceMock).getSupportManagementCasesByExternalId(MUNICIPALITY_ID, partyId);
		verify(supportManagementServiceMock).getClassificationDisplayName(MUNICIPALITY_ID, NAMESPACE_1, errand);
		verify(supportManagementMapperMock).toCaseStatusResponse(errand, NAMESPACE_1, statuses, classificationDisplayName);
		verify(statusVocabularyMock).lookupBySupportManagementStatus(smStatus);
		verify(statusVocabularyMock).translateOepStatus(any(CaseStatus.class));
		verify(taskExecutorSpy, times(4)).execute(any());
		verifyNoMoreInteractions(caseManagementIntegrationMock, openEIntegrationMock, supportManagementServiceMock, statusVocabularyMock);
	}

	/**
	 * Test scenario where one case is found in CaseManagement and one case is found in OpenE.
	 */
	@Test
	void getEnterpriseCaseStatuses1() {
		final var partyId = "somePartyId";
		final var includeDrafts = true;

		final var caseStatus = createCaseStatusDTO(BYGGR);
		final var caseStatuses = List.of(caseStatus);
		final var title = "someTitle";

		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID)).thenReturn(caseStatuses);
		when(caseManagementMapperMock.toCaseStatusResponse(caseStatus, MUNICIPALITY_ID)).thenReturn(createCaseStatusResponse("BYGGR", "1234567890"));
		when(openEIntegrationMock.getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true)).thenReturn(List.of(new CaseEnvelope().displayName(title).status(new CaseStatus().name("someStatus")).flowInstanceId("someFlowInstanceId")));

		final var result = caseStatusService.getCaseStatusesForParty(partyId, MUNICIPALITY_ID, includeDrafts);

		assertThat(result).isNotNull().hasSize(2);

		verify(caseManagementIntegrationMock).getCaseStatusForPartyId(partyId, MUNICIPALITY_ID);
		verify(caseManagementMapperMock).toCaseStatusResponse(caseStatus, MUNICIPALITY_ID);
		verify(openEIntegrationMock).getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true);
		verify(openEIntegrationMock).getMultisignCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true);
		verify(openEIntegrationMock).getUnsubmittedCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true);
		verify(taskExecutorSpy, times(4)).execute(any());
		verifyNoMoreInteractions(caseManagementIntegrationMock, caseManagementMapperMock, openEIntegrationMock);
	}

	/**
	 * Test scenario where no case is found in CaseManagement and one case is found in OpenE
	 */
	@Test
	void getEnterpriseCaseStatuses2() {
		final var partyId = "somePartyId";
		final var title = "someTitle";
		final var includeDrafts = true;

		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID)).thenReturn(emptyList());
		when(openEIntegrationMock.getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true)).thenReturn(List.of(new CaseEnvelope().displayName(title).status(new CaseStatus().name("someStatus")).flowInstanceId("someFlowInstanceId")));

		final var result = caseStatusService.getCaseStatusesForParty(partyId, MUNICIPALITY_ID, includeDrafts);

		assertThat(result).isNotNull().hasSize(1);

		verify(caseManagementIntegrationMock).getCaseStatusForPartyId(partyId, MUNICIPALITY_ID);
		verify(openEIntegrationMock).getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true);
		verify(openEIntegrationMock).getMultisignCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true);
		verify(openEIntegrationMock).getUnsubmittedCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true);
		verify(taskExecutorSpy, times(4)).execute(any());
		verifyNoMoreInteractions(caseManagementIntegrationMock, caseManagementMapperMock, openEIntegrationMock);
	}

	/**
	 * Test scenario where only CaseManagement returns a case for the party.
	 */
	@Test
	void getCaseStatusesForParty1() {
		final var partyId = "somePartyId";
		final var includeDrafts = true;

		final var caseStatus = createCaseStatusDTO(BYGGR);
		final var caseStatuses = List.of(caseStatus);
		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID)).thenReturn(caseStatuses);
		when(caseManagementMapperMock.toCaseStatusResponse(caseStatus, MUNICIPALITY_ID)).thenReturn(createCaseStatusResponse("CASEDATA", "1234567890"));

		// act
		final var result = caseStatusService.getCaseStatusesForParty(partyId, MUNICIPALITY_ID, includeDrafts);

		assertThat(result).isNotNull().hasSize(1);

		verify(caseManagementIntegrationMock).getCaseStatusForPartyId(partyId, MUNICIPALITY_ID);
		verify(caseManagementMapperMock).toCaseStatusResponse(caseStatus, MUNICIPALITY_ID);
		verify(taskExecutorSpy, times(4)).execute(any());
		verifyNoMoreInteractions(partyIntegrationMock, caseManagementMapperMock, caseManagementIntegrationMock);
	}

	/**
	 * Test scenario where only Open-E returns a case for the party.
	 */
	@Test
	void getCaseStatusesForParty2() {
		final var partyId = "somePartyId";
		final var includeDrafts = true;

		final var spy = Mockito.spy(caseStatusService);
		final var title = "someTitle";

		when(openEIntegrationMock.getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true)).thenReturn(List.of(new CaseEnvelope().displayName(title).status(new CaseStatus().name("someStatus")).flowInstanceId("someFlowInstanceId")));

		final var result = caseStatusService.getCaseStatusesForParty(partyId, MUNICIPALITY_ID, includeDrafts);

		assertThat(result).isNotNull().hasSize(1);

		verify(openEIntegrationMock).getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true);
		verify(openEIntegrationMock).getMultisignCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true);
		verify(openEIntegrationMock).getUnsubmittedCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true);
		verify(taskExecutorSpy, times(4)).execute(any());
		verifyNoMoreInteractions(partyIntegrationMock, spy);
	}

	/**
	 * Multi-sign cases share the same flowInstanceId as a regular OeP case (the same flow instance is also waiting for
	 * signature). The multi-sign entry should win and the regular OeP entry should be dropped before filterResponses
	 * runs.
	 */
	@Test
	void getCaseStatusesForParty_multisignDeduplicatesRegularOepCase() {
		final var partyId = "somePartyId";
		final var flowInstanceId = "sharedFlowInstanceId";
		final var includeDrafts = true;

		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID)).thenReturn(emptyList());

		when(openEIntegrationMock.getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true))
			.thenReturn(List.of(new CaseEnvelope().displayName("regular").status(new CaseStatus().name("regularStatus")).flowInstanceId(flowInstanceId)));
		when(openEIntegrationMock.getMultisignCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true))
			.thenReturn(List.of(new CaseEnvelope().displayName("multisign").status(new CaseStatus().name("multisignStatus")).flowInstanceId(flowInstanceId)));

		final var result = caseStatusService.getCaseStatusesForParty(partyId, MUNICIPALITY_ID, includeDrafts);

		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst().getExternalCaseId()).isEqualTo(flowInstanceId);
		assertThat(result.getFirst().getCaseType()).isEqualTo("multisign");
		assertThat(result.getFirst().getStatus()).isEqualTo("multisignStatus");
	}

	/**
	 * Multi-sign cases are actionable items and must bypass the draft filter, even when includeDrafts=false.
	 */
	@Test
	void getCaseStatusesForParty_multisignBypassesDraftFilter() {
		final var partyId = "somePartyId";
		final var includeDrafts = false;

		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID)).thenReturn(emptyList());
		when(openEIntegrationMock.getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true)).thenReturn(emptyList());
		when(openEIntegrationMock.getMultisignCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true))
			.thenReturn(List.of(new CaseEnvelope().displayName("multisign").status(new CaseStatus().name("Utkast")).flowInstanceId("multisignFlowInstanceId")));

		final var result = caseStatusService.getCaseStatusesForParty(partyId, MUNICIPALITY_ID, includeDrafts);

		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst().getExternalCaseId()).isEqualTo("multisignFlowInstanceId");
		assertThat(result.getFirst().getStatus()).isEqualTo("Utkast");
	}

	/**
	 * Unsubmitted (saved but not yet submitted) Open-E cases are drafts and must surface when includeDrafts=true.
	 */
	@Test
	void getCaseStatusesForParty_unsubmittedIncludedWhenDraftsRequested() {
		final var partyId = "somePartyId";
		final var includeDrafts = true;

		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID)).thenReturn(emptyList());
		when(openEIntegrationMock.getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true)).thenReturn(emptyList());
		when(openEIntegrationMock.getUnsubmittedCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true))
			.thenReturn(List.of(new CaseEnvelope().displayName("unsubmitted").status(new CaseStatus().name("Utkast")).flowInstanceId("unsubmittedFlowInstanceId")));

		final var result = caseStatusService.getCaseStatusesForParty(partyId, MUNICIPALITY_ID, includeDrafts);

		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst().getExternalCaseId()).isEqualTo("unsubmittedFlowInstanceId");
		assertThat(result.getFirst().getStatus()).isEqualTo("Utkast");

		verify(openEIntegrationMock).getUnsubmittedCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true);
	}

	/**
	 * Unsubmitted Open-E cases are drafts by definition and are therefore not even fetched when includeDrafts=false,
	 * unlike multi-sign cases which are fetched and bypass the draft filter.
	 */
	@Test
	void getCaseStatusesForParty_unsubmittedNotFetchedWhenDraftsExcluded() {
		final var partyId = "somePartyId";
		final var includeDrafts = false;

		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID)).thenReturn(emptyList());
		when(openEIntegrationMock.getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true)).thenReturn(emptyList());

		final var result = caseStatusService.getCaseStatusesForParty(partyId, MUNICIPALITY_ID, includeDrafts);

		assertThat(result).isNotNull().isEmpty();

		verify(openEIntegrationMock, never()).getUnsubmittedCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId, true);
	}

	/**
	 * Test scenario where two CaseStatusResponses are filtered. They share the same 'externalCaseId' but different
	 * 'system'. Expects that the response with the 'system' value 'OPEN_E_PLATFORM' is filtered out.
	 */
	@Test
	void filterResponses1() {
		final var caseResponse1 = createCaseStatusResponse("OPEN_E_PLATFORM", "externalCaseId");
		final var caseResponse2 = createCaseStatusResponse("BYGGR", "externalCaseId");
		final var responses = List.of(caseResponse1, caseResponse2);
		final var includeDrafts = true;

		final var result = caseAggregator.filterResponses(responses, includeDrafts);

		assertThat(result).isNotNull().containsOnly(caseResponse2);
	}

	/**
	 * Test scenario where two CaseStatusResponses are filtered. They have different externalCaseId's. Expects that both
	 * responses are returned.
	 */
	@Test
	void filterResponses2() {
		final var caseResponse1 = createCaseStatusResponse("OPEN_E_PLATFORM", "12345");
		final var caseResponse2 = createCaseStatusResponse("BYGGR", "54321");
		final var responses = List.of(caseResponse1, caseResponse2);
		final var includeDrafts = true;

		final var result = caseAggregator.filterResponses(responses, includeDrafts);

		assertThat(result).isNotNull().containsOnly(caseResponse1, caseResponse2);
	}

	/**
	 * Test scenario where two CaseStatusResponses are filtered. Both have 'null' and 'externalCaseId'. Expects that no
	 * filtering is done.
	 */
	@Test
	void filterResponses3() {
		final var caseResponse1 = createCaseStatusResponse("OPEN_E_PLATFORM", null);
		final var caseResponse2 = createCaseStatusResponse("BYGGR", null);
		final var responses = List.of(caseResponse1, caseResponse2);
		final var includeDrafts = true;

		final var result = caseAggregator.filterResponses(responses, includeDrafts);

		assertThat(result).isNotNull().containsOnly(caseResponse1, caseResponse2);
	}

	/**
	 * Test scenario where two OPEN_E_PLATFORM responses are filtered. They share the same 'externalCaseId'. Expects that
	 * both responses are filtered out. 'ExternalCaseId' is unique per instance of Open-E Platform, this scenario should
	 * never happen.
	 */
	@Test
	void filterResponses4() {
		final var caseResponse1 = createCaseStatusResponse("OPEN_E_PLATFORM", "12345");
		final var caseResponse2 = createCaseStatusResponse("OPEN_E_PLATFORM", "12345");
		final var includeDrafts = true;

		final var responses = List.of(caseResponse1, caseResponse2);

		final var result = caseAggregator.filterResponses(responses, includeDrafts);

		assertThat(result).isNotNull().isEmpty();
	}

	/**
	 * Test scenario where rafts are filtered out
	 */
	@Test
	void filterResponses5() {
		final var caseResponse1 = createCaseStatusResponse("BYGGR", "BR-12345");
		final var caseResponse2 = createCaseStatusResponse("CASE_DATA", "CD-12345");
		caseResponse2.setStatus("Utkast");
		final var includeDrafts = false;

		final var responses = List.of(caseResponse1, caseResponse2);

		final var result = caseAggregator.filterResponses(responses, includeDrafts);

		assertThat(result)
			.isNotNull()
			.hasSize(1)
			.containsOnly(caseResponse1);
	}

	@Test
	void getErrandStatuses_noRequestParameters() {
		assertThatThrownBy(() -> caseStatusService.getErrandStatuses(MUNICIPALITY_ID, null, null))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Request: Either propertyDesignation or errandNumber must be provided");
		verifyNoInteractions(caseDataIntegrationMock, supportManagementServiceMock);
	}

	@ParameterizedTest
	@CsvSource(value = {
		"'',''", "' ',' '", "'',", ",''", "' ',"
	})
	void getErrandStatuses_blankRequestParameters(final String propertyDesignation, final String errandNumber) {
		assertThatThrownBy(() -> caseStatusService.getErrandStatuses(MUNICIPALITY_ID, propertyDesignation, errandNumber))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Request: Either propertyDesignation or errandNumber must be provided");
		verifyNoInteractions(caseDataIntegrationMock, supportManagementServiceMock);
	}

	@Test
	void getErrandStatuses_bothRequestParameters() {
		assertThatThrownBy(() -> caseStatusService.getErrandStatuses(MUNICIPALITY_ID, "Moon Street 1", "Case 123"))
			.isInstanceOf(Problem.class)
			.hasMessage("Bad Request: Both propertyDesignation and errandNumber cannot be provided at the same time");
		verifyNoInteractions(caseDataIntegrationMock, supportManagementServiceMock);
	}

	@Test
	void getErrandStatuses_propertyDesignation() {
		final var propertyDesignation = "Moon Street 1";

		when(caseDataIntegrationMock.getNamespaces()).thenReturn(List.of(NAMESPACE_1, NAMESPACE_2));
		when(caseDataIntegrationMock.getCaseDataCaseByPropertyDesignation(MUNICIPALITY_ID, NAMESPACE_1, propertyDesignation))
			.thenReturn(List.of(createCaseStatusResponse("CASE_DATA", "1234567890")));
		when(caseDataIntegrationMock.getCaseDataCaseByPropertyDesignation(MUNICIPALITY_ID, NAMESPACE_2, propertyDesignation))
			.thenReturn(List.of(createCaseStatusResponse("CASE_DATA", "0987654321")));

		final var result = caseStatusService.getErrandStatuses(MUNICIPALITY_ID, propertyDesignation, null);

		assertThat(result).isNotNull().hasSize(2);

		verify(caseDataIntegrationMock).getCaseDataCaseByPropertyDesignation(MUNICIPALITY_ID, NAMESPACE_1, propertyDesignation);
		verify(caseDataIntegrationMock).getCaseDataCaseByPropertyDesignation(MUNICIPALITY_ID, NAMESPACE_2, propertyDesignation);
		verifyNoMoreInteractions(caseManagementIntegrationMock);
		verifyNoInteractions(supportManagementServiceMock);
	}

	@Test
	void getErrandStatuses_errandNumber() {
		final var errandNumber = "Case 123";
		final var oepStatus = "oepStatus";
		final var externalStatus = "externalStatus";
		final var smStatus = "smStatus";
		final var cmStatus = "status";
		final var statuses = StatusesEntity.builder()
			.withSupportManagementStatus(smStatus)
			.withCaseManagementStatus(cmStatus)
			.withOepStatus(oepStatus)
			.withExternalStatus(externalStatus)
			.build();

		final var supportManagementErrand = createErrand().status(smStatus);
		final var classificationDisplayName = "classificationDisplayName";

		when(supportManagementServiceMock.getSupportManagementCases(MUNICIPALITY_ID, "errandNumber:'%s'".formatted(errandNumber)))
			.thenReturn(Map.of(NAMESPACE_1, List.of(supportManagementErrand)));

		when(statusVocabularyMock.lookupBySupportManagementStatus(smStatus)).thenReturn(statuses);
		when(supportManagementServiceMock.getClassificationDisplayName(MUNICIPALITY_ID, NAMESPACE_1, supportManagementErrand)).thenReturn(classificationDisplayName);

		when(supportManagementMapperMock.toCaseStatusResponse(supportManagementErrand, NAMESPACE_1, statuses, classificationDisplayName)).thenReturn(createCaseStatusResponse("SUPPORT_MANAGEMENT",
			"1234567890"));
		when(caseDataIntegrationMock.getNamespaces()).thenReturn(List.of(NAMESPACE_1));
		when(caseDataIntegrationMock.getCaseDataCaseByErrandNumber(MUNICIPALITY_ID, NAMESPACE_1, errandNumber))
			.thenReturn(List.of(createCaseStatusResponse("CASE_DATA", "1234567890")));

		final var result = caseStatusService.getErrandStatuses(MUNICIPALITY_ID, null, errandNumber);

		assertThat(result).isNotNull().hasSize(2);
		assertThat(result.getFirst().getSystem()).isEqualTo("SUPPORT_MANAGEMENT");
		assertThat(result.getLast().getSystem()).isEqualTo("CASE_DATA");

		verify(caseDataIntegrationMock).getCaseDataCaseByErrandNumber(MUNICIPALITY_ID, NAMESPACE_1, errandNumber);
		verify(caseDataIntegrationMock).getNamespaces();
		verify(supportManagementServiceMock).getSupportManagementCases(MUNICIPALITY_ID, "errandNumber:'%s'".formatted(errandNumber));
		verify(supportManagementServiceMock).getClassificationDisplayName(MUNICIPALITY_ID, NAMESPACE_1, supportManagementErrand);
		verify(supportManagementMapperMock).toCaseStatusResponse(supportManagementErrand, NAMESPACE_1, statuses, classificationDisplayName);
		verify(statusVocabularyMock).lookupBySupportManagementStatus(smStatus);
		verifyNoMoreInteractions(caseDataIntegrationMock, supportManagementServiceMock, statusVocabularyMock, supportManagementMapperMock);
	}
}
