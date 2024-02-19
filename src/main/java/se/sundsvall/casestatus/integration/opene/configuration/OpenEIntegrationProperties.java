package se.sundsvall.casestatus.integration.opene.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.open-e")
public record OpenEIntegrationProperties(
	String username,
	String password,
	int connectTimeout,
	int readTimeout
) {

}
