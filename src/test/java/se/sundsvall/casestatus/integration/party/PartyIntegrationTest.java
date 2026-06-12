package se.sundsvall.casestatus.integration.party;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static generated.se.sundsvall.party.PartyType.ENTERPRISE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartyIntegrationTest {

	private static final String PARTY_ID = "partyId";
	private static final String MUNICIPALITY_ID = "municipalityId";

	@Mock
	private PartyClient partyClientMock;

	@InjectMocks
	private PartyIntegration partyIntegration;

	@Test
	void getPartyIdByOrganizationNumberFound() {
		when(partyClientMock.getPartyIdByLegalId(MUNICIPALITY_ID, ENTERPRISE, "2120002411")).thenReturn(Optional.of(PARTY_ID));

		final var result = partyIntegration.getPartyIdByOrganizationNumber(MUNICIPALITY_ID, "2120002411");

		assertThat(result).contains(PARTY_ID);
		verify(partyClientMock).getPartyIdByLegalId(MUNICIPALITY_ID, ENTERPRISE, "2120002411");
		verifyNoMoreInteractions(partyClientMock);
	}

	@Test
	void getPartyIdByOrganizationNumberStripsFormatting() {
		when(partyClientMock.getPartyIdByLegalId(MUNICIPALITY_ID, ENTERPRISE, "2120002411")).thenReturn(Optional.of(PARTY_ID));

		final var result = partyIntegration.getPartyIdByOrganizationNumber(MUNICIPALITY_ID, "212000-2411");

		assertThat(result).contains(PARTY_ID);
		verify(partyClientMock).getPartyIdByLegalId(MUNICIPALITY_ID, ENTERPRISE, "2120002411");
		verifyNoMoreInteractions(partyClientMock);
	}

	@Test
	void getPartyIdByOrganizationNumberWithoutDigits() {
		final var result = partyIntegration.getPartyIdByOrganizationNumber(MUNICIPALITY_ID, "no-digits-here");

		assertThat(result).isEmpty();
		verifyNoInteractions(partyClientMock);
	}

	@Test
	void getPartyIdByOrganizationNumberNotFound() {
		when(partyClientMock.getPartyIdByLegalId(MUNICIPALITY_ID, ENTERPRISE, "2120002411")).thenReturn(Optional.empty());

		final var result = partyIntegration.getPartyIdByOrganizationNumber(MUNICIPALITY_ID, "2120002411");

		assertThat(result).isEmpty();
		verify(partyClientMock).getPartyIdByLegalId(MUNICIPALITY_ID, ENTERPRISE, "2120002411");
		verifyNoMoreInteractions(partyClientMock);
	}

}
