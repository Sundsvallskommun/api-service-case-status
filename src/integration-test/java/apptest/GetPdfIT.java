package apptest;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

import se.sundsvall.casestatus.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@WireMockAppTestSuite(files = "classpath:/GetPdfIT/", classes = Application.class)
@Sql(scripts = {"/db/truncate.sql", "/db/casestatus.sql"})
class GetPdfIT extends AbstractAppTest {

	private static final String MUNICIPALITY_ID = "2281";

	private static final String PATH = "/" + MUNICIPALITY_ID + "/1308f4ca-f7d6-4c88-9098-64be43b3a905/pdf";

	private static final String FAULTY_PATH = "/" + MUNICIPALITY_ID + "/fel/pdf";

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
	void test2_not_found_pdf_in_opene() {
		setupCall()
			.withServicePath(FAULTY_PATH)
			.withHttpMethod(HttpMethod.GET)
			.withExpectedResponseStatus(HttpStatus.NOT_FOUND)
			.withExpectedResponse("expected-response.json")
			.sendRequestAndVerifyResponse();
	}

}
