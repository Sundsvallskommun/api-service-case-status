package se.sundsvall.casestatus.integration.opene.soap.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("integration.open-e.callback")
public record OpenECallbackProperties(int connectTimeout, int readTimeout, String username,
	String externalPassword, String internalPassword) {
}
