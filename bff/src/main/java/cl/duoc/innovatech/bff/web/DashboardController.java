package cl.duoc.innovatech.bff.web;

import cl.duoc.innovatech.bff.domain.DashboardDto;
import cl.duoc.innovatech.bff.domain.UserRole;
import cl.duoc.innovatech.bff.service.DashboardDtoFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

// El role sale de las authorities (las pobla JwtAuthoritiesConverter).
// Tomamos la primera que matchee con los roles validos de plataforma.
@RestController
public class DashboardController {

    private static final Set<String> VALIDOS = Set.of("PM", "DEV", "DIR");

    private final DashboardDtoFactory factory;

    public DashboardController(DashboardDtoFactory factory) {
        this.factory = factory;
    }

    @GetMapping("/dashboard")
    public DashboardDto dashboard(Authentication auth) {
        var role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .filter(VALIDOS::contains)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "usuario sin rol de plataforma (PM/DEV/DIR)"));
        return factory.create(UserRole.valueOf(role));
    }
}
