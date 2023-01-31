package se.sundsvall.casestatus.util.casestatuscache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties("cache.scheduled")
public class CaseStatusCacheProperties {
    private Duration initialdelay;
    private Duration fixedrate;
}
