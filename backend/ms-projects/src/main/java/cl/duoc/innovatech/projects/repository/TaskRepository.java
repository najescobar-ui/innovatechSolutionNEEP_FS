package cl.duoc.innovatech.projects.repository;

import cl.duoc.innovatech.projects.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
