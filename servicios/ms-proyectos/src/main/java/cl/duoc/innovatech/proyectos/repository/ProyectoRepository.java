package cl.duoc.innovatech.proyectos.repository;

import cl.duoc.innovatech.proyectos.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
}
