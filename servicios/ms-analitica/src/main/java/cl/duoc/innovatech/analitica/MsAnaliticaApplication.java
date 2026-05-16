package cl.duoc.innovatech.analitica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MsAnaliticaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsAnaliticaApplication.class, args);
    }
}
