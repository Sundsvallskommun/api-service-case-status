package se.sundsvall.casestatus.integration.casemanagement;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;

@Component
public class CaseManagementIntegration {
    
    private static final Logger LOG = LoggerFactory.getLogger(CaseManagementIntegration.class);
    
    private final CaseManagementClient client;
    
    public CaseManagementIntegration(final CaseManagementClient client) {
        this.client = client;
    }
    
    public Optional<CaseStatusDTO> getCaseStatusForExternalId(final String externalCaseId) {
        try {
            return Optional.of(client.getCaseStatusForExternalCaseId(externalCaseId));
        } catch (Exception e) {
            LOG.info("Unable to get case status for external id {}", externalCaseId, e);
            return Optional.empty();
        }
    }
    
    public List<CaseStatusDTO> getCaseStatusForOrganizationNumber(final String organizationNumber) {
        try {
            return client.getCaseStatusForOrganizationNumber(organizationNumber);
        } catch (Exception e) {
            LOG.info("Unable to get case status for organizationNumber{}", organizationNumber, e);
            return List.of();
        }
    }
}
