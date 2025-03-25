package se.sundsvall.casestatus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.TestDataFactory.createCaseStatusResponse;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import generated.se.sundsvall.party.PartyType;
import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
			.status("someStatus");

		when(caseManagementIntegrationMock.getCaseStatusForExternalId(any(String.class), any(String.class)))
			.thenReturn(Optional.of(caseStatus));

		when(caseManagementMapperMock.toCaseStatusResponse(caseStatus, MUNICIPALITY_ID))
			.thenReturn(CaseStatusResponse.builder().withCaseId("someCaseId").withExternalCaseId(EXTERNAL_CASE_ID).withCaseType("PARKING_PERMIT").build());

		final var result = caseStatusService.getCaseStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID);

		assertThat(result).isNotNull();
		assertThat(result.getCaseId()).isEqualTo("someCaseId");
		assertThat(result.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
		assertThat(result.getCaseType()).isEqualTo("PARKING_PERMIT");

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

	@Test
	void getCaseStatusesForPartyPrivate() {
		final var partyId = "somePartyId";
		final var legalId = "someLegalId";
		final var dto1 = new CaseStatusDTO().externalCaseId("3").status("someStatus");
		final var dto2 = new CaseStatusDTO().externalCaseId("4").status("someOtherStatus");

		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, partyId)).thenReturn(Map.of(PartyType.PRIVATE, legalId));

		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID))
			.thenReturn(List.of(dto1, dto2));

		when(caseManagementMapperMock.toCaseStatusResponse(any(CaseStatusDTO.class), eq(MUNICIPALITY_ID)))
			.thenReturn(CaseStatusResponse.builder().build())
			.thenReturn(CaseStatusResponse.builder().build());

		when(caseRepositoryMock.findByPersonIdAndMunicipalityId(partyId, MUNICIPALITY_ID))
			.thenReturn(List.of(CaseEntity.builder().withFlowInstanceId("1").build()));

		when(openEIntegrationMock.getCaseStatuses(MUNICIPALITY_ID, legalId))
			.thenReturn(List.of(CaseEntity.builder().withFlowInstanceId("2").build()));

		when(supportManagementServiceMock.getSupportManagementCases(any(String.class), any(String.class)))
			.thenReturn(Map.of("namespace", List.of(new Errand()
				.id("someErrandId")
				.modified(OffsetDateTime.now())
				.created(OffsetDateTime.now().minusDays(1))
				.classification(new Classification().type("someType"))
				.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
				.addExternalTagsItem(new ExternalTag().key("caseId").value("5"))
				.status("someStatus"))));

		final var result = caseStatusService.getCaseStatusesForParty(partyId, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(5);

		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, partyId);
		verify(caseManagementIntegrationMock).getCaseStatusForPartyId(partyId, MUNICIPALITY_ID);
		verify(caseManagementMapperMock, times(2)).toCaseStatusResponse(any(CaseStatusDTO.class), eq(MUNICIPALITY_ID));
		verify(caseRepositoryMock).findByPersonIdAndMunicipalityId(partyId, MUNICIPALITY_ID);
		verify(openEIntegrationMock).getCaseStatuses(MUNICIPALITY_ID, legalId);
		verify(supportManagementServiceMock).getSupportManagementCases(any(String.class), any(String.class));
		verifyNoMoreInteractions(partyIntegrationMock, caseManagementIntegrationMock, caseRepositoryMock, openEIntegrationMock, supportManagementServiceMock, caseManagementMapperMock);
	}

	@Test
	void getCaseStatusesForPartyPrivate_withFilter() {
		final var partyId = "somePartyId";
		final var legalId = "someLegalId";
		final var lastStatusChange = OffsetDateTime.parse("2025-01-10T14:24:00Z");
		final var caseStatusDto = new CaseStatusDTO().externalCaseId("1").status("someStatus");

		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, partyId)).thenReturn(Map.of(PartyType.PRIVATE, legalId));
		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID))
			.thenReturn(List.of(caseStatusDto));

		when(caseManagementMapperMock.toCaseStatusResponse(caseStatusDto, MUNICIPALITY_ID))
			.thenReturn(CaseStatusResponse.builder().withExternalCaseId("1").withStatus("someStatus").build());

		when(caseRepositoryMock.findByPersonIdAndMunicipalityId(partyId, MUNICIPALITY_ID))
			.thenReturn(List.of(
				CaseEntity.builder().withFlowInstanceId("1").withLastStatusChange("2023-01-01 10:00").build(),
				CaseEntity.builder().withFlowInstanceId("1").withLastStatusChange("2023-01-01 12:00").build()));

		when(openEIntegrationMock.getCaseStatuses(MUNICIPALITY_ID, legalId))
			.thenReturn(List.of(
				CaseEntity.builder().withFlowInstanceId("2").withLastStatusChange("2023-01-01 11:00").build(),
				CaseEntity.builder().withFlowInstanceId("2").withLastStatusChange("2023-01-01 09:00").build()));

		when(supportManagementServiceMock.getSupportManagementCases(any(String.class), any(String.class)))
			.thenReturn(Map.of("namespace", List.of(new Errand()
				.id("someErrandId")
				.modified(lastStatusChange)
				.created(OffsetDateTime.now().minusDays(1))
				.classification(new Classification().type("someType"))
				.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
				.addExternalTagsItem(new ExternalTag().key("caseId").value("5"))
				.status("someStatus"))));

		final var result = caseStatusService.getCaseStatusesForParty(partyId, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(3);
		assertThat(result).extracting("externalCaseId").containsExactlyInAnyOrder("1", "2", "5");
		assertThat(result).extracting("lastStatusChange").containsExactlyInAnyOrder("2023-01-01 12:00", "2023-01-01 11:00", "2025-01-10 14:24");

		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, partyId);
		verify(caseManagementIntegrationMock).getCaseStatusForPartyId(partyId, MUNICIPALITY_ID);
		verify(caseRepositoryMock).findByPersonIdAndMunicipalityId(partyId, MUNICIPALITY_ID);
		verify(openEIntegrationMock).getCaseStatuses(MUNICIPALITY_ID, legalId);
		verify(supportManagementServiceMock).getSupportManagementCases(any(String.class), any(String.class));
		verifyNoMoreInteractions(partyIntegrationMock, caseManagementIntegrationMock, caseManagementMapperMock, caseRepositoryMock, openEIntegrationMock, supportManagementServiceMock);
	}

	/**
	 * Test scenario where the party id represents an enterprise.
	 */
	@Test
	void getCaseStatusesForParty_1() {
		final var partyId = "somePartyId";
		final var legalId = "1234567890";
		final var partyResult = Map.of(PartyType.ENTERPRISE, legalId);
		final var caseResponses = List.of(createCaseStatusResponse(CaseStatusDTO.SystemEnum.BYGGR));
		final var spy = Mockito.spy(caseStatusService);

		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, partyId)).thenReturn(partyResult);
		when(spy.getEnterpriseCaseStatuses(partyId, partyResult, MUNICIPALITY_ID)).thenReturn(caseResponses);
		when(spy.filterResponse(caseResponses)).thenReturn(caseResponses);

		final var result = spy.getCaseStatusesForParty(partyId, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(1);

		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, partyId);
		verify(spy).getEnterpriseCaseStatuses(partyId, partyResult, MUNICIPALITY_ID);
		verify(spy).filterResponse(caseResponses);
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
		final var caseResponses = List.of(createCaseStatusResponse(CaseStatusDTO.SystemEnum.BYGGR));
		final var spy = Mockito.spy(caseStatusService);

		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, partyId)).thenReturn(partyResult);
		when(spy.getPrivateCaseStatuses(partyId, partyResult, MUNICIPALITY_ID)).thenReturn(caseResponses);
		when(spy.filterResponse(caseResponses)).thenReturn(caseResponses);

		final var result = spy.getCaseStatusesForParty(partyId, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(1);

		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, partyId);
		verify(spy).getPrivateCaseStatuses(partyId, partyResult, MUNICIPALITY_ID);
		verify(spy).filterResponse(caseResponses);
		verify(spy).getCaseStatusesForParty(partyId, MUNICIPALITY_ID);
		verifyNoMoreInteractions(partyIntegrationMock, spy);
	}
}
