package se.sundsvall.casestatus.service.mapper;

import generated.client.oep_integrator.CaseEnvelope;
import generated.client.oep_integrator.CaseStatus;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import se.sundsvall.casestatus.integration.db.model.CaseEntity;
import se.sundsvall.casestatus.service.StatusVocabulary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenEMapperTest {

	@Mock
	private StatusVocabulary statusVocabulary;

	@InjectMocks
	private OpenEMapper openEMapper;

	@Test
	void toCaseStatusResponse_caseEntity() {
		final var caseEntity = new CaseEntity();
		caseEntity.setFlowInstanceId("flowInstanceId");
		caseEntity.setErrandType("errandType");
		caseEntity.setStatus("status");
		caseEntity.setLastStatusChange("lastStatusChange");
		caseEntity.setFirstSubmitted("firstSubmitted");

		when(statusVocabulary.translateOepStatus("status")).thenReturn("Handläggning pågår");

		final var response = openEMapper.toCaseStatusResponse(caseEntity);

		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo("flowInstanceId");
		assertThat(response.getCaseType()).isEqualTo("errandType");
		assertThat(response.getStatus()).isEqualTo("status");
		assertThat(response.getExternalStatus()).isEqualTo("Handläggning pågår");
		assertThat(response.getLastStatusChange()).isEqualTo("lastStatusChange");
		assertThat(response.getFirstSubmitted()).isEqualTo("firstSubmitted");
		assertThat(response.getSystem()).isEqualTo("OPEN_E_PLATFORM");
		assertThat(response.getExternalCaseId()).isEqualTo("flowInstanceId");
		assertThat(response.getErrandNumber()).isEqualTo("flowInstanceId");
		assertThat(response.getNamespace()).isNull();
	}

	@Test
	void toCaseStatusResponse_caseEntity_returnsNull_whenInputNull() {
		assertThat(openEMapper.toCaseStatusResponse((CaseEntity) null)).isNull();
	}

	@Test
	void toCaseStatusResponse_caseEnvelope() {
		final var envelope = new CaseEnvelope()
			.flowInstanceId("999")
			.displayName("displayName")
			.status(new CaseStatus().name("Inskickat"));

		when(statusVocabulary.translateOepStatus(envelope.getStatus())).thenReturn("Inskickat");

		final var response = openEMapper.toCaseStatusResponse(envelope);

		assertThat(response).isNotNull();
		assertThat(response.getCaseId()).isEqualTo("999");
		assertThat(response.getCaseType()).isEqualTo("displayName");
		assertThat(response.getStatus()).isEqualTo("Inskickat");
		assertThat(response.getExternalStatus()).isEqualTo("Inskickat");
		assertThat(response.getSystem()).isEqualTo("OPEN_E_PLATFORM");
		assertThat(response.getExternalCaseId()).isEqualTo("999");
		assertThat(response.getErrandNumber()).isEqualTo("999");
	}

	@Test
	void toCaseStatusResponse_caseEnvelope_returnsNull_whenInputNull() {
		assertThat(openEMapper.toCaseStatusResponse((CaseEnvelope) null)).isNull();
	}

	@Test
	void toCaseStatusResponse_caseEnvelope_returnsNull_whenStatusMissing() {
		assertThat(openEMapper.toCaseStatusResponse(new CaseEnvelope().flowInstanceId("1"))).isNull();
	}

	@Test
	void toCasePdfResponse() throws IOException {
		final var externalCaseId = "externalCaseId";
		final var pdfContent = "pdfContent";
		final var pdfInputStream = new InputStreamResource(new ByteArrayInputStream(pdfContent.getBytes()));

		final var response = openEMapper.toCasePdfResponse(externalCaseId, pdfInputStream);

		assertThat(response).isNotNull();
		assertThat(response.getExternalCaseId()).isEqualTo(externalCaseId);
		assertThat(response.getBase64()).isEqualTo(Base64.getEncoder().encodeToString(pdfContent.getBytes()));
	}

	@Test
	void toOepStatusResponse() {
		final var response = openEMapper.toOepStatusResponse("openEId");

		assertThat(response).isNotNull();
		assertThat(response.getKey()).isEqualTo("status");
		assertThat(response.getValue()).isEqualTo("openEId");
	}
}
