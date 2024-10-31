package se.sundsvall.casestatus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casestatus.integration.casemanagement.CaseManagementIntegration;
import se.sundsvall.casestatus.integration.db.CaseManagementOpeneViewRepository;
import se.sundsvall.casestatus.integration.db.CaseTypeRepository;
import se.sundsvall.casestatus.integration.db.CompanyRepository;
import se.sundsvall.casestatus.integration.db.IncidentOpeneViewRepository;
import se.sundsvall.casestatus.integration.db.PrivateRepository;
import se.sundsvall.casestatus.integration.db.UnknownRepository;
import se.sundsvall.casestatus.integration.db.model.CaseTypeEntity;
import se.sundsvall.casestatus.integration.db.model.CompanyEntity;
import se.sundsvall.casestatus.integration.db.model.views.CaseManagementOpeneView;
import se.sundsvall.casestatus.integration.db.model.views.IncidentOpeneView;
import se.sundsvall.casestatus.integration.incident.IncidentIntegration;
import se.sundsvall.casestatus.integration.opene.OpenEIntegration;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import generated.se.sundsvall.incident.IncidentOepResponse;

@ExtendWith(MockitoExtension.class)
class CaseStatusServiceTests {

	private static final String EXTERNAL_CASE_ID = "someExternalCaseId";

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private CaseManagementIntegration mockCaseManagementIntegration;

	@Mock
	private IncidentIntegration mockIncidentIntegration;

	@Mock
	private OpenEIntegration mockOpenEIntegration;

	@Mock
	private CompanyRepository companyRepositoryMock;

	@Mock
	private PrivateRepository privateRepositoryMock;

	@Mock
	private UnknownRepository unknownRepositoryMock;

	@Mock
	private CaseManagementOpeneViewRepository caseManagementOpeneViewRepositoryMock;

	@Mock
	private IncidentOpeneViewRepository incidentOpeneViewRepositoryMock;

	@Mock
	private CaseTypeRepository caseTypeRepositoryMock;

	@InjectMocks
	private CaseStatusService caseStatusService;

	@Test
	void getOepStatus_caseStatusFoundInCaseManagement() {
		when(mockCaseManagementIntegration.getCaseStatusForExternalId(any(String.class), any(String.class)))
			.thenReturn(Optional.of(new CaseStatusDTO().status("someStatus")));
		when(caseManagementOpeneViewRepositoryMock.findByCaseManagementId("someStatus"))
			.thenReturn(Optional.ofNullable(CaseManagementOpeneView.builder().withCaseManagementId("status").withOpenEId("someStatus").build()));

		final var status = caseStatusService.getOepStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID);

		assertThat(status).isNotNull().satisfies(oepStatus -> {
			assertThat(oepStatus.getKey()).isEqualTo("status");
			assertThat(oepStatus.getValue()).isEqualTo("someStatus");
		});

