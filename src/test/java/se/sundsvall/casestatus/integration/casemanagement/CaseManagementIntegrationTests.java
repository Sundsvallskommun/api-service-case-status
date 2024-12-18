package se.sundsvall.casestatus.integration.casemanagement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;

@ExtendWith(MockitoExtension.class)
class CaseManagementIntegrationTests {

	private static final String MUNICIPALITY_ID = "2281";

	private static final String ORGANIZATION_NUMBER = "someOrganizationNumber";

	private static final String EXTERNAL_CASE_ID = "someExternalCaseId";

	@Mock
	private CaseManagementClient mockCaseManagementClient;

	@InjectMocks
	private CaseManagementIntegration caseManagementIntegration;

	@Test
	void getCaseStatusForExternalCaseId_ok() {
		final var caseStatus = new CaseStatusDTO().externalCaseId(EXTERNAL_CASE_ID);

		when(mockCaseManagementClient.getCaseStatusForExternalCaseId(MUNICIPALITY_ID, EXTERNAL_CASE_ID)).thenReturn(caseStatus);

		final var result = caseManagementIntegration.getCaseStatusForExternalId(EXTERNAL_CASE_ID, MUNICIPALITY_ID);

		assertThat(result).isNotNull().isPresent();

		verify(mockCaseManagementClient).getCaseStatusForExternalCaseId(MUNICIPALITY_ID, EXTERNAL_CASE_ID);
		verifyNoMoreInteractions(mockCaseManagementClient);
	}

	@Test
	void getCaseStatusForExternalCaseId_error() {
		when(mockCaseManagementClient.getCaseStatusForExternalCaseId(MUNICIPALITY_ID, EXTERNAL_CASE_ID))
			.thenThrow(Problem.builder().build());

		final var result = caseManagementIntegration.getCaseStatusForExternalId(EXTERNAL_CASE_ID, MUNICIPALITY_ID);

		assertThat(result).isNotNull().isNotPresent();

		verify(mockCaseManagementClient).getCaseStatusForExternalCaseId(MUNICIPALITY_ID, EXTERNAL_CASE_ID);
		verifyNoMoreInteractions(mockCaseManagementClient);
	}

	@Test
	void getCaseStatusForOrganizationNumber_ok() {
		when(mockCaseManagementClient.getCaseStatusForOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER))
			.thenReturn(List.of(new CaseStatusDTO(), new CaseStatusDTO(), new CaseStatusDTO()));

		final var result = caseManagementIntegration.getCaseStatusForOrganizationNumber(ORGANIZATION_NUMBER, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(3);

		verify(mockCaseManagementClient).getCaseStatusForOrganizationNumber(any(String.class), any(String.class));
		verifyNoMoreInteractions(mockCaseManagementClient);
	}

	@Test
	void getCaseStatusForOrganizationNumber_error() {
		when(mockCaseManagementClient.getCaseStatusForOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER))
			.thenThrow(Problem.builder().build());

		final var result = caseManagementIntegration.getCaseStatusForOrganizationNumber(ORGANIZATION_NUMBER, MUNICIPALITY_ID);

		assertThat(result).isNotNull().isEmpty();

		verify(mockCaseManagementClient).getCaseStatusForOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verifyNoMoreInteractions(mockCaseManagementClient);
	}

	@Test
	void getCaseStatusForPartyId_ok() {
		when(mockCaseManagementClient.getCaseStatusForPartyId(MUNICIPALITY_ID, ORGANIZATION_NUMBER))
			.thenReturn(List.of(new CaseStatusDTO(), new CaseStatusDTO(), new CaseStatusDTO()));

		final var result = caseManagementIntegration.getCaseStatusForPartyId(ORGANIZATION_NUMBER, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(3);

		verify(mockCaseManagementClient).getCaseStatusForPartyId(any(String.class), any(String.class));
		verifyNoMoreInteractions(mockCaseManagementClient);

	}

	@Test
	void getCaseStatusForPartyId_error() {
		when(mockCaseManagementClient.getCaseStatusForPartyId(MUNICIPALITY_ID, ORGANIZATION_NUMBER))
			.thenThrow(Problem.builder().build());

		final var result = caseManagementIntegration.getCaseStatusForPartyId(ORGANIZATION_NUMBER, MUNICIPALITY_ID);

		assertThat(result).isNotNull().isEmpty();

		verify(mockCaseManagementClient).getCaseStatusForPartyId(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verifyNoMoreInteractions(mockCaseManagementClient);
	}

}
