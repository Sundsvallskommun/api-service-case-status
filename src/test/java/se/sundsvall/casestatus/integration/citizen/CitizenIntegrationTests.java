package se.sundsvall.casestatus.integration.citizen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;

@ExtendWith(MockitoExtension.class)
class CitizenIntegrationTests {

	@Mock
	private CitizenClient mockCitizenClient;

	@InjectMocks
	private CitizenIntegration citizenIntegration;

	@Test
	void getPersonID_ok() {

		when(mockCitizenClient.getPersonId(any(String.class))).thenReturn("someGUID");

		final var result = citizenIntegration.getPersonId("someExternalCaseId");

		assertThat(result).isEqualTo("someGUID");

		verify(mockCitizenClient).getPersonId(any(String.class));
		verifyNoMoreInteractions(mockCitizenClient);
	}

	@Test
	void getPersonID_error() {
		when(mockCitizenClient.getPersonId(any(String.class)))
			.thenThrow(Problem.builder().build());

		final var result = citizenIntegration.getPersonId("someGUID");

		assertThat(result).isEmpty();

		verify(mockCitizenClient).getPersonId(any(String.class));
		verifyNoMoreInteractions(mockCitizenClient);
	}
}
