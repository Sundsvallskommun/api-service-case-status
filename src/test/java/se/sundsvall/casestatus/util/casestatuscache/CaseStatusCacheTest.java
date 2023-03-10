package se.sundsvall.casestatus.util.casestatuscache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import se.sundsvall.casestatus.util.ContextUtil;
import se.sundsvall.casestatus.util.casestatuscache.domain.FamilyId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CaseStatusCacheTest {

    @Mock
    private CaseStatusCacheWorker caseStatusCacheWorker;

    @InjectMocks
    private CaseStatusCache caseStatusCache;


    @Test
    void isProduction_test() {
        ReflectionTestUtils.setField(caseStatusCache, "isProd", true);
        var result = caseStatusCache.isProduction();
        assertThat(result).isTrue();
    }

    @Test
    void scheduledJob() {
        ReflectionTestUtils.setField(caseStatusCache, "isProd", false);
        try (MockedStatic<ContextUtil> utilities = Mockito.mockStatic(ContextUtil.class)) {
            utilities.when(() -> ContextUtil.getBean(any())).thenReturn(caseStatusCache);
            caseStatusCache.scheduledCacheJob();
            verify(caseStatusCacheWorker, times(12)).cacheStatusesForFamilyID(any(FamilyId.class));
        }


    }


}