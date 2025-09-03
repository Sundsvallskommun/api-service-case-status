package se.sundsvall.casestatus.integration.casemanagement;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CaseManagementIntegration {

	private static final Logger LOG = LoggerFactory.getLogger(CaseManagementIntegration.class);

	private final CaseManagementClient client;

	public CaseManagementIntegration(final CaseManagementClient client) {
		this.client = client;
	}

	public Optional<CaseStatusDTO> getCaseStatusForExternalId(final String externalCaseId, final String municipalityId) {
		try {
			return Optional.of(client.getCaseStatusForExternalCaseId(municipalityId, externalCaseId));
		} catch (final Exception e) {
			LOG.info("Unable to get case status for external id {}", sanitizeForLogging(externalCaseId), e);
			return empty();
		}
	}

	public List<CaseStatusDTO> getCaseStatusForOrganizationNumber(final String organizationNumber, final String municipalityId) {
		try {
			return client.getCaseStatusForOrganizationNumber(municipalityId, organizationNumber);
		} catch (final Exception e) {
			LOG.info("Unable to get case status for organizationNumber{}", sanitizeForLogging(organizationNumber), e);
			return emptyList();
		}
	}

	public List<CaseStatusDTO> getCaseStatusForPartyId(final String partyId, final String municipalityId) {
		try {
			return client.getCaseStatusForPartyId(municipalityId, partyId);
		} catch (final Exception e) {
			LOG.info("Unable to get case status for partyId{}", sanitizeForLogging(partyId), e);
			return emptyList();
		}
	}
}
