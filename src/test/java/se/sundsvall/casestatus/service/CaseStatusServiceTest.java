package se.sundsvall.casestatus.service;

import static generated.se.sundsvall.casemanagement.CaseStatusDTO.SystemEnum.BYGGR;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.TestDataFactory.createCaseStatusDTO;
import static se.sundsvall.TestDataFactory.createCaseStatusResponse;
import static se.sundsvall.TestDataFactory.createErrand;

import generated.client.oep_integrator.CaseEnvelope;
import generated.client.oep_integrator.CaseStatus;
import generated.client.oep_integrator.InstanceType;
import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import generated.se.sundsvall.party.PartyType;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.Problem;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.integration.casedata.CaseDataIntegration;
import se.sundsvall.casestatus.integration.casemanagement.CaseManagementIntegration;
import se.sundsvall.casestatus.integration.db.CaseManagementOpeneViewRepository;
import se.sundsvall.casestatus.integration.db.CaseRepository;
import se.sundsvall.casestatus.integration.db.CaseTypeRepository;
import se.sundsvall.casestatus.integration.db.model.CaseEntity;
import se.sundsvall.casestatus.integration.db.model.views.CaseManagementOpeneView;
import se.sundsvall.casestatus.integration.oepintegrator.OepIntegratorClient;
import se.sundsvall.casestatus.integration.party.PartyIntegration;
import se.sundsvall.casestatus.service.mapper.CaseManagementMapper;
import se.sundsvall.casestatus.service.mapper.SupportManagementMapper;

@ExtendWith(MockitoExtension.class)
class CaseStatusServiceTest {

	private static final String EXTERNAL_CASE_ID = "someExternalCaseId";
	private static final String MUNICIPALITY_ID = "2281";
	private static final String NAMESPACE_1 = "namespace1";
	private static final String NAMESPACE_2 = "namespace2";
	private static final InstanceType INSTANCE_TYPE = InstanceType.EXTERNAL;

	@Mock
	private PartyIntegration partyIntegrationMock;

	@Mock
	private CaseManagementIntegration caseManagementIntegrationMock;

	@Mock
	private CaseDataIntegration caseDataIntegrationMock;

	@Mock
	private OepIntegratorClient openEIntegrationMock;

	@Mock
	private CaseRepository caseRepositoryMock;

	@Mock
	private CaseManagementOpeneViewRepository caseManagementOpeneViewRepositoryMock;

	@Mock
	private CaseTypeRepository caseTypeRepositoryMock;

	@Mock
	private SupportManagementService supportManagementServiceMock;

	@Mock
	private CaseManagementMapper caseManagementMapperMock;

	@Mock
	private SupportManagementMapper supportManagementMapperMock;

	@InjectMocks
	private CaseStatusService caseStatusService;

	@Test
	void getOepStatusCaseStatusFoundInCaseManagement() {
		when(caseManagementIntegrationMock.getCaseStatusForExternalId(any(String.class), any(String.class)))
			.thenReturn(Optional.of(new CaseStatusDTO().status("someStatus")));
		when(caseManagementOpeneViewRepositoryMock.findByCaseManagementId("someStatus"))
			.thenReturn(Optional.of(CaseManagementOpeneView.builder().withCaseManagementId("status").withOpenEId("someStatus").build()));

		final var status = caseStatusService.getOepStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID);

		assertThat(status).isNotNull().satisfies(oepStatus -> {
			assertThat(oepStatus.getKey()).isEqualTo("status");
			assertThat(oepStatus.getValue()).isEqualTo("someStatus");
		});

		verify(caseManagementIntegrationMock).getCaseStatusForExternalId(any(String.class), any(String.class));
		verifyNoMoreInteractions(caseManagementIntegrationMock);
		verify(caseManagementOpeneViewRepositoryMock).findByCaseManagementId(any(String.class));
		verifyNoMoreInteractions(caseManagementOpeneViewRepositoryMock);
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
		verifyNoMoreInteractions(caseTypeRepositoryMock, caseManagementIntegrationMock);
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
		when(caseManagementIntegrationMock.getCaseStatusForOrganizationNumber(any(String.class), any(String.class)))
			.thenReturn(List.of(new CaseStatusDTO().status("someStatus"), new CaseStatusDTO().status("someOtherStatus")));

		when(caseManagementMapperMock.toCaseStatusResponse(any(CaseStatusDTO.class), eq(MUNICIPALITY_ID)))
			.thenReturn(CaseStatusResponse.builder().build());

