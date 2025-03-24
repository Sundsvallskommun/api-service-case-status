package se.sundsvall;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import java.time.LocalDateTime;

public final class TestDataFactory {

	private TestDataFactory() {}

	public static CaseStatusDTO createCaseStatusDTO(final CaseStatusDTO.SystemEnum system) {
		var caseStatus = new CaseStatusDTO();
		caseStatus.setCaseId("caseId");
		caseStatus.setExternalCaseId("externalCaseId");
		caseStatus.setCaseType("caseType");
		caseStatus.setStatus("status");
		caseStatus.setServiceName("serviceName");
		caseStatus.setTimestamp(LocalDateTime.now());
		caseStatus.setSystem(system);
		return caseStatus;
	}
}