		verify(mockCaseManagementIntegration).getCaseStatusForExternalId(any(String.class), any(String.class));
		verifyNoMoreInteractions(mockCaseManagementIntegration);
		verify(caseManagementOpeneViewRepositoryMock).findByCaseManagementId(any(String.class));
		verifyNoMoreInteractions(caseManagementOpeneViewRepositoryMock);
	}

	@Test
	void getOepStatus_caseStatusNotFoundInCaseManagement() {

		// Arrange
		when(mockCaseManagementIntegration.getCaseStatusForExternalId(any(String.class), any(String.class)))
			.thenReturn(Optional.empty());

		when(mockIncidentIntegration.getIncidentStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID))
			.thenReturn(Optional.of(new IncidentOepResponse().statusId(678)));

		when(incidentOpeneViewRepositoryMock.findByIncidentId(678))
			.thenReturn(Optional.of(IncidentOpeneView.builder().withIncidentId(678).withOpenEId("someStatus").build()));

		// Act
		final var status = caseStatusService.getOepStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID);

		// Assert
		assertThat(status).isNotNull().satisfies(oepStatus -> {
			assertThat(oepStatus.getKey()).isEqualTo("status");
			assertThat(oepStatus.getValue()).isEqualTo("someStatus");
		});

		verify(mockCaseManagementIntegration).getCaseStatusForExternalId(any(String.class), any(String.class));
		verify(mockIncidentIntegration).getIncidentStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID);
		verify(incidentOpeneViewRepositoryMock).findByIncidentId(any(Integer.class));
		verifyNoMoreInteractions(mockCaseManagementIntegration, incidentOpeneViewRepositoryMock, mockIncidentIntegration);
	}

	@Test
	void getCaseStatus_caseStatusFoundInCaseManagement() {
		final var caseStatus = new CaseStatusDTO()
			.caseId("someCaseId")
			.externalCaseId(EXTERNAL_CASE_ID)
			.caseType("PARKING_PERMIT")
			.timestamp(LocalDateTime.now())
			.status("someStatus");

		when(mockCaseManagementIntegration.getCaseStatusForExternalId(any(String.class), any(String.class)))
			.thenReturn(Optional.of(caseStatus));

		when(caseManagementOpeneViewRepositoryMock.findByCaseManagementId(any(String.class))).thenReturn(Optional.of(CaseManagementOpeneView.builder().withCaseManagementId("someStatus").withOpenEId("someStatus").build()));

		when(caseTypeRepositoryMock.findByEnumValueAndMunicipalityId(any(String.class), any(String.class))).thenReturn(Optional.of(
			CaseTypeEntity.builder().withEnumValue("PARKING_PERMIT").withDescription("someText").build()));

		final var result = caseStatusService.getCaseStatus(EXTERNAL_CASE_ID, MUNICIPALITY_ID);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo("someCaseId");
		assertThat(result.getExternalCaseId()).isEqualTo(EXTERNAL_CASE_ID);
		assertThat(result.getCaseType()).isEqualTo("someText");
		assertThat(result.getLastStatusChange()).isEqualTo(CaseStatusService.DATE_TIME_FORMATTER.format(caseStatus.getTimestamp()));
		assertThat(result.getFirstSubmitted()).isEqualTo(CaseStatusService.MISSING);
		assertThat(result.isOpenEErrand()).isFalse();

		verify(mockCaseManagementIntegration).getCaseStatusForExternalId(any(String.class), any(String.class));
		verify(caseManagementOpeneViewRepositoryMock).findByCaseManagementId(any(String.class));
		verify(caseTypeRepositoryMock).findByEnumValueAndMunicipalityId(any(String.class), any(String.class));
		verifyNoMoreInteractions(caseTypeRepositoryMock, mockCaseManagementIntegration);
	}

	@Test
	void getCaseStatus_caseStatusNotFoundInCaseManagement() {
		final var companyEntity = CompanyEntity.builder()
			.withFlowInstanceId("someFlowInstanceId")
			.withErrandType("someErrandType")
			.withStatus("someStatus")
			.withFirstSubmitted("someFirstSubmittedValue")
			.withLastStatusChange("someLastStatusChangeValue")
			.build();

		when(mockCaseManagementIntegration.getCaseStatusForExternalId(any(String.class), any(String.class)))
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

		verify(mockCaseManagementIntegration).getCaseStatusForExternalId(any(String.class), any(String.class));
		verify(companyRepositoryMock).findByFlowInstanceIdAndMunicipalityId(any(String.class), any(String.class));
		verifyNoMoreInteractions(mockCaseManagementIntegration, companyRepositoryMock);
	}

	@Test
	void getCasePdf() {
		when(mockOpenEIntegration.getPdf(any(String.class))).thenReturn(Optional.of("someBase64String"));

		final var result = caseStatusService.getCasePdf("someExternalCaseID", MUNICIPALITY_ID);

		assertThat(result).isNotNull();

		verify(mockOpenEIntegration).getPdf(any(String.class));
		verifyNoMoreInteractions(mockOpenEIntegration);
	}

	@Test
	void getCaseStatuses() {

		when(mockCaseManagementIntegration.getCaseStatusForOrganizationNumber(any(String.class), any(String.class)))
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

		verify(mockCaseManagementIntegration).getCaseStatusForOrganizationNumber(any(String.class), any(String.class));
		verify(companyRepositoryMock).findByOrganisationNumberAndMunicipalityId(any(String.class), any(String.class));
		verify(caseManagementOpeneViewRepositoryMock, times(2)).findByCaseManagementId(any(String.class));
		verifyNoMoreInteractions(mockCaseManagementIntegration, companyRepositoryMock, caseManagementOpeneViewRepositoryMock);
	}

}
