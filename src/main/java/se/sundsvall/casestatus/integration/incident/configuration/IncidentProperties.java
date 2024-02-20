package se.sundsvall.casestatus.integration.incident.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "integration.incident")
public record IncidentProperties(int connectTimeout, int readTimeout) {

}
