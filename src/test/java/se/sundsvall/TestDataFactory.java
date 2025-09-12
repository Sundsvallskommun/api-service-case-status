package se.sundsvall;

import generated.se.sundsvall.casedata.Status;
import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.integration.db.model.CaseEntity;

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
		caseStatus.setErrandNumber("errandNumber");
		caseStatus.setNamespace("namespace");
		return caseStatus;
	}

	public static CaseStatusResponse createCaseStatusResponse(final String system) {
		return CaseStatusResponse.builder()
			.withSystem(system)
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

	public static CaseStatusResponse createCaseStatusResponse(final String system, final String externalCaseId) {
		return CaseStatusResponse.builder()
			.withSystem(system)
			.withCaseId("caseId")
			.withExternalCaseId(externalCaseId)
			.withCaseType("caseType")
			.withStatus("status")
			.withExternalStatus("externalStatus")
			.withLastStatusChange("2025-03-24 12:00:00")
			.withFirstSubmitted("2025-03-24 12:00:00")
			.withErrandNumber("errandNumber")
			.withNamespace("namespace")
			.build();
	}

	public static CaseEntity createCaseEntity() {
		return CaseEntity.builder()
			.withStatus("status")
			.withFamilyId("familyId")
			.withFlowInstanceId("flowInstanceId")
			.withErrandType("errandType")
			.withPersonId("personId")
			.withLastStatusChange("lastStatusChange")
			.withFirstSubmitted("firstSubmitted")
			.withContentType("contentType")
			.withMunicipalityId("municipalityId")
			.withOrganisationNumber("organisationNumber")
			.build();
	}

	public static Errand createErrand() {
		return new Errand()
			.created(OffsetDateTime.of(2022, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC))
			.modified(OffsetDateTime.of(2022, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC))
			.id("id")
			.externalTags(Set.of(new ExternalTag().key("familyId").value("familyId"), new ExternalTag().key("caseId").value("externalCaseId")))
			.status("status")
			.classification(new Classification().type("caseType").category("caseCategory"))
			.title("title")
			.channel("channel")
			.errandNumber("errandNumber");
	}

	public static generated.se.sundsvall.casedata.Errand createCaseDataErrand() {
		var status = new Status().statusType("status").created(OffsetDateTime.now());
		return new generated.se.sundsvall.casedata.Errand()
			.id(123L)
			.caseType("caseType")
			.status(status)
			.statuses(List.of(status))
			.externalCaseId("externalCaseId")
			.namespace("namespace");
	}
}
