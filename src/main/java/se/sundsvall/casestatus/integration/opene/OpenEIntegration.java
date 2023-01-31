package se.sundsvall.casestatus.integration.opene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.util.casestatuscache.domain.FamilyId;

import java.util.Base64;
import java.util.Optional;

@Component
public class OpenEIntegration {

    static final String INTEGRATION_NAME = "OpenE";

    private static final Logger LOG = LoggerFactory.getLogger(OpenEIntegration.class);

    private final OpenEClient client;

    public OpenEIntegration(final OpenEClient client) {
        this.client = client;
    }

    public Optional<String> getPdf(final String externalCaseId) {

        try {
            return Optional.of(Base64.getEncoder().encodeToString(client.getPDF(externalCaseId)));
        } catch (Exception e) {
            LOG.info("Unable to get pdf for external id {}", externalCaseId, e);
            return Optional.empty();
        }
    }

    public byte[] getErrandIds(FamilyId familyID) {

        try {
            return client.getErrandIds(String.valueOf(familyID.getValue()));
        } catch (Exception e) {
            LOG.info("Unable to get errandIds for familyId {}", familyID, e);
            return "".getBytes();
        }

    }

    public byte[] getErrand(String flowInstanceId) {
        try {
            return client.getErrand(flowInstanceId);
        } catch (Exception e) {
            LOG.info("Unable to get errand for flowInstanceId {}", flowInstanceId, e);
            return "".getBytes();
        }

    }

    public byte[] getErrandStatus(String flowInstanceId) {
        try {
            return client.getErrandStatus(flowInstanceId);
        } catch (Exception e) {
            LOG.info("Unable to get errandStatus for flowInstanceId {}", flowInstanceId, e);
            return "".getBytes();
        }
    }


}
