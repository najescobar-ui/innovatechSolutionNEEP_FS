package cl.duoc.innovatech.proyectos.repository;

import cl.duoc.innovatech.proyectos.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;

// Repository Pattern (internal docs §6): Spring Data JPA generates the
// implementation at runtime. Services depend on this interface, not on
// Hibernate or JDBC.
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
}
