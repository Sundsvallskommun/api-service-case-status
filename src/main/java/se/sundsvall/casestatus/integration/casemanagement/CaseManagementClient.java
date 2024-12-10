package se.sundsvall.casestatus.integration.casemanagement;

import static se.sundsvall.casestatus.integration.casemanagement.configuration.CaseManagementConfiguration.CLIENT_ID;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import se.sundsvall.casestatus.integration.casemanagement.configuration.CaseManagementConfiguration;

@FeignClient(
	name = CLIENT_ID,
	url = "${integration.case-management.base-url}",
	configuration = CaseManagementConfiguration.class)
public interface CaseManagementClient {

	@GetMapping("/{municipalityId}/cases/{externalCaseId}/status")
	CaseStatusDTO getCaseStatusForExternalCaseId(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("externalCaseId") final String externalCaseId);

	@GetMapping("/{municipalityId}/organization/{organizationNumber}/cases/status")
	List<CaseStatusDTO> getCaseStatusForOrganizationNumber(
		@PathVariable("municipalityId") final String municipalityId,
		@PathVariable("organizationNumber") final String organizationNumber);

}
