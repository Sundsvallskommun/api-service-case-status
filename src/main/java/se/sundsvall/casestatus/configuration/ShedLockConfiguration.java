package se.sundsvall.casestatus.configuration;

import javax.sql.DataSource;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;

@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT30M")
@ExcludeFromJacocoGeneratedCoverageReport
public class ShedLockConfiguration {

	@Bean
	LockProvider lockProvider(DataSource dataSource) {
		return new JdbcTemplateLockProvider(
			JdbcTemplateLockProvider.Configuration.builder()
				.usingDbTime()
				.withJdbcTemplate(new JdbcTemplate(dataSource))
				.build());
	}
}
