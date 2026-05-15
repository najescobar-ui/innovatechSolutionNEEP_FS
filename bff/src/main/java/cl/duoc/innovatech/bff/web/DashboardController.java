package cl.duoc.innovatech.bff.web;

import cl.duoc.innovatech.bff.domain.DashboardDto;
import cl.duoc.innovatech.bff.domain.UserRole;
import cl.duoc.innovatech.bff.service.DashboardDtoFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// URL externa: /api/dashboard. El gateway hace StripPrefix=1.
// El role viene por query hasta que metamos JWT.
@RestController
public class DashboardController {

    private final DashboardDtoFactory factory;

    public DashboardController(DashboardDtoFactory factory) {
        this.factory = factory;
    }

    @GetMapping("/dashboard")
    public DashboardDto dashboard(@RequestParam UserRole role) {
        return factory.create(role);
    }
}
