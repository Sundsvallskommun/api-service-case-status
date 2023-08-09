package apptest;

import static apptest.CommonStubs.stubForAccessToken;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;

import se.sundsvall.casestatus.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/GetOrganisationStatusesIT/", classes = Application.class)
class GetOrganisationStatusesIT extends AbstractAppTest {

	private static final String PATH = "/1234561235/statuses";
	private static final String FAULTY_PATH = "/1234561233/statuses";
	private static final String EMPTY_PATH = "/1234561232/statuses";

	@BeforeEach
	void setUp() {
		stubForAccessToken();
	}

	@Test
	void test1_successful() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(HttpMethod.GET)
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_not_found_in_casemanagement_but_match_in_casestatus_db() {
		setupCall()
			.withServicePath(FAULTY_PATH)
			.withHttpMethod(HttpMethod.GET)
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_not_found_in_casemanagement_and_no_match_in_casestatus_db() {
		setupCall()
			.withServicePath(EMPTY_PATH)
			.withHttpMethod(HttpMethod.GET)
			.withExpectedResponseStatus(HttpStatus.NOT_FOUND)
			.sendRequestAndVerifyResponse();
	}
}
