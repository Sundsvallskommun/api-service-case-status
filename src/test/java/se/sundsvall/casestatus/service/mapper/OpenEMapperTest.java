package se.sundsvall.casestatus.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import se.sundsvall.casestatus.integration.db.model.CaseEntity;

class OpenEMapperTest {

	@Test
	void toCaseStatusResponse() {
		// Arrange
		final var caseEntity = new CaseEntity();
		caseEntity.setFlowInstanceId("flowInstanceId");
		caseEntity.setErrandType("errandType");
		caseEntity.setStatus("status");
		caseEntity.setLastStatusChange("lastStatusChange");
		caseEntity.setFirstSubmitted("firstSubmitted");

		// Act
		final var response = OpenEMapper.toCaseStatusResponse(caseEntity);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo("flowInstanceId");
		assertThat(response.getCaseType()).isEqualTo("errandType");
		assertThat(response.getStatus()).isEqualTo("status");
		assertThat(response.getLastStatusChange()).isEqualTo("lastStatusChange");
		assertThat(response.getFirstSubmitted()).isEqualTo("firstSubmitted");
		assertThat(response.getSystem()).isEqualTo("OPEN_E_PLATFORM");
		assertThat(response.getExternalCaseId()).isEqualTo("flowInstanceId");
		assertThat(response.getErrandNumber()).isNull();
		assertThat(response.getNamespace()).isNull();
	}

	@Test
	void toCasePdfResponse() {
		// Arrange
		final var externalCaseId = "externalCaseId";
		final var pdfContent = "pdfContent";

		// Act
		final var response = OpenEMapper.toCasePdfResponse(externalCaseId, pdfContent);

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
		final var response = OpenEMapper.toOepStatusResponse(openEId);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getKey()).isEqualTo("status");
		assertThat(response.getValue()).isEqualTo(openEId);
	}
}
