package se.sundsvall.casestatus.configuration;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

	public static final String MDC_EXECUTOR = "mdcAwareExecutor";

	@Bean(name = MDC_EXECUTOR)
	Executor mdcAwareExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(20);
		executor.setQueueCapacity(50);
		executor.setThreadNamePrefix("MDC-Async-");

		executor.setTaskDecorator(new MDCTaskDecorator());

		executor.initialize();
		return executor;
	}
}
