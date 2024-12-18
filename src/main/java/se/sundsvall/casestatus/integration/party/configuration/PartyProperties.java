package se.sundsvall.casestatus.integration.party.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.party")
public record PartyProperties(int connectTimeout, int readTimeout) {
}
