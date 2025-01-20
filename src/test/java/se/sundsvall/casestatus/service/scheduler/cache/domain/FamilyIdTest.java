package se.sundsvall.casestatus.service.scheduler.cache.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casestatus.service.scheduler.cache.CaseStatusCache;
import se.sundsvall.casestatus.service.scheduler.cache.ContextUtil;

@ExtendWith(MockitoExtension.class)
class FamilyIdTest {

	private final FamilyId familyId = FamilyId.ROKKANALELDSTAD;

	@Mock
	private CaseStatusCache caseStatusCache;

	@Test
	void getValue_Test() {
		try (final MockedStatic<ContextUtil> utilities = Mockito.mockStatic(ContextUtil.class)) {
			utilities.when(() -> ContextUtil.getBean(any())).thenReturn(caseStatusCache);
			when(caseStatusCache.isProduction()).thenReturn(true).thenReturn(false);
			final int resultTrue = familyId.getValue();
			assertThat(resultTrue).isEqualTo(437);
			final int resultFalse = familyId.getValue();
			assertThat(resultFalse).isEqualTo(382);
		}
	}
}
