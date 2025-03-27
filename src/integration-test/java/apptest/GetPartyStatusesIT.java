package apptest;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import se.sundsvall.casestatus.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/GetPartyStatusesIT/", classes = Application.class)
class GetPartyStatusesIT extends AbstractAppTest {

	private static final String PATH = "/2281/party/a893dfa2-f781-49a9-9c5f-cba6353cf059/statuses";
	private static final String RESPONSE_FILE = "response.json";

	/**
	 * Test scenario where the party represents a private individual. Four cases are found in CaseManagement, one case is found in OpenE and one case is found in SupportManagement.
	 */
	@Test
	void test1_successful_private() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * Test scenario where the party represents an enterprise. Four cases are found in CaseManagement and one case is found in OpenE.
	 */
	@Test
	void test2_successful_enterprise() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}
}
