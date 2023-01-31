package se.sundsvall.casestatus.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CasePdfResponseTests {

    @Test
    void builderAndGettersWorkAsExpected() {
        var response = CasePdfResponse.builder()
            .withExternalCaseId("someExternalCaseId")
            .withBase64("someBase64String")
            .build();

        assertThat(response.getExternalCaseId()).isEqualTo("someExternalCaseId");
        assertThat(response.getBase64()).isEqualTo("someBase64String");
    }
}
