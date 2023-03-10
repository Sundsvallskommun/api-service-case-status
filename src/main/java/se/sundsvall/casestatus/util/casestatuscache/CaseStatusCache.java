package se.sundsvall.casestatus.util.casestatuscache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.util.casestatuscache.domain.FamilyId;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
@Component
public class CaseStatusCache {
    @Value("${cache.isprod}")
    private boolean isProd;

    private final CaseStatusCacheWorker caseStatusCacheWorker;

    private static final Logger LOG = LoggerFactory.getLogger(CaseStatusCacheWorker.class);

    public CaseStatusCache(CaseStatusCacheWorker caseStatusCacheWorker) {
        this.caseStatusCacheWorker = caseStatusCacheWorker;
    }

    @Scheduled(initialDelayString = "#{@caseStatusCacheProperties.getInitialdelay().toSeconds()}", fixedRateString = "#{@caseStatusCacheProperties.getFixedrate().toSeconds()}", timeUnit = TimeUnit.SECONDS)
    public void scheduledCacheJob() {
        LOG.info("CacheJob run started");

        Arrays.stream(FamilyId.values()).forEach(familyId -> {
                    if (familyId.getValue() != 0) {
                        caseStatusCacheWorker.cacheStatusesForFamilyID(familyId);
                    }
                }

        );
        var result = mergeCaseStatusCache();
        LOG.info("CacheJob run completed, {} rows were affected", result);
    }

    private int mergeCaseStatusCache() {
        return caseStatusCacheWorker.mergeCaseStatusCache();
    }

    public boolean isProduction() {
        return isProd;
    }

}
