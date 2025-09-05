package se.sundsvall.casestatus.service.scheduler.cache.domain;

import lombok.Getter;
import se.sundsvall.casestatus.service.scheduler.cache.CaseStatusCache;
import se.sundsvall.casestatus.service.scheduler.cache.ContextUtil;

public enum FamilyId {

	ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR(381, 225, "2281"),
	ANMALANFORSELJNINGSERVERINGFOLKOL(378, 69, "2281"),
	TILLSTANDFORSALJNINGTOBAKSVAROR(380, 187, "2281");

	private final int testValue;
	private final int prodValue;

	@Getter
	private final String municipalityId;

	FamilyId(final int testValue, final int prodValue, final String municipalityId) {
		this.testValue = testValue;
		this.prodValue = prodValue;
		this.municipalityId = municipalityId;
	}

	public int getValue() {
		final var bean = ContextUtil.getBean(CaseStatusCache.class);
		final var isProd = bean.isProduction();

		return isProd ? prodValue : testValue;
	}
}
