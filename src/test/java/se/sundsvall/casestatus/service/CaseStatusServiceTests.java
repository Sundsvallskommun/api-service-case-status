package se.sundsvall.casestatus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casestatus.service.CaseStatusService.DATE_TIME_FORMATTER;
import static se.sundsvall.casestatus.service.CaseStatusService.MISSING;

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
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;
import se.sundsvall.casestatus.integration.casemanagement.CaseManagementIntegration;
import se.sundsvall.casestatus.integration.db.CaseManagementOpeneViewRepository;
import se.sundsvall.casestatus.integration.db.CaseRepository;
import se.sundsvall.casestatus.integration.db.CaseTypeRepository;
import se.sundsvall.casestatus.integration.db.model.CaseEntity;
import se.sundsvall.casestatus.integration.db.model.CaseTypeEntity;
import se.sundsvall.casestatus.integration.db.model.views.CaseManagementOpeneView;
import se.sundsvall.casestatus.integration.opene.OpenEIntegration;
import se.sundsvall.casestatus.integration.party.PartyIntegration;

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

		when(caseManagementOpeneViewRepositoryMock.findByCaseManagementId(any(String.class)))
			.thenReturn(Optional.of(CaseManagementOpeneView.builder().withCaseManagementId("someStatus").withOpenEId("someStatus").build()));

		when(caseTypeRepositoryMock.findByEnumValueAndMunicipalityId(any(String.class), any(String.class)))
			.thenReturn(Optional.of(CaseTypeEntity.builder().withEnumValue("PARKING_PERMIT").withDescription("someText").build()));

		final var result = caseStatusService.getCaseStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo("someCaseId");
		assertThat(result.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
		assertThat(result.getCaseType()).isEqualTo("someText");
		assertThat(result.getLastStatusChange()).isEqualTo(DATE_TIME_FORMATTER.format(caseStatus.getTimestamp()));
		assertThat(result.getFirstSubmitted()).isEqualTo(MISSING);
		assertThat(result.isOpenEErrand()).isFalse();

		verify(caseManagementIntegrationMock).getCaseStatusForExternalId(any(String.class), any(String.class));
		verify(caseManagementOpeneViewRepositoryMock).findByCaseManagementId(any(String.class));
		verify(caseTypeRepositoryMock).findByEnumValueAndMunicipalityId(any(String.class), any(String.class));
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
		assertThat(result.isOpenEErrand()).isTrue();

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

		when(caseManagementOpeneViewRepositoryMock.findByCaseManagementId(any(String.class)))
			.thenReturn(Optional.of(CaseManagementOpeneView.builder().withCaseManagementId("someStatus").withOpenEId("someResolvedStatus").build()))
			.thenReturn(Optional.of(CaseManagementOpeneView.builder().withCaseManagementId("someOtherStatus").withOpenEId("someOtherResolvedStatus").build()));

		when(caseRepositoryMock.findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class)))
			.thenReturn(List.of(CaseEntity.builder().build()));

		final var result = caseStatusService.getCaseStatuses("someOrganizationId", MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(3);

		verify(caseManagementIntegrationMock).getCaseStatusForOrganizationNumber(any(String.class), any(String.class));
		verify(caseRepositoryMock).findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class));
		verify(caseManagementOpeneViewRepositoryMock, times(2)).findByCaseManagementId(any(String.class));
		verifyNoMoreInteractions(caseManagementIntegrationMock, caseRepositoryMock, caseManagementOpeneViewRepositoryMock);
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
		when(caseManagementOpeneViewRepositoryMock.findByCaseManagementId(any(String.class)))
			.thenReturn(Optional.of(CaseManagementOpeneView.builder().withCaseManagementId("someStatus").withOpenEId("someResolvedStatus").build()));
		when(caseRepositoryMock.findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class)))
			.thenReturn(List.of());

		final var result = caseStatusService.getCaseStatuses("someOrganizationId", MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(1);

		verify(caseManagementIntegrationMock).getCaseStatusForOrganizationNumber(any(String.class), any(String.class));
		verify(caseRepositoryMock).findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class));
		verify(caseManagementOpeneViewRepositoryMock).findByCaseManagementId(any(String.class));
		verifyNoMoreInteractions(caseManagementIntegrationMock, caseRepositoryMock, caseManagementOpeneViewRepositoryMock);
	}

	@Test
	void getCaseStatusesForPartyPrivate() {
		final var partyId = "somePartyId";
		final var legalId = "someLegalId";
		final var municipalityId = "someMunicipalityId";

		when(partyIntegrationMock.getLegalIdByPartyId(municipalityId, partyId)).thenReturn(Map.of(PartyType.PRIVATE, legalId));

		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, municipalityId))
			.thenReturn(List.of(new CaseStatusDTO().externalCaseId("3").status("someStatus"), new CaseStatusDTO().externalCaseId("4").status("someOtherStatus")));

		when(caseRepositoryMock.findByPersonIdAndMunicipalityId(partyId, municipalityId))
			.thenReturn(List.of(CaseEntity.builder().withFlowInstanceId("1").build()));

		when(openEIntegrationMock.getCaseStatuses(municipalityId, legalId))
			.thenReturn(List.of(CaseEntity.builder().withFlowInstanceId("2").build()));

		final var result = caseStatusService.getCaseStatusesForParty(partyId, municipalityId);

		assertThat(result).isNotNull().hasSize(4);

		verify(partyIntegrationMock).getLegalIdByPartyId(municipalityId, partyId);
		verify(caseManagementIntegrationMock).getCaseStatusForPartyId(partyId, municipalityId);
		verify(caseRepositoryMock).findByPersonIdAndMunicipalityId(partyId, municipalityId);
		verify(openEIntegrationMock).getCaseStatuses(municipalityId, legalId);
		verifyNoMoreInteractions(partyIntegrationMock, caseManagementIntegrationMock, caseRepositoryMock, openEIntegrationMock);
	}

	@Test
	void getCaseStatusesForPartyPrivate_withFilter() {
		final var partyId = "somePartyId";
		final var legalId = "someLegalId";
		final var municipalityId = "someMunicipalityId";

		when(partyIntegrationMock.getLegalIdByPartyId(municipalityId, partyId)).thenReturn(Map.of(PartyType.PRIVATE, legalId));
		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, municipalityId))
			.thenReturn(List.of(new CaseStatusDTO().status("someStatus")));
		when(caseRepositoryMock.findByPersonIdAndMunicipalityId(partyId, municipalityId))
			.thenReturn(List.of(
				CaseEntity.builder().withFlowInstanceId("1").withLastStatusChange("2023-01-01 10:00").build(),
				CaseEntity.builder().withFlowInstanceId("1").withLastStatusChange("2023-01-01 12:00").build()));
		when(openEIntegrationMock.getCaseStatuses(municipalityId, legalId))
			.thenReturn(List.of(
				CaseEntity.builder().withFlowInstanceId("2").withLastStatusChange("2023-01-01 11:00").build(),
				CaseEntity.builder().withFlowInstanceId("2").withLastStatusChange("2023-01-01 09:00").build()));

		final var result = caseStatusService.getCaseStatusesForParty(partyId, municipalityId);

		assertThat(result).isNotNull().hasSize(2);
		assertThat(result).extracting("externalCaseId").containsExactlyInAnyOrder("1", "2");
		assertThat(result).extracting("lastStatusChange").containsExactlyInAnyOrder("2023-01-01 12:00", "2023-01-01 11:00");

		verify(partyIntegrationMock).getLegalIdByPartyId(municipalityId, partyId);
		verify(caseManagementIntegrationMock).getCaseStatusForPartyId(partyId, municipalityId);
		verify(caseRepositoryMock).findByPersonIdAndMunicipalityId(partyId, municipalityId);
		verify(openEIntegrationMock).getCaseStatuses(municipalityId, legalId);
		verifyNoMoreInteractions(partyIntegrationMock, caseManagementIntegrationMock, caseRepositoryMock, openEIntegrationMock);
	}

	@Test
	void getCaseStatusesForPartyEnterprise() {
		final var partyId = "somePartyId";

		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, partyId)).thenReturn(Map.of(PartyType.ENTERPRISE, "someLegalId"));
		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID))
			.thenReturn(List.of(new CaseStatusDTO().status("someStatus")));

		final var result = caseStatusService.getCaseStatusesForParty(partyId, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(1);

		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, partyId);
		verify(caseManagementIntegrationMock).getCaseStatusForPartyId(partyId, MUNICIPALITY_ID);
		verify(caseRepositoryMock).findByOrganisationNumberAndMunicipalityId("someLegalId", MUNICIPALITY_ID);
		verifyNoMoreInteractions(partyIntegrationMock, caseManagementIntegrationMock);
	}

	@Test
	void getCaseStatusesForPartyNoPrivateOrEnterprise() {
		final var partyId = "somePartyId";

		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, partyId)).thenReturn(Map.of());
		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID))
			.thenReturn(List.of(new CaseStatusDTO().status("someStatus")));

		final var result = caseStatusService.getCaseStatusesForParty(partyId, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(1);

		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, partyId);
		verify(caseManagementIntegrationMock).getCaseStatusForPartyId(partyId, MUNICIPALITY_ID);
		verifyNoMoreInteractions(partyIntegrationMock, caseManagementIntegrationMock);
	}
}
