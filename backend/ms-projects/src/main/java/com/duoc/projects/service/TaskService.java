package com.duoc.projects.service;

import com.duoc.projects.dto.CreateTaskRequest;
import com.duoc.projects.dto.TaskDto;
import com.duoc.projects.dto.UpdateTaskRequest;
import com.duoc.projects.entity.Task;
import com.duoc.projects.entity.TaskStatus;
import com.duoc.projects.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TaskService {

    private final TaskRepository repo;

    public TaskService(TaskRepository repo) {
        this.repo = repo;
    }

    /**
     * Lista tareas aplicando los filtros que vengan no-null. El dataset es
     * pequeno, asi que se filtra en memoria para soportar cualquier combinacion.
     */
    @Transactional(readOnly = true)
    public List<TaskDto> list(Long projectId, Long assigneeResourceId, TaskStatus status) {
        return repo.findAll().stream()
                .filter(t -> projectId == null || projectId.equals(t.getProjectId()))
                .filter(t -> assigneeResourceId == null || assigneeResourceId.equals(t.getAssigneeResourceId()))
                .filter(t -> status == null || status == t.getStatus())
                .map(TaskDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<TaskDto> findById(Long id) {
        return repo.findById(id).map(TaskDto::fromEntity);
    }

    public TaskDto create(CreateTaskRequest req) {
        var t = new Task();
        t.setProjectId(req.projectId());
        t.setTitle(req.title());
        t.setDescription(req.description());
        t.setStatus(req.status() != null ? req.status() : TaskStatus.TODO);
        t.setAssigneeResourceId(req.assigneeResourceId());
        t.setEstimatedHours(req.estimatedHours() != null ? req.estimatedHours() : 0);
        t.setDueDate(req.dueDate());
        return TaskDto.fromEntity(repo.save(t));
    }

    public boolean delete(Long id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }

    public Optional<TaskDto> patch(Long id, UpdateTaskRequest req) {
        return repo.findById(id).map(t -> {
            if (req.title() != null) t.setTitle(req.title());
            if (req.description() != null) t.setDescription(req.description());
            if (req.status() != null) t.setStatus(req.status());
            if (req.assigneeResourceId() != null) t.setAssigneeResourceId(req.assigneeResourceId());
            if (req.estimatedHours() != null) t.setEstimatedHours(req.estimatedHours());
            if (req.dueDate() != null) t.setDueDate(req.dueDate());
            return TaskDto.fromEntity(repo.save(t));
        });
    }
}
