package se.sundsvall.casestatus.integration.incident;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import generated.se.sundsvall.incident.IncidentOepResponse;

@Component
public class IncidentIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(IncidentIntegration.class);

	private final IncidentClient client;

	public IncidentIntegration(final IncidentClient client) {
		this.client = client;
	}

	public Optional<IncidentOepResponse> getIncidentStatus(final String externalCaseId, final String municipalityId) {
		try {
			return Optional.of(client.getIncidentStatusForExternalCaseId(municipalityId, externalCaseId));
		} catch (final Exception e) {
			LOG.warn("Unable to get incident status for external id {}", externalCaseId, e);

			return Optional.empty();
		}
	}

}
