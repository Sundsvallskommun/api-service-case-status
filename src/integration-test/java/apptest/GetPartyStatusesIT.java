package apptest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casestatus.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/GetPartyStatusesIT/", classes = Application.class)
@Sql(scripts = { "/db/truncate.sql", "/db/casestatus.sql" })
class GetPartyStatusesIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";

	private static final String PATH = "/" + MUNICIPALITY_ID + "/party/1308f4ca-f7d6-4c88-9098-64be43b3a905/statuses";

	private static final String EMPTY_PATH = "/" + MUNICIPALITY_ID + "/party/1308f4ca-f7d6-4c88-9098-64be43b3a906/statuses";

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
	void test2_not_found_in_casemanagement_but_match_in_opene_and_casestatus_db() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(HttpMethod.GET)
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_not_found_in_opene_or_casemanagement_but_in_db() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(HttpMethod.GET)
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_not_found_anywhere() {
		setupCall()
			.withServicePath(EMPTY_PATH)
			.withHttpMethod(HttpMethod.GET)
			.withExpectedResponseStatus(HttpStatus.OK)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse();
	}
}
