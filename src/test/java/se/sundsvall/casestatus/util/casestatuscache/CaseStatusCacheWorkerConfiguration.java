package se.sundsvall.casestatus.util.casestatuscache;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CaseStatusCacheWorkerConfiguration {
	@Bean
	@Primary
	public CaseStatusCacheWorker createMock() {
		return Mockito.mock(CaseStatusCacheWorker.class);
	}
}
