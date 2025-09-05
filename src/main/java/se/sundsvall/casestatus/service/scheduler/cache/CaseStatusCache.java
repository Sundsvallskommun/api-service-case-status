package se.sundsvall.casestatus.service.scheduler.cache;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.casestatus.service.scheduler.cache.domain.FamilyId;
import se.sundsvall.dept44.requestid.RequestId;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

@Component
public class CaseStatusCache {

	private static final Logger LOG = LoggerFactory.getLogger(CaseStatusCache.class);

	private final CaseStatusCacheWorker caseStatusCacheWorker;

	@Value("${cache.isprod}")
	private boolean isProd;

	public CaseStatusCache(final CaseStatusCacheWorker caseStatusCacheWorker) {
		this.caseStatusCacheWorker = caseStatusCacheWorker;
	}

	@Dept44Scheduled(
		cron = "${cache.scheduled.cron}",
		name = "${cache.scheduled.name}",
		lockAtMostFor = "${cache.scheduled.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${cache.scheduled.maximum-execution-time}")
	public void scheduledCacheJob() {
		try {
			RequestId.init();
			LOG.info("CacheJob run started");

			Arrays.stream(FamilyId.values()).forEach(familyId -> {
				if (familyId.getValue() != 0) {
					caseStatusCacheWorker.cacheStatusesForFamilyId(familyId);
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
