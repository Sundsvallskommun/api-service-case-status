package se.sundsvall.casestatus.integration.casemanagement.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.case-management")
public record CaseManagementProperties(int connectTimeout, int readTimeout) {

}
