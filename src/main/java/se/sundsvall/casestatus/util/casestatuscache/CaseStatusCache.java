package se.sundsvall.casestatus.util.casestatuscache;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import se.sundsvall.casestatus.util.casestatuscache.domain.FamilyId;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import se.sundsvall.dept44.requestid.RequestId;

@Configuration
@EnableScheduling
@Component
public class CaseStatusCache {

	private static final Logger LOG = LoggerFactory.getLogger(CaseStatusCache.class);

	private final CaseStatusCacheWorker caseStatusCacheWorker;

	@Value("${cache.isprod}")
	private boolean isProd;

	public CaseStatusCache(final CaseStatusCacheWorker caseStatusCacheWorker) {
		this.caseStatusCacheWorker = caseStatusCacheWorker;
	}

	@SchedulerLock(name = "cache_job", lockAtMostFor = "${cache.scheduled.shedlock-lock-at-most-for}")
	@Scheduled(cron = "${cache.scheduled.cron}")
	public void scheduledCacheJob() {
		try {
			RequestId.init();
			LOG.info("CacheJob run started");

			Arrays.stream(FamilyId.values()).forEach(familyId -> {
				if (familyId.getValue() != 0) {
					caseStatusCacheWorker.cacheStatusesForFamilyID(familyId);
				}
			});
		} finally {
			RequestId.reset();
		}
	}

	public boolean isProduction() {
		return isProd;
	}

}
