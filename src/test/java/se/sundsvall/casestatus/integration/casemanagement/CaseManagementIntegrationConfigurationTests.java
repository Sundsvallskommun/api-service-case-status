package se.sundsvall.casestatus.integration.casemanagement;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("junit")
@SpringBootTest(classes = CaseManagementIntegrationConfiguration.class)
class CaseManagementIntegrationConfigurationTests {
    
    @Autowired
    private FeignBuilderCustomizer feignBuilderCustomizer;
    
    @Autowired
    private CaseManagementIntegrationProperties properties;
    
    @Test
    void feignBuilderCustomizerBeanIsCreated() {
        assertThat(feignBuilderCustomizer).isNotNull();
    }
    
    @Test
    void propertiesBeanIsCreatedAndHasCorrectValues() {
        assertThat(properties).isNotNull();
        assertThat(properties.getBaseUrl()).isEqualTo("http://case-management.url");
        assertThat(properties.getOAuth2()).isNotNull().satisfies(oAuth2 -> {
            assertThat(oAuth2.getTokenUri()).isEqualTo("http://token.url");
            assertThat(oAuth2.getClientId()).isEqualTo("someClientId");
            assertThat(oAuth2.getClientSecret()).isEqualTo("someClientSecret");
            assertThat(oAuth2.getGrantType()).isEqualTo("client_credentials");
        });
        assertThat(properties.getConnectTimeout()).isEqualTo(Duration.ofSeconds(9));
        assertThat(properties.getReadTimeout()).isEqualTo(Duration.ofSeconds(10));
    }
}
