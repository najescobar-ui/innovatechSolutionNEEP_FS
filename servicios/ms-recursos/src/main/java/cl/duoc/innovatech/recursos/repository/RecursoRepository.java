package cl.duoc.innovatech.recursos.repository;

import cl.duoc.innovatech.recursos.entity.Recurso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecursoRepository extends JpaRepository<Recurso, Long> {

    boolean existsByEmail(String email);
}
