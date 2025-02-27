package se.sundsvall.casestatus.integration.party;

import static generated.se.sundsvall.party.PartyType.ENTERPRISE;
import static generated.se.sundsvall.party.PartyType.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casestatus.integration.party.PartyIntegration.INVALID_LEGAL_ID;
import static se.sundsvall.casestatus.integration.party.PartyIntegration.INVALID_PARTY_ID;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;

@ExtendWith(MockitoExtension.class)
class PartyIntegrationTest {

	private static final String PARTY_ID = "partyId";
	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String LEGAL_ID = "legalId";

	@Mock
	private PartyClient partyClientMock;

	@InjectMocks
	private PartyIntegration partyIntegration;

	@Test
	void getLegalIdByPartyId_privateFound() {
		when(partyClientMock.getLegalIdByPartyId(MUNICIPALITY_ID, PRIVATE, PARTY_ID)).thenReturn(Optional.of(LEGAL_ID));

		final var result = partyIntegration.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID);

		assertThat(result).containsOnlyKeys(PRIVATE).containsEntry(PRIVATE, LEGAL_ID);
		verify(partyClientMock).getLegalIdByPartyId(MUNICIPALITY_ID, PRIVATE, PARTY_ID);
		verifyNoMoreInteractions(partyClientMock);
	}

	@Test
	void getLegalIdByPartyId_enterpriseFound() {
		when(partyClientMock.getLegalIdByPartyId(MUNICIPALITY_ID, PRIVATE, PARTY_ID)).thenReturn(Optional.empty());
		when(partyClientMock.getLegalIdByPartyId(MUNICIPALITY_ID, ENTERPRISE, PARTY_ID)).thenReturn(Optional.of(LEGAL_ID));

		final var result = partyIntegration.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID);

		assertThat(result).containsOnlyKeys(ENTERPRISE).containsEntry(ENTERPRISE, LEGAL_ID);
		verify(partyClientMock).getLegalIdByPartyId(MUNICIPALITY_ID, PRIVATE, PARTY_ID);
		verify(partyClientMock).getLegalIdByPartyId(MUNICIPALITY_ID, ENTERPRISE, PARTY_ID);
		verifyNoMoreInteractions(partyClientMock);
	}

	@Test
	void getPartyIdByLegalId_badRequest() {
		when(partyClientMock.getLegalIdByPartyId(MUNICIPALITY_ID, PRIVATE, PARTY_ID)).thenReturn(Optional.empty());
		when(partyClientMock.getLegalIdByPartyId(MUNICIPALITY_ID, ENTERPRISE, PARTY_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> partyIntegration.getLegalIdByPartyId(MUNICIPALITY_ID, PARTY_ID))
			.isInstanceOf(Problem.class)
			.hasMessageContaining(INVALID_PARTY_ID.formatted(PARTY_ID));

		verify(partyClientMock).getLegalIdByPartyId(MUNICIPALITY_ID, PRIVATE, PARTY_ID);
		verify(partyClientMock).getLegalIdByPartyId(MUNICIPALITY_ID, ENTERPRISE, PARTY_ID);
		verifyNoMoreInteractions(partyClientMock);
	}

	@Test
	void getPartyIdByLegalId_privateFound() {
		when(partyClientMock.getPartyIdByLegalId(MUNICIPALITY_ID, PRIVATE, LEGAL_ID)).thenReturn(Optional.of(PARTY_ID));

		final var result = partyIntegration.getPartyIdByLegalId(MUNICIPALITY_ID, LEGAL_ID);

		assertThat(result).containsOnlyKeys(PRIVATE).containsEntry(PRIVATE, PARTY_ID);
		verify(partyClientMock).getPartyIdByLegalId(MUNICIPALITY_ID, PRIVATE, LEGAL_ID);
		verifyNoMoreInteractions(partyClientMock);
	}

	@Test
	void getPartyIdByLegalId_enterpriseFound() {
		when(partyClientMock.getPartyIdByLegalId(MUNICIPALITY_ID, PRIVATE, LEGAL_ID)).thenReturn(Optional.empty());
		when(partyClientMock.getPartyIdByLegalId(MUNICIPALITY_ID, ENTERPRISE, LEGAL_ID)).thenReturn(Optional.of(PARTY_ID));

		final var result = partyIntegration.getPartyIdByLegalId(MUNICIPALITY_ID, LEGAL_ID);

		assertThat(result).containsOnlyKeys(ENTERPRISE).containsEntry(ENTERPRISE, PARTY_ID);
		verify(partyClientMock).getPartyIdByLegalId(MUNICIPALITY_ID, PRIVATE, LEGAL_ID);
		verify(partyClientMock).getPartyIdByLegalId(MUNICIPALITY_ID, ENTERPRISE, LEGAL_ID);
		verifyNoMoreInteractions(partyClientMock);
	}

	@Test
	void getLegalIdByPartyId_badRequest() {
		when(partyClientMock.getPartyIdByLegalId(MUNICIPALITY_ID, PRIVATE, LEGAL_ID)).thenReturn(Optional.empty());
		when(partyClientMock.getPartyIdByLegalId(MUNICIPALITY_ID, ENTERPRISE, LEGAL_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> partyIntegration.getPartyIdByLegalId(MUNICIPALITY_ID, LEGAL_ID))
			.isInstanceOf(Problem.class)
			.hasMessageContaining(INVALID_LEGAL_ID.formatted(LEGAL_ID));

		verify(partyClientMock).getPartyIdByLegalId(MUNICIPALITY_ID, PRIVATE, LEGAL_ID);
		verify(partyClientMock).getPartyIdByLegalId(MUNICIPALITY_ID, ENTERPRISE, LEGAL_ID);
		verifyNoMoreInteractions(partyClientMock);
	}

}
