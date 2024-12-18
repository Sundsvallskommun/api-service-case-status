package se.sundsvall.casestatus.service.scheduler;

import java.util.Arrays;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.service.scheduler.domain.FamilyId;
import se.sundsvall.dept44.requestid.RequestId;

@Component
public class CaseStatusCache {

	private static final Logger LOG = LoggerFactory.getLogger(CaseStatusCache.class);

	private final CaseStatusCacheWorker caseStatusCacheWorker;

	@Value("${cache.isprod}")
	private boolean isProd;

	public CaseStatusCache(final CaseStatusCacheWorker caseStatusCacheWorker) {
		this.caseStatusCacheWorker = caseStatusCacheWorker;
	}

	@Scheduled(cron = "${cache.scheduled.cron}")
	@SchedulerLock(name = "cache_job", lockAtMostFor = "${cache.scheduled.shedlock-lock-at-most-for}")
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
