package se.sundsvall.casestatus.service.scheduler.cache.domain;

import java.util.EnumSet;
import lombok.Getter;
import se.sundsvall.casestatus.service.scheduler.cache.CaseStatusCache;
import se.sundsvall.casestatus.service.scheduler.cache.ContextUtil;

public enum FamilyId {
	ATTEFALLSATGARD(384, 427, "2281"),
	ROKKANALELDSTAD(382, 437, "2281"),
	FORHANDSBESKED(377, 428, "2281"),
	STRANDSKYDDSDISPENS(396, 439, "2281"),
	BYGGLOVPLANKSPALJE(383, 431, "2281"),
	BYGGLOVTILLBYGGNAD(395, 443, "2281"),
	RIVNINGANSOKANLOV(12, 716, "2281"),
	ANMALANFORSELJNINGSERVERINGFOLKOL(378, 69, "2281"),
	FORSALJNINGECIGGARETTER(379, 188, "2281"),
	TILLSTANDFORSALJNINGTOBAKSVAROR(380, 187, "2281"),
	ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR(381, 225, "2281");

	private static final EnumSet<FamilyId> APPLICANT = EnumSet.of(
		ROKKANALELDSTAD, FORHANDSBESKED, STRANDSKYDDSDISPENS, BYGGLOVPLANKSPALJE, BYGGLOVTILLBYGGNAD);

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

	public boolean isApplicant() {
		return APPLICANT.contains(this);
	}
}
