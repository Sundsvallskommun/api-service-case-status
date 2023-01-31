package se.sundsvall.casestatus.integration.casemanagement;

import feign.Request;
import feign.codec.ErrorDecoder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.FeignBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import se.sundsvall.dept44.configuration.feign.FeignConfiguration;
import se.sundsvall.dept44.configuration.feign.FeignMultiCustomizer;
import se.sundsvall.dept44.configuration.feign.decoder.ProblemErrorDecoder;

import java.util.concurrent.TimeUnit;

@Import(FeignConfiguration.class)
@EnableConfigurationProperties(CaseManagementIntegrationProperties.class)
class CaseManagementIntegrationConfiguration {

    private final CaseManagementIntegrationProperties properties;

    CaseManagementIntegrationConfiguration(final CaseManagementIntegrationProperties properties) {
        this.properties = properties;
    }

    @Bean
    FeignBuilderCustomizer customizer() {
        return FeignMultiCustomizer.create()
                .withErrorDecoder(errorDecoder())
                .withRequestOptions(requestOptions())
                .withRetryableOAuth2InterceptorForClientRegistration(clientRegistration())
                .composeCustomizersToOne();
    }

    Request.Options requestOptions() {
        return new Request.Options(
                properties.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS,
                properties.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS,
                true);

    }

    ClientRegistration clientRegistration() {
        return ClientRegistration
                .withRegistrationId(CaseManagementIntegration.INTEGRATION_NAME)
                .tokenUri(properties.getOAuth2().getTokenUri())
                .clientId(properties.getOAuth2().getClientId())
                .clientSecret(properties.getOAuth2().getClientSecret())
                .authorizationGrantType(new AuthorizationGrantType(properties.getOAuth2().getGrantType()))
                .build();
    }

    ErrorDecoder errorDecoder() {
        return new ProblemErrorDecoder(CaseManagementIntegration.INTEGRATION_NAME);
    }
}
