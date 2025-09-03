package se.sundsvall.casestatus;

import static org.springframework.boot.SpringApplication.run;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import se.sundsvall.dept44.ServiceApplication;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;

@ServiceApplication
@EnableFeignClients
@EnableScheduling
@EnableCaching
@ExcludeFromJacocoGeneratedCoverageReport
public class Application {
	public static void main(String... args) {
		run(Application.class, args);
	}
}
