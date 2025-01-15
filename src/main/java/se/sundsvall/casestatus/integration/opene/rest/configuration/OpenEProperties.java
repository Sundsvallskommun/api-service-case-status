package se.sundsvall.casestatus.integration.opene.rest.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.open-e")
public record OpenEProperties(
	String username,
	String password,
	int connectTimeout,
	int readTimeout) {

}
