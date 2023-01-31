package se.sundsvall.casestatus.integration.incident;

import generated.se.sundsvall.incident.IncidentOepResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class IncidentIntegration {

    static final String INTEGRATION_NAME = "Incident";

    private static final Logger LOG = LoggerFactory.getLogger(IncidentIntegration.class);

    private final IncidentClient client;

    public IncidentIntegration(final IncidentClient client) {
        this.client = client;
    }

    public Optional<IncidentOepResponse> getIncidentStatus(final String externalCaseId) {
        try {
            return Optional.of(client.getIncidentStatusForExternalCaseId(externalCaseId));
        } catch (Exception e) {
            LOG.info("Unable to get incident status for external id {}", externalCaseId, e);

            return Optional.empty();
        }
    }
}
