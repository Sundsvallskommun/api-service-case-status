package se.sundsvall;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import java.time.LocalDateTime;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;

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

	public static CaseStatusResponse createCaseStatusResponse(final CaseStatusDTO.SystemEnum system) {
		return CaseStatusResponse.builder()
			.withSystem(system.getValue())
			.withCaseId("caseId")
			.withExternalCaseId("externalCaseId")
			.withCaseType("caseType")
			.withStatus("status")
			.withLastStatusChange("2025-03-24 12:00:00")
			.withFirstSubmitted("2025-03-24 12:00:00")
			.withErrandNumber("errandNumber")
			.withNamespace("namespace")
			.build();
	}
}
