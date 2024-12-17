package se.sundsvall.casestatus.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casestatus.Application;
import se.sundsvall.casestatus.api.model.CasePdfResponse;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.api.model.OepStatusResponse;
import se.sundsvall.casestatus.service.CaseStatusService;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class CaseStatusResourceTests {

	private static final String MUNICIPALITY_ID = "2281";

	private static final String PATH = "/{municipalityId}/{externalCaseId}";

	@MockitoBean
	private CaseStatusService mockCaseStatusService;

	@Captor
	private ArgumentCaptor<String> caseStatusServiceArgumentCaptor;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getOepStatus() {
		when(mockCaseStatusService.getOepStatus(any(String.class), any(String.class))).thenReturn(OepStatusResponse.builder()
			.withKey("status")
			.withValue("someValue")
			.build());

		webTestClient.get()
			.uri(PATH + "/oepstatus", MUNICIPALITY_ID, "someExternalCaseId")
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.key").isEqualTo("status")
			.jsonPath("$.value").isEqualTo("someValue");

		verify(mockCaseStatusService).getOepStatus(caseStatusServiceArgumentCaptor.capture(), eq("2281"));
		verifyNoMoreInteractions(mockCaseStatusService);

		assertThat(caseStatusServiceArgumentCaptor.getValue()).isEqualTo("someExternalCaseId");
	}

	@Test
	void getCaseStatus() {
		when(mockCaseStatusService.getCaseStatus(any(String.class), any(String.class))).thenReturn(CaseStatusResponse.builder()
			.withId("someId")
			.withExternalCaseId("someExternalCaseId")
			.withStatus("someStatus")
			.withCaseType("someCaseType")
			.withFirstSubmitted("someFirstSubmittedValue")
			.withLastStatusChange("someLastStatusChangeValue")
			.withIsOpenEErrand(true)
			.build());

		webTestClient.get()
			.uri(PATH + "/status", MUNICIPALITY_ID, "someExternalCaseId")
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.id").isEqualTo("someId")
			.jsonPath("$.externalCaseId").isEqualTo("someExternalCaseId")
			.jsonPath("$.caseType").isEqualTo("someCaseType")
			.jsonPath("$.firstSubmitted").isEqualTo("someFirstSubmittedValue")
			.jsonPath("$.lastStatusChange").isEqualTo("someLastStatusChangeValue")
			.jsonPath("$.openEErrand").isEqualTo(true);

		verify(mockCaseStatusService).getCaseStatus(caseStatusServiceArgumentCaptor.capture(), eq("2281"));
		verifyNoMoreInteractions(mockCaseStatusService);

		assertThat(caseStatusServiceArgumentCaptor.getValue()).isEqualTo("someExternalCaseId");
	}

	@Test
	void getCasePdf() {
		when(mockCaseStatusService.getCasePdf(any(String.class))).thenReturn(CasePdfResponse.builder()
			.withExternalCaseId("someExternalCaseId")
			.withBase64("someBase64String")
			.build());

		webTestClient.get()
			.uri(PATH + "/pdf", MUNICIPALITY_ID, "someExternalCaseId")
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.externalCaseId").isEqualTo("someExternalCaseId")
			.jsonPath("$.base64").isEqualTo("someBase64String");

		verify(mockCaseStatusService).getCasePdf(caseStatusServiceArgumentCaptor.capture());
		verifyNoMoreInteractions(mockCaseStatusService);

		assertThat(caseStatusServiceArgumentCaptor.getValue()).isEqualTo("someExternalCaseId");
	}

	@Test
	void getOrganisationStatuses() {
		when(mockCaseStatusService.getCaseStatuses(any(String.class), any(String.class))).thenReturn(List.of(CaseStatusResponse.builder()
			.withId("someId")
			.withExternalCaseId("someExternalCaseId")
			.withStatus("someStatus")
			.withCaseType("someCaseType")
			.withFirstSubmitted("someFirstSubmittedValue")
			.withLastStatusChange("someLastStatusChangeValue")
			.withIsOpenEErrand(true)
			.build()));

		webTestClient.get()
			.uri(PATH + "/statuses", MUNICIPALITY_ID, "5591621234")
			.exchange()
			.expectStatus()
			.isOk()
			.expectHeader()
			.contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$").isArray()
			.jsonPath("$[0].id").isEqualTo("someId")
			.jsonPath("$[0].externalCaseId").isEqualTo("someExternalCaseId")
			.jsonPath("$[0].caseType").isEqualTo("someCaseType")
			.jsonPath("$[0].firstSubmitted").isEqualTo("someFirstSubmittedValue")
			.jsonPath("$[0].lastStatusChange").isEqualTo("someLastStatusChangeValue")
			.jsonPath("$[0].openEErrand").isEqualTo(true);

		verify(mockCaseStatusService).getCaseStatuses(caseStatusServiceArgumentCaptor.capture(), eq("2281"));
		verifyNoMoreInteractions(mockCaseStatusService);

		assertThat(caseStatusServiceArgumentCaptor.getValue()).isEqualTo("5591621234");
	}

}
