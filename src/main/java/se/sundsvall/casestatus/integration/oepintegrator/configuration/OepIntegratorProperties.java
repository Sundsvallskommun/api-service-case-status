package se.sundsvall.casestatus.integration.oepintegrator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.oep-integrator")
public record OepIntegratorProperties(int connectTimeout, int readTimeout) {
}
