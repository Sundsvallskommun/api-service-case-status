package se.sundsvall.casestatus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import se.sundsvall.casestatus.integration.db.CaseTypeRepository;
import se.sundsvall.casestatus.integration.db.CompanyRepository;
import se.sundsvall.casestatus.integration.db.PrivateRepository;
import se.sundsvall.casestatus.integration.db.UnknownRepository;
import se.sundsvall.casestatus.integration.db.model.CaseTypeEntity;
import se.sundsvall.casestatus.integration.db.model.CompanyEntity;
import se.sundsvall.casestatus.integration.db.model.PrivateEntity;
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
	private CompanyRepository companyRepositoryMock;
	@Mock
	private PrivateRepository privateRepositoryMock;
	@Mock
	private UnknownRepository unknownRepositoryMock;
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
			.thenReturn(Optional.ofNullable(CaseManagementOpeneView.builder().withCaseManagementId("status").withOpenEId("someStatus").build()));

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

		// Arrange
		when(caseManagementIntegrationMock.getCaseStatusForExternalId(any(String.class), any(String.class)))
			.thenReturn(Optional.empty());

		// Act & Assert
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

		when(caseManagementOpeneViewRepositoryMock.findByCaseManagementId(any(String.class))).thenReturn(Optional.of(CaseManagementOpeneView.builder().withCaseManagementId("someStatus").withOpenEId("someStatus").build()));

		when(caseTypeRepositoryMock.findByEnumValueAndMunicipalityId(any(String.class), any(String.class))).thenReturn(Optional.of(
			CaseTypeEntity.builder().withEnumValue("PARKING_PERMIT").withDescription("someText").build()));

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
		final var companyEntity = CompanyEntity.builder()
			.withFlowInstanceId("someFlowInstanceId")
			.withErrandType("someErrandType")
			.withStatus("someStatus")
			.withFirstSubmitted("someFirstSubmittedValue")
			.withLastStatusChange("someLastStatusChangeValue")
			.build();

		when(caseManagementIntegrationMock.getCaseStatusForExternalId(any(String.class), any(String.class)))
			.thenReturn(Optional.empty());
		when(companyRepositoryMock.findByFlowInstanceIdAndMunicipalityId(any(String.class), any(String.class))).thenReturn(Optional.ofNullable(companyEntity));

		final var result = caseStatusService.getCaseStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo("someFlowInstanceId");
		assertThat(result.getExternalCaseId()).isNull();
		assertThat(result.getCaseType()).isEqualTo("someErrandType");
		assertThat(result.getFirstSubmitted()).isEqualTo("someFirstSubmittedValue");
		assertThat(result.getLastStatusChange()).isEqualTo("someLastStatusChangeValue");
		assertThat(result.isOpenEErrand()).isTrue();

		verify(caseManagementIntegrationMock).getCaseStatusForExternalId(any(String.class), any(String.class));
		verify(companyRepositoryMock).findByFlowInstanceIdAndMunicipalityId(any(String.class), any(String.class));
		verifyNoMoreInteractions(caseManagementIntegrationMock, companyRepositoryMock);
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
			.thenReturn(List.of(new CaseStatusDTO().status("someStatus"),
				new CaseStatusDTO().status("someOtherStatus")));

		when(caseManagementOpeneViewRepositoryMock.findByCaseManagementId(any(String.class)))
			.thenReturn(Optional.of(
				CaseManagementOpeneView.builder().withCaseManagementId("someStatus").withOpenEId("someResolvedStatus").build()))
			.thenReturn(Optional.of(
				CaseManagementOpeneView.builder().withCaseManagementId("someOtherStatus").withOpenEId("someOtherResolvedStatus").build()));

		when(companyRepositoryMock.findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class)))
			.thenReturn(List.of(CompanyEntity.builder().build()));

		final var result = caseStatusService.getCaseStatuses("someOrganizationId", MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(3);

		verify(caseManagementIntegrationMock).getCaseStatusForOrganizationNumber(any(String.class), any(String.class));
		verify(companyRepositoryMock).findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class));
		verify(caseManagementOpeneViewRepositoryMock, times(2)).findByCaseManagementId(any(String.class));
		verifyNoMoreInteractions(caseManagementIntegrationMock, companyRepositoryMock, caseManagementOpeneViewRepositoryMock);
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
		when(companyRepositoryMock.findByFlowInstanceIdAndMunicipalityId(any(String.class), any(String.class)))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> caseStatusService.getCaseStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.isInstanceOf(Problem.class)
			.hasMessage("Not Found: Case with id someExternalCaseId not found");

		verify(caseManagementIntegrationMock).getCaseStatusForExternalId(any(String.class), any(String.class));
		verify(companyRepositoryMock).findByFlowInstanceIdAndMunicipalityId(any(String.class), any(String.class));
		verifyNoMoreInteractions(caseManagementIntegrationMock, companyRepositoryMock);
	}

	@Test
	void getCaseStatuses_companyRepositoryNotFound() {
		when(caseManagementIntegrationMock.getCaseStatusForOrganizationNumber(any(String.class), any(String.class)))
			.thenReturn(List.of(new CaseStatusDTO().status("someStatus")));
		when(caseManagementOpeneViewRepositoryMock.findByCaseManagementId(any(String.class)))
			.thenReturn(Optional.of(CaseManagementOpeneView.builder().withCaseManagementId("someStatus").withOpenEId("someResolvedStatus").build()));
		when(companyRepositoryMock.findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class)))
			.thenReturn(List.of());

		final var result = caseStatusService.getCaseStatuses("someOrganizationId", MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(1);

		verify(caseManagementIntegrationMock).getCaseStatusForOrganizationNumber(any(String.class), any(String.class));
		verify(companyRepositoryMock).findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class));
		verify(caseManagementOpeneViewRepositoryMock).findByCaseManagementId(any(String.class));
		verifyNoMoreInteractions(caseManagementIntegrationMock, companyRepositoryMock, caseManagementOpeneViewRepositoryMock);
	}

	@Test
	void getCaseStatusesForPartyPrivate() {

		// Arrange
		final var partyId = "somePartyId";
		final var legalId = "someLegalId";

		when(partyIntegrationMock.getLegalIdByPartyId(MUNICIPALITY_ID, partyId)).thenReturn(Map.of(PartyType.PRIVATE, legalId));

		when(caseManagementIntegrationMock.getCaseStatusForPartyId(partyId, MUNICIPALITY_ID))
			.thenReturn(List.of(new CaseStatusDTO().status("someStatus"),
				new CaseStatusDTO().status("someOtherStatus")));

		when(privateRepositoryMock.findByPersonIdAndMunicipalityId(partyId, MUNICIPALITY_ID))
			.thenReturn(List.of(PrivateEntity.builder().build()));

		// Act
		final var result = caseStatusService.getCaseStatusesForParty(partyId, MUNICIPALITY_ID);

		// Assert
		assertThat(result).isNotNull().hasSize(3);

		verify(partyIntegrationMock).getLegalIdByPartyId(MUNICIPALITY_ID, partyId);
		verify(caseManagementIntegrationMock).getCaseStatusForPartyId(partyId, MUNICIPALITY_ID);
		verify(privateRepositoryMock).findByPersonIdAndMunicipalityId(partyId, MUNICIPALITY_ID);
		verifyNoMoreInteractions(partyIntegrationMock, caseManagementIntegrationMock, privateRepositoryMock);
		verifyNoInteractions(companyRepositoryMock);

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
		verify(companyRepositoryMock).findByOrganisationNumberAndMunicipalityId("someLegalId", MUNICIPALITY_ID);
		verifyNoMoreInteractions(partyIntegrationMock, caseManagementIntegrationMock);
		verifyNoInteractions(privateRepositoryMock);
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
		verifyNoInteractions(companyRepositoryMock, privateRepositoryMock);
	}

}
