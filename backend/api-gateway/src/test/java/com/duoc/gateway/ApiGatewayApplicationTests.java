package com.duoc.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "eureka.client.register-with-eureka=false",
        "eureka.client.fetch-registry=false",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:1/certs"
})
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}
