package cl.duoc.innovatech.projects.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Tarea de un proyecto. assigneeResourceId es una referencia logica al recurso
 * en ms-resources (sin FK fisica entre BD de servicios), igual que owner_id en
 * Project. Mapea la tabla `tasks` (ver V003__schema_tasks.sql).
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TaskStatus status;

    @Column(name = "assignee_resource_id")
    private Long assigneeResourceId;

    @Column(name = "estimated_hours", nullable = false)
    private int estimatedHours;

    @Column(name = "due_date")
    private LocalDate dueDate;
}
