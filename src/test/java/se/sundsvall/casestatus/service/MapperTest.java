package se.sundsvall.casestatus.service;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casestatus.service.CaseStatusService.MISSING;

import generated.se.sundsvall.casemanagement.CaseStatusDTO;
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
		assertThat(response.getId()).isEqualTo(flowInstanceId);
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
		assertThat(response.getId()).isEqualTo(caseId);
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
}
