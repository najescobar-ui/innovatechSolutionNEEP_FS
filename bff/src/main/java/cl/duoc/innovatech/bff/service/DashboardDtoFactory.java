package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.DashboardDto;
import cl.duoc.innovatech.bff.domain.UserRole;
import org.springframework.stereotype.Component;

import java.util.List;

// Datos hardcoded por ahora. Cuando esten integrados ms-recursos/ms-analitica,
// esto va a recibir los agregados y solo armar el shape correspondiente.
@Component
public class DashboardDtoFactory {

    public DashboardDto create(UserRole role) {
        return switch (role) {
            case PM  -> new DashboardDto.PMDashboard("PM", 4, 2,
                    List.of("MVP demo 2026-06-15", "Release v1 2026-07-30"));
            case DEV -> new DashboardDto.DevDashboard("DEV", 12, 5,
                    List.of("Innovatech Platform", "Acme migration"));
            case DIR -> new DashboardDto.DirDashboard("DIR", 23, 0.78, 1);
        };
    }
}
