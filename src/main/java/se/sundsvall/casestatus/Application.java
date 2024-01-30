package se.sundsvall.casestatus;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;

import se.sundsvall.dept44.ServiceApplication;

@ServiceApplication

@EnableSchedulerLock(defaultLockAtMostFor = "PT30M")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
