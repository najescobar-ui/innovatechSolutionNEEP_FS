package cl.duoc.innovatech.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// API Gateway pattern: single entry point for external clients.
// Routes are declared in application.yml using `lb://<service-id>` URIs,
// resolved through the Eureka client.
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