		when(caseRepositoryMock.findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class)))
			.thenReturn(List.of(CaseEntity.builder().build()));

		final var result = caseStatusService.getCaseStatuses("someOrganizationId", MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(3);

		verify(caseManagementIntegrationMock).getCaseStatusForOrganizationNumber(any(String.class), any(String.class));
		verify(caseManagementMapperMock, times(2)).toCaseStatusResponse(any(CaseStatusDTO.class), eq(MUNICIPALITY_ID));
		verify(caseRepositoryMock).findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class));
		verifyNoMoreInteractions(caseManagementIntegrationMock, caseRepositoryMock, caseManagementMapperMock);
	}

	@Test
	void getOepStatusCaseStatusNotFoundInOpenE() {
		when(caseManagementIntegrationMock.getCaseStatusForExternalId(any(String.class), any(String.class)))
			.thenReturn(Optional.of(new CaseStatusDTO().status("someStatus")));
		when(caseManagementOpeneViewRepositoryMock.findByCaseManagementId("someStatus"))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> caseStatusService.getOepStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Could not find matching open-E status for status someStatus");

		verify(caseManagementIntegrationMock).getCaseStatusForExternalId(any(String.class), any(String.class));
		verify(caseManagementOpeneViewRepositoryMock).findByCaseManagementId(any(String.class));
		verifyNoMoreInteractions(caseManagementIntegrationMock, caseManagementOpeneViewRepositoryMock);
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

		final var result = caseStatusService.getCaseStatuses("someOrganizationId", MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(1);

		verify(caseManagementIntegrationMock).getCaseStatusForOrganizationNumber(any(String.class), any(String.class));
		verify(caseManagementMapperMock).toCaseStatusResponse(any(CaseStatusDTO.class), eq(MUNICIPALITY_ID));
		verify(caseRepositoryMock).findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class));
		verifyNoMoreInteractions(caseManagementIntegrationMock, caseRepositoryMock, caseManagementMapperMock);
	}

	/**
	 * Test scenario where one case is found in CaseManagement, one case is found in OpenE and one case is found in
	 * SupportManagement.
	 */
	@Test
	void getPrivateCaseStatuses() {
		final var partyId = "somePartyId";

		final var caseStatus = createCaseStatusDTO(BYGGR);
		final var caseStatuses = List.of(caseStatus);

		final var errand = createErrand();
		final var errands = List.of(errand);
		final var errandMap = Map.of(MUNICIPALITY_ID, errands);

		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID)).thenReturn(caseStatuses);

		when(openEIntegrationMock.getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId)).thenReturn(List.of(new CaseEnvelope().displayName("someTitle").flowInstanceId("someFlowInstanceId")));
		when(openEIntegrationMock.getCaseStatus(MUNICIPALITY_ID, INSTANCE_TYPE, "someFlowInstanceId")).thenReturn(new CaseStatus().name("someStatus"));

		when(supportManagementMapperMock.toCaseStatusResponse(errand, MUNICIPALITY_ID)).thenReturn(createCaseStatusResponse("BYGGR", "1234567890"));
		when(supportManagementServiceMock.getSupportManagementCasesByExternalId(MUNICIPALITY_ID, partyId)).thenReturn(errandMap);

		final var result = caseStatusService.getPrivateCaseStatuses(partyId, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(3);

		verify(caseManagementIntegrationMock).getCaseStatusForPartyId(partyId, MUNICIPALITY_ID);

		verify(openEIntegrationMock).getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId);
		verify(openEIntegrationMock).getCaseStatus(MUNICIPALITY_ID, INSTANCE_TYPE, "someFlowInstanceId");
		verify(supportManagementServiceMock).getSupportManagementCasesByExternalId(MUNICIPALITY_ID, partyId);
		verifyNoMoreInteractions(caseManagementIntegrationMock, openEIntegrationMock, supportManagementServiceMock);
	}

	/**
	 * Test scenario where one case is found in CaseManagement and one case is found in OpenE with a given legalId.
	 */
	@Test
	void getEnterpriseCaseStatuses_1() {
		final var partyId = "somePartyId";

		final var caseStatus = createCaseStatusDTO(BYGGR);
		final var caseStatuses = List.of(caseStatus);
		final var title = "someTitle";

		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID)).thenReturn(caseStatuses);
		when(caseManagementMapperMock.toCaseStatusResponse(caseStatus, MUNICIPALITY_ID)).thenReturn(createCaseStatusResponse("BYGGR", "1234567890"));
		when(openEIntegrationMock.getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId)).thenReturn(List.of(new CaseEnvelope().displayName(title).flowInstanceId("someFlowInstanceId")));
		when(openEIntegrationMock.getCaseStatus(MUNICIPALITY_ID, INSTANCE_TYPE, "someFlowInstanceId")).thenReturn(new CaseStatus().name("someStatus"));

		final var result = caseStatusService.getEnterpriseCaseStatuses(partyId, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(2);

		verify(caseManagementIntegrationMock).getCaseStatusForPartyId(partyId, MUNICIPALITY_ID);
		verify(caseManagementMapperMock).toCaseStatusResponse(caseStatus, MUNICIPALITY_ID);
		verify(openEIntegrationMock).getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId);
		verify(openEIntegrationMock).getCaseStatus(MUNICIPALITY_ID, INSTANCE_TYPE, "someFlowInstanceId");
		verifyNoMoreInteractions(caseManagementIntegrationMock, caseManagementMapperMock, openEIntegrationMock);
	}

	/**
	 * Test scenario where no case is found in CaseManagement and one case is found in OpenE with formatted legalId
	 */
	@Test
	void getEnterpriseCaseStatuses_2() {
		final var partyId = "somePartyId";
		final var title = "someTitle";

		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID)).thenReturn(emptyList());
		when(openEIntegrationMock.getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId)).thenReturn(List.of(new CaseEnvelope().displayName(title).flowInstanceId("someFlowInstanceId")));
		when(openEIntegrationMock.getCaseStatus(MUNICIPALITY_ID, INSTANCE_TYPE, "someFlowInstanceId")).thenReturn(new CaseStatus().name("someStatus"));

		final var result = caseStatusService.getEnterpriseCaseStatuses(partyId, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(1);

		verify(caseManagementIntegrationMock).getCaseStatusForPartyId(partyId, MUNICIPALITY_ID);
		verify(openEIntegrationMock).getCasesByPartyId(MUNICIPALITY_ID, INSTANCE_TYPE, partyId);
		verify(openEIntegrationMock).getCaseStatus(MUNICIPALITY_ID, INSTANCE_TYPE, "someFlowInstanceId");
		verifyNoMoreInteractions(caseManagementIntegrationMock, caseManagementMapperMock, openEIntegrationMock);
	}

	/**
	 * Test scenario where the party id represents an enterprise.
	 */
	@Test
	void getCaseStatusesForParty_1() {
		final var partyId = "somePartyId";
		final var legalId = "1234567890";
		final var partyResult = Map.of(PartyType.ENTERPRISE, legalId);
		final var caseResponses = List.of(createCaseStatusResponse("BYGGR"));
		final var spy = Mockito.spy(caseStatusService);

		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, partyId)).thenReturn(partyResult);
		when(spy.getEnterpriseCaseStatuses(partyId, MUNICIPALITY_ID)).thenReturn(caseResponses);
		when(spy.filterResponses(caseResponses)).thenReturn(caseResponses);

		final var result = spy.getCaseStatusesForParty(partyId, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(1);

		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, partyId);
		verify(spy).getEnterpriseCaseStatuses(partyId, MUNICIPALITY_ID);
		verify(spy).filterResponses(caseResponses);
		verify(spy).getCaseStatusesForParty(partyId, MUNICIPALITY_ID);
		verifyNoMoreInteractions(partyIntegrationMock, spy);
	}

	/**
	 * Test scenario where the party id represents an individual.
	 */
	@Test
	void getCaseStatusesForParty_2() {
		final var partyId = "somePartyId";
		final var legalId = "1234567890";
		final var partyResult = Map.of(PartyType.PRIVATE, legalId);
		final var caseResponses = List.of(createCaseStatusResponse("BYGGR"));
		final var spy = Mockito.spy(caseStatusService);

		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, partyId)).thenReturn(partyResult);
		when(spy.getPrivateCaseStatuses(partyId, MUNICIPALITY_ID)).thenReturn(caseResponses);
		when(spy.filterResponses(caseResponses)).thenReturn(caseResponses);

		final var result = spy.getCaseStatusesForParty(partyId, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(1);

		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, partyId);
		verify(spy).getPrivateCaseStatuses(partyId, MUNICIPALITY_ID);
		verify(spy).filterResponses(caseResponses);
		verify(spy).getCaseStatusesForParty(partyId, MUNICIPALITY_ID);
		verifyNoMoreInteractions(partyIntegrationMock, spy);
	}

	/**
	 * Test scenario where two CaseStatusResponses are filtered. They share the same 'externalCaseId' but different
	 * 'system'. Expects that the response with the 'system' value 'OPEN_E_PLATFORM' is filtered out.
	 */
	@Test
	void filterResponses_1() {
		final var caseResponse1 = createCaseStatusResponse("OPEN_E_PLATFORM", "externalCaseId");
		final var caseResponse2 = createCaseStatusResponse("BYGGR", "externalCaseId");
		final var responses = List.of(caseResponse1, caseResponse2);

		final var result = caseStatusService.filterResponses(responses);

		assertThat(result).isNotNull().containsOnly(caseResponse2);
	}

	/**
	 * Test scenario where two CaseStatusResponses are filtered. They have different externalCaseId's. Expects that both
	 * responses are returned.
	 */
	@Test
	void filterResponses_2() {
		final var caseResponse1 = createCaseStatusResponse("OPEN_E_PLATFORM", "12345");
		final var caseResponse2 = createCaseStatusResponse("BYGGR", "54321");
		final var responses = List.of(caseResponse1, caseResponse2);

		final var result = caseStatusService.filterResponses(responses);

		assertThat(result).isNotNull().containsOnly(caseResponse1, caseResponse2);
	}

	/**
	 * Test scenario where two CaseStatusResponses are filtered. Both have 'null' and 'externalCaseId'. Expects that no
	 * filtering is done.
	 */
	@Test
	void filterResponses_3() {
		final var caseResponse1 = createCaseStatusResponse("OPEN_E_PLATFORM", null);
		final var caseResponse2 = createCaseStatusResponse("BYGGR", null);
		final var responses = List.of(caseResponse1, caseResponse2);

		final var result = caseStatusService.filterResponses(responses);

		assertThat(result).isNotNull().containsOnly(caseResponse1, caseResponse2);
	}

	/**
	 * Test scenario where two OPEN_E_PLATFORM responses are filtered. They share the same 'externalCaseId'. Expects that
	 * both responses are filtered out. 'ExternalCaseId' is unique per instance of Open-E Platform, this scenario should
	 * never happen.
	 */
	@Test
	void filterResponses_4() {
		final var caseResponse1 = createCaseStatusResponse("OPEN_E_PLATFORM", "12345");
		final var caseResponse2 = createCaseStatusResponse("OPEN_E_PLATFORM", "12345");

		final var responses = List.of(caseResponse1, caseResponse2);

		final var result = caseStatusService.filterResponses(responses);

		assertThat(result).isNotNull().isEmpty();
	}

	@Test
	void getErrandStatuses_noRequestParameters() {
		assertThatThrownBy(() -> caseStatusService.getErrandStatuses(MUNICIPALITY_ID, null, null))
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

		final var supportManagementErrand = createErrand();
		when(supportManagementServiceMock.getSupportManagementCases(MUNICIPALITY_ID, "errandNumber:'%s'".formatted(errandNumber)))
			.thenReturn(Map.of("namespace", List.of(supportManagementErrand)));
		when(supportManagementMapperMock.toCaseStatusResponse(supportManagementErrand, "namespace")).thenCallRealMethod();
		when(caseDataIntegrationMock.getNamespaces()).thenReturn(List.of(NAMESPACE_1));
		when(caseDataIntegrationMock.getCaseDataCaseByErrandNumber(MUNICIPALITY_ID, NAMESPACE_1, errandNumber))
			.thenReturn(List.of(createCaseStatusResponse("CASE_DATA", "1234567890")));

		final var result = caseStatusService.getErrandStatuses(MUNICIPALITY_ID, null, errandNumber);

		assertThat(result).isNotNull().hasSize(2);
		assertThat(result.getFirst().getSystem()).isEqualTo("SUPPORT_MANAGEMENT");
		assertThat(result.getLast().getSystem()).isEqualTo("CASE_DATA");

		verify(caseDataIntegrationMock).getCaseDataCaseByErrandNumber(MUNICIPALITY_ID, NAMESPACE_1, errandNumber);
		verify(supportManagementServiceMock).getSupportManagementCases(MUNICIPALITY_ID, "errandNumber:'%s'".formatted(errandNumber));
		verify(supportManagementMapperMock).toCaseStatusResponse(supportManagementErrand, "namespace");
	}

}
