package cl.duoc.innovatech.bff.service;

import cl.duoc.innovatech.bff.domain.DashboardDto;
import cl.duoc.innovatech.bff.domain.UserRole;
import org.springframework.stereotype.Component;

import java.util.List;

// Factory Method (internal docs §6): the controller stays oblivious to the
// concrete DTO class — it just hands the role over and gets back a
// `DashboardDto`. The caller's serializer (Jackson) sees the actual record
// type at runtime and emits the right JSON shape.
//
// Stub payloads for now. Once ms-proyectos / ms-recursos / ms-analitica
// exist, this class will accept aggregated data and only do the shaping.
@Component
public class DashboardDtoFactory {

    public DashboardDto create(UserRole role) {
        return switch (role) {
            case PM -> new DashboardDto.PMDashboard(
                    "PM",
                    4,
                    2,
                    List.of("MVP demo 2026-06-15", "Release v1 2026-07-30")
            );
            case DEV -> new DashboardDto.DevDashboard(
                    "DEV",
                    12,
                    5,
                    List.of("Innovatech Platform", "Acme migration")
            );
            case DIR -> new DashboardDto.DirDashboard(
                    "DIR",
                    23,
                    0.78,
                    1
            );
        };
    }
}
