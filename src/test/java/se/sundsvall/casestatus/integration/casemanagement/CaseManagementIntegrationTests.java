package se.sundsvall.casestatus.integration.casemanagement;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.problem.Problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

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

	/**
	 * Failures propagate rather than being swallowed into an empty list, so that CaseAggregator can tell an unreachable
	 * CaseManagement apart from one that simply has no cases for the organization.
	 */
	@Test
	void getCaseStatusForOrganizationNumber_error() {
		final var problem = Problem.builder().build();
		when(mockCaseManagementClient.getCaseStatusForOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER))
			.thenThrow(problem);

		assertThatThrownBy(() -> caseManagementIntegration.getCaseStatusForOrganizationNumber(ORGANIZATION_NUMBER, MUNICIPALITY_ID))
			.isSameAs(problem);

		verify(mockCaseManagementClient).getCaseStatusForOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verifyNoMoreInteractions(mockCaseManagementClient);
	}

	@Test
	void getCaseStatusForOrganizationNumber_null() {
		when(mockCaseManagementClient.getCaseStatusForOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(null);

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
		final var problem = Problem.builder().build();
		when(mockCaseManagementClient.getCaseStatusForPartyId(MUNICIPALITY_ID, ORGANIZATION_NUMBER))
			.thenThrow(problem);

		assertThatThrownBy(() -> caseManagementIntegration.getCaseStatusForPartyId(ORGANIZATION_NUMBER, MUNICIPALITY_ID))
			.isSameAs(problem);

		verify(mockCaseManagementClient).getCaseStatusForPartyId(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verifyNoMoreInteractions(mockCaseManagementClient);
	}

	/**
	 * CaseManagement answers 404, not an empty list, when there are no cases — that must read as "nothing to contribute"
	 * rather than as a failed source.
	 */
	@Test
	void getCaseStatusForPartyId_noCases() {
		when(mockCaseManagementClient.getCaseStatusForPartyId(MUNICIPALITY_ID, ORGANIZATION_NUMBER))
			.thenThrow(new ClientProblem(NOT_FOUND, "No cases found"));

		final var result = caseManagementIntegration.getCaseStatusForPartyId(ORGANIZATION_NUMBER, MUNICIPALITY_ID);

		assertThat(result).isNotNull().isEmpty();

		verify(mockCaseManagementClient).getCaseStatusForPartyId(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verifyNoMoreInteractions(mockCaseManagementClient);
	}

	/**
	 * Any other 4xx means we sent a request CaseManagement rejected, which is our defect and must not be hidden.
	 */
	@Test
	void getCaseStatusForPartyId_clientErrorPropagates() {
		final var problem = new ClientProblem(BAD_REQUEST, "Bad request");
		when(mockCaseManagementClient.getCaseStatusForPartyId(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenThrow(problem);

		assertThatThrownBy(() -> caseManagementIntegration.getCaseStatusForPartyId(ORGANIZATION_NUMBER, MUNICIPALITY_ID))
			.isSameAs(problem);

		verify(mockCaseManagementClient).getCaseStatusForPartyId(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verifyNoMoreInteractions(mockCaseManagementClient);
	}

	@Test
	void getCaseStatusForPartyId_null() {
		when(mockCaseManagementClient.getCaseStatusForPartyId(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(null);

		final var result = caseManagementIntegration.getCaseStatusForPartyId(ORGANIZATION_NUMBER, MUNICIPALITY_ID);

		assertThat(result).isNotNull().isEmpty();

		verify(mockCaseManagementClient).getCaseStatusForPartyId(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verifyNoMoreInteractions(mockCaseManagementClient);
	}

}
