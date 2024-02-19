package se.sundsvall.casestatus;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

import se.sundsvall.dept44.ServiceApplication;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;

@ServiceApplication
@EnableFeignClients
@EnableSchedulerLock(defaultLockAtMostFor = "PT30M")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
