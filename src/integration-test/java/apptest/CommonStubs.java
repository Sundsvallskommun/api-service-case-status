package apptest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public final class CommonStubs {

    private CommonStubs() { }

    public static void stubForAccessToken() {
        stubForAccessToken("/token");
    }

    public static void stubForAccessToken(final String url) {
        stubFor(post(url)
            .willReturn(aResponse()
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"access_token\":\"abc123\",\"not-before-policy\":0,\"session_state\":\"88bbf486\",\"token_type\": \"bearer\"}")));
    }
}
