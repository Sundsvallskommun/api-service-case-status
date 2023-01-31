package se.sundsvall.casestatus.integration.opene;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import se.sundsvall.casestatus.integration.AbstractIntegrationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "integration.open-e")
class OpenEIntegrationProperties extends AbstractIntegrationProperties {

    private BasicAuth basicAuth = new BasicAuth();
    private int port;
    private String scheme;
}
