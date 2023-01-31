package se.sundsvall.casestatus.integration.citizen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CitizenIntegration {

    static final String INTEGRATION_NAME = "Citizen";

    private static final Logger LOG = LoggerFactory.getLogger(CitizenIntegration.class);

    private final CitizenClient client;

    public CitizenIntegration(final CitizenClient client) {
        this.client = client;
    }

    public String getPersonID(final String personNumber) {
        try {
            return client.getPersonID(personNumber);
        } catch (Exception e) {
            LOG.info("Unable to get personId for person", e);
            return "";
        }
    }

}
