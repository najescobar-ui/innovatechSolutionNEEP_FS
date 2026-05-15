package cl.duoc.innovatech.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Backend-For-Frontend pattern: receives a single request from the SPA,
// orchestrates calls to N downstream microservices, and returns a single
// response shaped to the caller's role via Factory Method.
@SpringBootApplication
public class BffApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffApplication.class, args);
    }
}
