package apptest;

import static apptest.CommonStubs.stubForAccessToken;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casestatus.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/GetCaseStatusIT/", classes = Application.class)
@Sql(scripts = { "/db/truncate.sql", "/db/casestatus.sql" })
class GetCaseStatusIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String PATH = "/" + MUNICIPALITY_ID + "/1308f4ca-f7d6-4c88-9098-64be43b3a905/status";
	private static final String FAULTY_PATH = "/" + MUNICIPALITY_ID + "/100757/status";
	private static final String EMPTY_PATH = "/" + MUNICIPALITY_ID + "/110757/status";

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
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse();
	}

}
