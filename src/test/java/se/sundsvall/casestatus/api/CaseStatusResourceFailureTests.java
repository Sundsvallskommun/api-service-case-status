package se.sundsvall.casestatus.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.casestatus.Application;
import se.sundsvall.casestatus.service.CaseStatusService;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@ActiveProfiles("junit")
@SpringBootTest(classes = Application.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class CaseStatusResourceFailureTests {

    @MockBean
    private CaseStatusService mockCaseStatusService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getOrganisationStatusesWithInvalidOrganizationNumber() {
        webTestClient.get()
                .uri("/{organizationNumber}/statuses", "invalid-org-no")
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectHeader()
                .contentType(APPLICATION_PROBLEM_JSON_VALUE)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Not Found")
                .jsonPath("$.status").isEqualTo(NOT_FOUND.value());
    }
}
