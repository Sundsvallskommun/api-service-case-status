package se.sundsvall.casestatus;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import se.sundsvall.dept44.ServiceApplication;
import se.sundsvall.dept44.util.jacoco.ExcludeFromJacocoGeneratedCoverageReport;

import static org.springframework.boot.SpringApplication.run;

@ServiceApplication
@EnableFeignClients
@EnableScheduling
@ExcludeFromJacocoGeneratedCoverageReport
public class Application {
	public static void main(String... args) {
		run(Application.class, args);
	}
}
