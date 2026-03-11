package se.sundsvall.casestatus.integration.messaging.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.messaging")
public record MessagingProperties(int connectTimeout, int readTimeout, String channel, String token) {
}
