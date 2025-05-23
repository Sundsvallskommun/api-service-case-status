package apptest;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

import org.junit.jupiter.api.Test;
import se.sundsvall.casestatus.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/GetErrandStatusesIT/", classes = Application.class)
class GetErrandStatusesIT extends AbstractAppTest {

	private static final String PATH = "/2281/errands/statuses";

	private static final String RESPONSE_FILE = "response.json";

	@Test
	void test1_getErrandsByErrandNumber() {
		setupCall()
			.withServicePath(PATH + "?errandNumber=Case123")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test2_getErrandsByPropertyDesignation() {
		setupCall()
			.withServicePath(PATH + "?propertyDesignation=Kattegatt 123")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(OK)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_noRequestParameters() {
		setupCall()
			.withServicePath(PATH)
			.withHttpMethod(GET)
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test4_bothRequestParameters() {
		setupCall()
			.withServicePath(PATH + "?errandNumber=Case123&propertyDesignation=Kattegatt 123")
			.withHttpMethod(GET)
			.withExpectedResponseStatus(BAD_REQUEST)
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse();
	}

}
