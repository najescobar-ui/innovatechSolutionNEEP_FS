package cl.duoc.innovatech.bff.service;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

// Passthrough: el shape lo decide ms-analitica. Si en algun momento hay
// que sumar campos en el BFF, conviene tipar a un record dedicado.
@Component
public class KpisClient {

    private final RestClient http;

    public KpisClient(@LoadBalanced RestClient.Builder builder) {
        this.http = builder.baseUrl("http://ms-analitica").build();
    }

    public Map<String, Object> obtener() {
        return http.get()
                .uri("/analitica/kpis")
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
