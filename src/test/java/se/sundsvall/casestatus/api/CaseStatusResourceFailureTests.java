package se.sundsvall.casestatus.api;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casestatus.Application;
import se.sundsvall.casestatus.api.model.CaseStatusResponse;
import se.sundsvall.casestatus.service.AggregatedCases;
import se.sundsvall.casestatus.service.CaseStatusService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ActiveProfiles("junit")
@AutoConfigureWebTestClient
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class CaseStatusResourceFailureTests {

	@MockitoBean
	private CaseStatusService mockCaseStatusService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getOrganisationStatusesWithInvalidOrganizationNumber() {
		when(mockCaseStatusService.getCaseStatuses(any(String.class), any(String.class))).thenReturn(new AggregatedCases(List.of(), List.of()));

		final var response = webTestClient.get()
			.uri("/{municipalityId}/{organizationNumber}/statuses", "2281", "invalid-org-no")
			.exchange()
			.expectStatus()
			.isOk()
			.expectBodyList(CaseStatusResponse.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isEmpty();
	}

	@Test
	void getErrandStatusesRejectsWildcardInPropertyDesignation() {
		// A bare wildcard matches every errand in every namespace - it must never reach the filter expression
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/2281/errands/statuses").queryParam("propertyDesignation", "*").build())
			.exchange()
			.expectStatus()
			.isBadRequest();

		verifyNoInteractions(mockCaseStatusService);
	}

	@Test
	void getErrandStatusesRejectsWildcardInErrandNumber() {
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/2281/errands/statuses").queryParam("errandNumber", "Case*").build())
			.exchange()
			.expectStatus()
			.isBadRequest();

		verifyNoInteractions(mockCaseStatusService);
	}

	@Test
	void getErrandStatusesRejectsOverlongSearchTerm() {
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/2281/errands/statuses").queryParam("errandNumber", "a".repeat(256)).build())
			.exchange()
			.expectStatus()
			.isBadRequest();

		verifyNoInteractions(mockCaseStatusService);
	}

	@Test
	void getErrandStatusesAcceptsApostropheInPropertyDesignation() {
		// An apostrophe is escaped rather than rejected - it occurs in real names
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/2281/errands/statuses").queryParam("propertyDesignation", "O'Brien 1").build())
			.exchange()
			.expectStatus()
			.isOk();

		verify(mockCaseStatusService).getErrandStatuses("2281", "O'Brien 1", null);
	}

}
