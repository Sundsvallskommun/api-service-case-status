package se.sundsvall.casestatus.integration.casemanagement;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;

@FeignClient(
    name = CaseManagementIntegration.INTEGRATION_NAME,
    url = "${integration.case-management.base-url}",
    configuration = CaseManagementIntegrationConfiguration.class
)
interface CaseManagementClient {
    
    @GetMapping("/cases/{externalCaseId}/status")
    CaseStatusDTO getCaseStatusForExternalCaseId(@PathVariable("externalCaseId") final String externalCaseId);
    
    @GetMapping("/organization/{organizationNumber}/cases/status")
    List<CaseStatusDTO> getCaseStatusForOrganizationNumber(@PathVariable("organizationNumber") final String organizationNumber);
}
