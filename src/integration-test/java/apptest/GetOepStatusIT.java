package apptest;

import static apptest.CommonStubs.stubForAccessToken;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;

import se.sundsvall.casestatus.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

@Testcontainers
@WireMockAppTestSuite(files = "classpath:/GetOepStatusIT/", classes = Application.class)
@Sql(scripts = {"/db/truncate.sql", "/db/casestatus.sql"})
class GetOepStatusIT extends AbstractAppTest {

    private static final String PATH = "/1308f4ca-f7d6-4c88-9098-64be43b3a905/oepstatus";

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
    void test2_found_in_casemanagement_but_no_match_in_casestatus_db() {
        setupCall()
                .withServicePath(PATH)
                .withHttpMethod(HttpMethod.GET)
                .withExpectedResponseStatus(HttpStatus.OK)
                .withExpectedResponse("expected-response.json")
                .sendRequestAndVerifyResponse();
    }

    @Test
    void test3_found_in_casemanagement_but_no_match_in_casestatus_db_or_incident() {
        setupCall()
                .withServicePath(PATH)
                .withHttpMethod(HttpMethod.GET)
                .withExpectedResponseStatus(HttpStatus.NOT_FOUND)
                .withExpectedResponse("expected-response.json")
                .sendRequestAndVerifyResponse();
    }

    @Test
    void test4_not_found_in_casemanagement() {
        setupCall()
                .withServicePath(PATH)
                .withHttpMethod(HttpMethod.GET)
                .withExpectedResponseStatus(HttpStatus.NOT_FOUND)
                .withExpectedResponse("expected-response.json")
                .sendRequestAndVerifyResponse();
    }
}
