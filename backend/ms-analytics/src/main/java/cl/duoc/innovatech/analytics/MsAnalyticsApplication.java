package cl.duoc.innovatech.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MsAnalyticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsAnalyticsApplication.class, args);
    }
}
