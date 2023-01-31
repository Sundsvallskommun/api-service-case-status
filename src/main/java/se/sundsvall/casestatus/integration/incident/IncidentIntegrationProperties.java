package se.sundsvall.casestatus.integration.incident;

import org.springframework.boot.context.properties.ConfigurationProperties;

import se.sundsvall.casestatus.integration.AbstractIntegrationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.incident")
class IncidentIntegrationProperties extends AbstractIntegrationProperties {

    private OAuth2 oAuth2 = new OAuth2();
}
