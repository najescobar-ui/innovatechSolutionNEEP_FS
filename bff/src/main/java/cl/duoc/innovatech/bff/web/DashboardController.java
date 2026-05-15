package cl.duoc.innovatech.bff.web;

import cl.duoc.innovatech.bff.domain.DashboardDto;
import cl.duoc.innovatech.bff.domain.UserRole;
import cl.duoc.innovatech.bff.service.DashboardDtoFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DashboardController {

    private final DashboardDtoFactory factory;

    public DashboardController(DashboardDtoFactory factory) {
        this.factory = factory;
    }

    // External path is /api/dashboard (gateway StripPrefix=1 turns it into
    // /dashboard before reaching this controller). The role param is a
    // placeholder until JWT-based extraction is wired through Keycloak.
    @GetMapping("/dashboard")
    public DashboardDto dashboard(@RequestParam UserRole role) {
        return factory.create(role);
    }
}
