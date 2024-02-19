package se.sundsvall.casestatus.integration.citizen.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.citizen")
public record CitizenProperties(int connectTimeout, int readTimeout) {

}
