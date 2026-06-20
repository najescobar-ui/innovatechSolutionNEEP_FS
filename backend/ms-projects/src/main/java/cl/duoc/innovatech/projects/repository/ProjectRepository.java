package cl.duoc.innovatech.projects.repository;

import cl.duoc.innovatech.projects.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
