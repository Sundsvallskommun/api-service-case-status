package se.sundsvall.casestatus.service;

import static generated.se.sundsvall.casemanagement.CaseStatusDTO.SystemEnum.BYGGR;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.TestDataFactory.createCaseEntity;
import static se.sundsvall.TestDataFactory.createCaseStatusDTO;
import static se.sundsvall.TestDataFactory.createCaseStatusResponse;
import static se.sundsvall.TestDataFactory.createErrand;
import static se.sundsvall.casestatus.util.FormattingUtil.getFormattedOrganizationNumber;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import generated.se.sundsvall.party.PartyType;
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
import org.zalando.problem.Problem;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.integration.casemanagement.CaseManagementIntegration;
import se.sundsvall.casestatus.integration.db.CaseManagementOpeneViewRepository;
import se.sundsvall.casestatus.integration.db.CaseRepository;
import se.sundsvall.casestatus.integration.db.CaseTypeRepository;
import se.sundsvall.casestatus.integration.db.model.CaseEntity;
import se.sundsvall.casestatus.integration.db.model.views.CaseManagementOpeneView;
import se.sundsvall.casestatus.integration.opene.rest.OpenEIntegration;
import se.sundsvall.casestatus.integration.party.PartyIntegration;
import se.sundsvall.casestatus.service.mapper.CaseManagementMapper;

@ExtendWith(MockitoExtension.class)
class CaseStatusServiceTests {

	private static final String EXTERNAL_CASE_ID = "someExternalCaseId";
	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private PartyIntegration partyIntegrationMock;
	@Mock
	private CaseManagementIntegration caseManagementIntegrationMock;
	@Mock
	private OpenEIntegration openEIntegrationMock;
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
		when(openEIntegrationMock.getPdf(any(String.class))).thenReturn(Optional.of("someBase64String"));

		final var result = caseStatusService.getCasePdf("someExternalCaseID");

		assertThat(result).isNotNull();

