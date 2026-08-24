package apptest;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.casestatus.util.Constants.SOURCE_OPEN_E_PLATFORM;
import static se.sundsvall.casestatus.util.Constants.UNAVAILABLE_SOURCES_HEADER;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.casestatus.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/GetPartyStatusesIT/", classes = Application.class)
class GetPartyStatusesIT extends AbstractAppTest {

	private static final String PATH = "/2281/party/a893dfa2-f781-49a9-9c5f-cba6353cf059/statuses";
	private static final String RESPONSE_FILE = "response.json";

	/**
	 * Test scenario where the party represents a private individual. Four cases are found in CaseManagement (one is a
	 * draft), one case is found in OpenE, one case is found in SupportManagement and two cases are found in OpenE awaiting
	 * multi-sign signing. One of the multi-sign cases shares its flowInstanceId with the regular OpenE case — verifies
	 * that the multi-sign entry wins the dedup. The other multi-sign case has a draft-like status — verifies that
	 * multi-sign cases bypass the draft filter even when includeDrafts is the default false.
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
	 * Test scenario where the party represents an enterprise. Four cases are found in CaseManagement and one case is found
	 * in OpenE.
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

	/**
	 * Test scenario where the party represents a private individual. Four cases are found in CaseManagement (one is a
	 * draft),
	 * one case is found in OpenE and one case is found in SupportManagement.
	 */
	@Test
	void test3_successful_private_includeDrafts() {
		setupCall()
			.withServicePath(PATH + "?includeDrafts=true")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	/**
	 * Test scenario where Open-E does not answer. The remaining sources still contribute, the request succeeds instead of
	 * failing outright, and the response names Open-E as unavailable so the caller can tell that the case list is
	 * incomplete rather than empty.
	 */
	@Test
	void test4_open_e_unavailable() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(UNAVAILABLE_SOURCES_HEADER, List.of(SOURCE_OPEN_E_PLATFORM))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

}
