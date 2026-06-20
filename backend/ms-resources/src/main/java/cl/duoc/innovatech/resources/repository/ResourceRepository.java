package cl.duoc.innovatech.resources.repository;

import cl.duoc.innovatech.resources.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    boolean existsByEmail(String email);

    Optional<Resource> findByEmail(String email);
}
