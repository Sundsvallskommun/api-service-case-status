package se.sundsvall.casestatus.integration.opene;

import java.util.Base64;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.util.casestatuscache.domain.FamilyId;

@Component
public class OpenEIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(OpenEIntegration.class);

	private final OpenEClient client;

	public OpenEIntegration(final OpenEClient client) {
		this.client = client;
	}

	public Optional<String> getPdf(final String externalCaseId) {

		try {
			return Optional.of(Base64.getEncoder().encodeToString(client.getPDF(externalCaseId)));
		} catch (final Exception e) {
			LOG.info("Unable to get pdf for external id {}", externalCaseId, e);
			return Optional.empty();
		}
	}

	public byte[] getErrandIds(final FamilyId familyID) {

		try {
			return client.getErrandIds(String.valueOf(familyID.getValue()));
		} catch (final Exception e) {
			LOG.info("Unable to get errandIds for familyId {}", familyID, e);
			return "".getBytes();
		}
	}

	public byte[] getErrand(final String flowInstanceId) {
		try {
			return client.getErrand(flowInstanceId);
		} catch (final Exception e) {
			LOG.info("Unable to get errand for flowInstanceId {}", flowInstanceId, e);
			return "".getBytes();
		}
	}

	public byte[] getErrandStatus(final String flowInstanceId) {
		try {
			return client.getErrandStatus(flowInstanceId);
		} catch (final Exception e) {
			LOG.info("Unable to get errandStatus for flowInstanceId {}", flowInstanceId, e);
			return "".getBytes();
		}
	}

}
