package se.sundsvall.casestatus.util.casestatuscache.domain;


import se.sundsvall.casestatus.util.ContextUtil;
import se.sundsvall.casestatus.util.casestatuscache.CaseStatusCache;

import java.util.EnumSet;

public enum FamilyId {
    ATTEFALLSATGARD(384, 427),
    ROKKANALELDSTAD(382, 437),
    FORHANDSBESKED(377, 428),
    STRANDSKYDDSDISPENS(396, 439),
    BESTALLAKARTUTDRAG(417, 255),
    NYBYGGNADSKARTA(37, 253),
    BYGGLOVPLANKSPALJE(383, 431),
    BYGGLOVTILLBYGGNAD(395, 443),
    RIVNINGANSOKANLOV(0, 12),
    ANMALANFORSELJNINGSERVERINGFOLKOL(378, 69),
    FORSALJNINGECIGGARETTER(379, 188),
    TILLSTANDFORSALJNINGTOBAKSVAROR(380, 187),
    TIPSBRISTERFORSALJNINGTOBAKSVAROR(0, 78),
    ANDRINGAVSLUTFORSALJNINGTOBAKSVAROR(381, 225);

    private static final EnumSet<FamilyId> APPLICANT = EnumSet.of(
            ROKKANALELDSTAD, FORHANDSBESKED, STRANDSKYDDSDISPENS, BYGGLOVPLANKSPALJE, BYGGLOVTILLBYGGNAD
    );
    private final int testValue;
    private final int prodValue;

    FamilyId(int testValue, int prodValue) {
        this.testValue = testValue;
        this.prodValue = prodValue;
    }

    public int getValue() {
        var bean = ContextUtil.getBean(CaseStatusCache.class);
        var isProd = bean.isProduction();
        return isProd ? prodValue : testValue;

    }

    public boolean isApplicant() {
        return APPLICANT.contains(this);
    }
}
