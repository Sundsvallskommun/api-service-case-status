package se.sundsvall.casestatus.util.casestatuscache.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casestatus.util.ContextUtil;
import se.sundsvall.casestatus.util.casestatuscache.CaseStatusCache;

@ExtendWith(MockitoExtension.class)
class FamilyIdTest {

	private final FamilyId familyId = FamilyId.ROKKANALELDSTAD;

	@Mock
	private CaseStatusCache caseStatusCache;

	@Test
	void getValue_Test() {
		try (MockedStatic<ContextUtil> utilities = Mockito.mockStatic(ContextUtil.class)) {
			utilities.when(() -> ContextUtil.getBean(any())).thenReturn(caseStatusCache);
			when(caseStatusCache.isProduction()).thenReturn(true).thenReturn(false);
			int resultTrue = familyId.getValue();
			assertThat(resultTrue).isEqualTo(437);
			int resultFalse = familyId.getValue();
			assertThat(resultFalse).isEqualTo(382);
		}
	}
}
