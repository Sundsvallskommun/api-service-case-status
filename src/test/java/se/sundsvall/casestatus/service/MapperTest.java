package se.sundsvall.casestatus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casestatus.utility.Constants.MISSING;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
import generated.se.sundsvall.supportmanagement.Classification;
import generated.se.sundsvall.supportmanagement.Errand;
import generated.se.sundsvall.supportmanagement.ExternalTag;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import se.sundsvall.casestatus.integration.db.model.CaseEntity;

class MapperTest {

	@Test
	void mapToCaseStatusResponse() {
		// Arrange
		final var flowInstanceId = "flowInstanceId";
		final var errandType = "errandType";
		final var status = "status";
		final var firstSubmitted = "firstSubmitted";
		final var lastStatusChange = "lastStatusChange";
		final var caseEntity = CaseEntity.builder()
			.withFlowInstanceId(flowInstanceId)
			.withErrandType(errandType)
			.withStatus(status)
			.withFirstSubmitted(firstSubmitted)
			.withLastStatusChange(lastStatusChange)
			.build();

		// Act
		final var response = Mapper.mapToCaseStatusResponse(caseEntity);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getExternalCaseId()).isEqualTo(flowInstanceId);
		assertThat(response.getCaseType()).isEqualTo(errandType);
		assertThat(response.getStatus()).isEqualTo(status);
		assertThat(response.getFirstSubmitted()).isEqualTo(firstSubmitted);
		assertThat(response.getLastStatusChange()).isEqualTo(lastStatusChange);
		assertThat(response.isOpenEErrand()).isTrue();
	}

	@Test
	void toCaseStatusResponse() {
		// Arrange
		final var caseId = "caseId";
		final var externalCaseId = "externalCaseId";
		final var status = "status";
		final var serviceName = "serviceName";
		final var newStatus = "newStatus";
		final var timestamp = "timestamp";
		final var caseStatusDTO = new CaseStatusDTO()
			.caseId(caseId)
			.externalCaseId(externalCaseId)
			.status(status);

		// Act
		final var response = Mapper.toCaseStatusResponse(caseStatusDTO, serviceName, newStatus, timestamp);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo(caseId);
		assertThat(response.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(response.getCaseType()).isEqualTo(serviceName);
		assertThat(response.getStatus()).isEqualTo(newStatus);
		assertThat(response.getLastStatusChange()).isEqualTo(timestamp);
		assertThat(response.getFirstSubmitted()).isEqualTo(MISSING);
		assertThat(response.isOpenEErrand()).isFalse();
	}

	@Test
	void toCasePdfResponse() {
		// Arrange
		final var externalCaseId = "externalCaseId";
		final var pdfContent = "pdfContent";

		// Act
		final var response = Mapper.toCasePdfResponse(externalCaseId, pdfContent);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(response.getBase64()).isEqualTo(pdfContent);
	}

	@Test
	void toOepStatusResponse() {
		// Arrange
		final var openEId = "openEId";

		// Act
		final var response = Mapper.toOepStatusResponse(openEId);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getKey()).isEqualTo("status");
		assertThat(response.getValue()).isEqualTo(openEId);
	}

	@Test
	void toCaseStatusResponse_withExternalCaseId() {
		// Arrange
		final var errand = new Errand()
			.id("errandId")
			.classification(new Classification().type("someType"))
			.status("someStatus")
			.created(OffsetDateTime.parse("2023-01-01T10:00:00Z"))
			.modified(OffsetDateTime.parse("2023-01-02T10:00:00Z"))
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
			.addExternalTagsItem(new ExternalTag().key("caseId").value("caseId"));

		// Act
		final var response = Mapper.toCaseStatusResponse(errand);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo("errandId");
		assertThat(response.getExternalCaseId()).isEqualTo("caseId");
		assertThat(response.getCaseType()).isEqualTo("someType");
		assertThat(response.getStatus()).isEqualTo("someStatus");
		assertThat(response.getLastStatusChange()).isEqualTo("2023-01-02 10:00");
		assertThat(response.getFirstSubmitted()).isEqualTo("2023-01-01 10:00");
		assertThat(response.isOpenEErrand()).isTrue();
	}

	@Test
	void toCaseStatusResponse_withoutExternalCaseId() {
		// Arrange
		final var errand = new Errand()
			.id("errandId")
			.classification(new Classification().type("someType"))
			.status("someStatus")
			.created(OffsetDateTime.parse("2023-01-01T10:00:00Z"))
			.modified(OffsetDateTime.parse("2023-01-02T10:00:00Z"))
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"));

		// Act
		final var response = Mapper.toCaseStatusResponse(errand);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo("errandId");
		assertThat(response.getExternalCaseId()).isNull();
		assertThat(response.getCaseType()).isEqualTo("someType");
		assertThat(response.getStatus()).isEqualTo("someStatus");
		assertThat(response.getLastStatusChange()).isEqualTo("2023-01-02 10:00");
		assertThat(response.getFirstSubmitted()).isEqualTo("2023-01-01 10:00");
		assertThat(response.isOpenEErrand()).isFalse();
	}

	@Test
	void toCaseStatusResponse_withNullModified() {
		// Arrange
		final var errand = new Errand()
			.id("errandId")
			.classification(new Classification().type("someType"))
			.status("someStatus")
			.created(OffsetDateTime.parse("2023-01-01T10:00:00Z"))
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
			.addExternalTagsItem(new ExternalTag().key("caseId").value("caseId"));

		// Act
		final var response = Mapper.toCaseStatusResponse(errand);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo("errandId");
		assertThat(response.getExternalCaseId()).isEqualTo("caseId");
		assertThat(response.getCaseType()).isEqualTo("someType");
		assertThat(response.getStatus()).isEqualTo("someStatus");
		assertThat(response.getLastStatusChange()).isNull();
		assertThat(response.getFirstSubmitted()).isEqualTo("2023-01-01 10:00");
		assertThat(response.isOpenEErrand()).isTrue();
	}

	@Test
	void toCaseEntity_withExternalCaseId() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var errand = new Errand()
			.id("errandId")
			.classification(new Classification().type("someType"))
			.status("someStatus")
			.created(OffsetDateTime.parse("2023-01-01T10:00:00Z"))
			.modified(OffsetDateTime.parse("2023-01-02T10:00:00Z"))
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"))
			.addExternalTagsItem(new ExternalTag().key("caseId").value("caseId"));

		// Act
		final var caseEntity = Mapper.toCaseEntity(errand, municipalityId);

		// Assert
		assertThat(caseEntity).isNotNull();
		assertThat(caseEntity.getFlowInstanceId()).isEqualTo("caseId");
		assertThat(caseEntity.getFamilyId()).isEqualTo("123");
		assertThat(caseEntity.getStatus()).isEqualTo("someStatus");
		assertThat(caseEntity.getErrandType()).isEqualTo("someType");
		assertThat(caseEntity.getFirstSubmitted()).isEqualTo("2023-01-01 10:00");
		assertThat(caseEntity.getLastStatusChange()).isEqualTo("2023-01-02 10:00");
		assertThat(caseEntity.getMunicipalityId()).isEqualTo(municipalityId);
	}

	@Test
	void toCaseEntity_withoutExternalCaseId() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var errand = new Errand()
			.id("errandId")
			.classification(new Classification().type("someType"))
			.status("someStatus")
			.created(OffsetDateTime.parse("2023-01-01T10:00:00Z"))
			.modified(OffsetDateTime.parse("2023-01-02T10:00:00Z"))
			.addExternalTagsItem(new ExternalTag().key("familyId").value("123"));

		// Act
		final var caseEntity = Mapper.toCaseEntity(errand, municipalityId);

		// Assert
		assertThat(caseEntity).isNotNull();
		assertThat(caseEntity.getFlowInstanceId()).isNull();
		assertThat(caseEntity.getFamilyId()).isEqualTo("123");
		assertThat(caseEntity.getStatus()).isEqualTo("someStatus");
		assertThat(caseEntity.getErrandType()).isEqualTo("someType");
		assertThat(caseEntity.getFirstSubmitted()).isEqualTo("2023-01-01 10:00");
		assertThat(caseEntity.getLastStatusChange()).isEqualTo("2023-01-02 10:00");
		assertThat(caseEntity.getMunicipalityId()).isEqualTo(municipalityId);
	}
}
