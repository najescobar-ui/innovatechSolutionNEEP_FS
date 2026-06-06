package cl.duoc.innovatech.bff.service;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

// Passthrough: el shape lo decide ms-analytics. Si en algun momento hay
// que sumar campos en el BFF, conviene tipar a un record dedicado.
@Component
public class KpisClient {

    private final RestClient http;

    public KpisClient(@LoadBalanced RestClient.Builder builder) {
        this.http = builder.baseUrl("http://ms-analytics").build();
    }

    public Map<String, Object> get() {
        return http.get()
                .uri("/analytics/kpis")
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Map<String, Object> history(int points, String from, String to) {
        return http.get()
                .uri(uri -> {
                    var b = uri.path("/analytics/kpis/history").queryParam("points", points);
                    if (from != null && !from.isBlank()) b.queryParam("from", from);
                    if (to != null && !to.isBlank()) b.queryParam("to", to);
                    return b.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
}