		verify(openEIntegrationMock).getPdf(any(String.class));
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
	void getPrivateCaseStatuses_1() {
		var partyId = "somePartyId";
		var legalId = "1234567890";

		var caseStatus = createCaseStatusDTO(BYGGR);
		var caseStatuses = List.of(caseStatus);

		var caseEntity = createCaseEntity();
		var caseEntities = List.of(caseEntity);

		var errand = createErrand();
		var errands = List.of(errand);
		var errandMap = Map.of(MUNICIPALITY_ID, errands);

		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID)).thenReturn(caseStatuses);
		when(openEIntegrationMock.getCaseStatuses(MUNICIPALITY_ID, legalId)).thenReturn(caseEntities);
		when(supportManagementServiceMock.getSupportManagementCases(MUNICIPALITY_ID, "stakeholders.externalId:'%s'".formatted(partyId))).thenReturn(errandMap);

		var result = caseStatusService.getPrivateCaseStatuses(partyId, legalId, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(3);

		verify(caseManagementIntegrationMock).getCaseStatusForPartyId(partyId, MUNICIPALITY_ID);
		verify(openEIntegrationMock).getCaseStatuses(MUNICIPALITY_ID, legalId);
		verify(supportManagementServiceMock).getSupportManagementCases(MUNICIPALITY_ID, "stakeholders.externalId:'%s'".formatted(partyId));
		verifyNoMoreInteractions(caseManagementIntegrationMock, openEIntegrationMock, supportManagementServiceMock);
	}

	/**
	 * Test scenario where one case is found in CaseManagement and one case is found in OpenE with given legalId.
	 */
	@Test
	void getEnterpriseCaseStatuses_1() {
		var partyId = "somePartyId";
		var legalId = "1234567890";

		var caseStatus = createCaseStatusDTO(BYGGR);
		var caseStatuses = List.of(caseStatus);

		var caseEntity = createCaseEntity();
		var caseEntities = List.of(caseEntity);

		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID)).thenReturn(caseStatuses);
		when(caseManagementMapperMock.toCaseStatusResponse(caseStatus, MUNICIPALITY_ID)).thenReturn(createCaseStatusResponse("BYGGR", "1234567890"));
		when(openEIntegrationMock.getCaseStatuses(MUNICIPALITY_ID, legalId)).thenReturn(caseEntities);
		when(openEIntegrationMock.getCaseStatuses(MUNICIPALITY_ID, getFormattedOrganizationNumber(legalId))).thenReturn(emptyList());

		var result = caseStatusService.getEnterpriseCaseStatuses(partyId, legalId, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(2);

		verify(caseManagementIntegrationMock).getCaseStatusForPartyId(partyId, MUNICIPALITY_ID);
		verify(caseManagementMapperMock).toCaseStatusResponse(caseStatus, MUNICIPALITY_ID);
		verify(openEIntegrationMock).getCaseStatuses(MUNICIPALITY_ID, legalId);
		verify(openEIntegrationMock).getCaseStatuses(MUNICIPALITY_ID, getFormattedOrganizationNumber(legalId));
		verifyNoMoreInteractions(caseManagementIntegrationMock, caseManagementMapperMock, openEIntegrationMock);
	}

	/**
	 * Test scenario where no case is found in CaseManagement and one case is found in OpenE with formatted legalId
	 */
	@Test
	void getEnterpriseCaseStatuses_2() {
		var partyId = "somePartyId";
		var legalId = "1234567890";

		var caseEntity = createCaseEntity();
		var caseEntities = List.of(caseEntity);

		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID)).thenReturn(emptyList());
		when(openEIntegrationMock.getCaseStatuses(MUNICIPALITY_ID, legalId)).thenReturn(emptyList());
		when(openEIntegrationMock.getCaseStatuses(MUNICIPALITY_ID, getFormattedOrganizationNumber(legalId))).thenReturn(caseEntities);

		var result = caseStatusService.getEnterpriseCaseStatuses(partyId, legalId, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(1);

		verify(caseManagementIntegrationMock).getCaseStatusForPartyId(partyId, MUNICIPALITY_ID);
		verify(openEIntegrationMock).getCaseStatuses(MUNICIPALITY_ID, legalId);
		verify(openEIntegrationMock).getCaseStatuses(MUNICIPALITY_ID, getFormattedOrganizationNumber(legalId));
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
		when(spy.getEnterpriseCaseStatuses(partyId, legalId, MUNICIPALITY_ID)).thenReturn(caseResponses);
		when(spy.filterResponses(caseResponses)).thenReturn(caseResponses);

		final var result = spy.getCaseStatusesForParty(partyId, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(1);

		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, partyId);
		verify(spy).getEnterpriseCaseStatuses(partyId, legalId, MUNICIPALITY_ID);
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
		when(spy.getPrivateCaseStatuses(partyId, legalId, MUNICIPALITY_ID)).thenReturn(caseResponses);
		when(spy.filterResponses(caseResponses)).thenReturn(caseResponses);

		final var result = spy.getCaseStatusesForParty(partyId, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(1);

		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, partyId);
		verify(spy).getPrivateCaseStatuses(partyId, legalId, MUNICIPALITY_ID);
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
		var caseResponse1 = createCaseStatusResponse("OPEN_E_PLATFORM", "externalCaseId");
		var caseResponse2 = createCaseStatusResponse("BYGGR", "externalCaseId");
		var responses = List.of(caseResponse1, caseResponse2);

		var result = caseStatusService.filterResponses(responses);

		assertThat(result).isNotNull().containsOnly(caseResponse2);
	}

	/**
	 * Test scenario where two CaseStatusResponses are filtered. They have different externalCaseId's. Expects that both
	 * responses are returned.
	 */
	@Test
	void filterResponses_2() {
		var caseResponse1 = createCaseStatusResponse("OPEN_E_PLATFORM", "12345");
		var caseResponse2 = createCaseStatusResponse("BYGGR", "54321");
		var responses = List.of(caseResponse1, caseResponse2);

		var result = caseStatusService.filterResponses(responses);

		assertThat(result).isNotNull().containsOnly(caseResponse1, caseResponse2);
	}

	/**
	 * Test scenario where two CaseStatusResponses are filtered. Both have 'null' 'externalCaseId'. Expects that no
	 * filtering is done.
	 */
	@Test
	void filterResponses_3() {
		var caseResponse1 = createCaseStatusResponse("OPEN_E_PLATFORM", null);
		var caseResponse2 = createCaseStatusResponse("BYGGR", null);
		var responses = List.of(caseResponse1, caseResponse2);

		var result = caseStatusService.filterResponses(responses);

		assertThat(result).isNotNull().containsOnly(caseResponse1, caseResponse2);
	}

	/**
	 * Test scenario where two OPEN_E_PLATFORM responses are filtered. They share the same 'externalCaseId'. Expects that
	 * both responses are filtered out. 'ExternalCaseId' is unique per instance of Open-E Platform, this scenario should
	 * never happen.
	 */
	@Test
	void filterResponses_4() {
		var caseResponse1 = createCaseStatusResponse("OPEN_E_PLATFORM", "12345");
		var caseResponse2 = createCaseStatusResponse("OPEN_E_PLATFORM", "12345");

		var responses = List.of(caseResponse1, caseResponse2);

		var result = caseStatusService.filterResponses(responses);

		assertThat(result).isNotNull().isEmpty();
	}

}
