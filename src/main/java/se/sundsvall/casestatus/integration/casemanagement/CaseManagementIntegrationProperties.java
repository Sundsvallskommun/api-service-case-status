package se.sundsvall.casestatus.integration.casemanagement;

import org.springframework.boot.context.properties.ConfigurationProperties;

import se.sundsvall.casestatus.integration.AbstractIntegrationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.case-management")
class CaseManagementIntegrationProperties extends AbstractIntegrationProperties {

    private OAuth2 oAuth2 = new OAuth2();
}
