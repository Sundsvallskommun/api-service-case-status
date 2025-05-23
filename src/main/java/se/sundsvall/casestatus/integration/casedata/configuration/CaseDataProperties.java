package se.sundsvall.casestatus.integration.casedata.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.case-data")
public record CaseDataProperties(int connectTimeout, int readTimeout) {
}
