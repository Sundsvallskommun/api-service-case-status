package se.sundsvall.casestatus.integration.party;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static generated.se.sundsvall.party.PartyType.ENTERPRISE;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

@Component
public class PartyIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(PartyIntegration.class);

	private final PartyClient client;

	public PartyIntegration(final PartyClient client) {
		this.client = client;
	}

	/**
	 * Looks up the partyId for an organization number. Accepts both formatted (123456-1235) and unformatted input since
	 * the lookup is done on digits only.
	 *
	 * @param  municipalityId     the municipality id
	 * @param  organizationNumber the organization number to look up
	 * @return                    the partyId, or an empty Optional when the organization number is unknown to Party
	 */
	public Optional<String> getPartyIdByOrganizationNumber(final String municipalityId, final String organizationNumber) {
		final var legalId = organizationNumber.replaceAll("\\D", "");
		if (legalId.isEmpty()) {
			return Optional.empty();
		}
		final var partyId = client.getPartyIdByLegalId(municipalityId, ENTERPRISE, legalId);
		if (partyId.isEmpty()) {
			LOG.info("No partyId found for organization number {}", sanitizeForLogging(legalId));
		}
		return partyId;
	}
}
