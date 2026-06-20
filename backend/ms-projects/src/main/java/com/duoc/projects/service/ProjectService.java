package com.duoc.projects.service;

import com.duoc.projects.dto.UpdateProjectRequest;
import com.duoc.projects.dto.CreateProjectRequest;
import com.duoc.projects.dto.ProjectDto;
import com.duoc.projects.entity.Project;
import com.duoc.projects.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository repo;

    public ProjectService(ProjectRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> listAll() {
        return repo.findAll().stream().map(ProjectDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProjectDto> findById(Long id) {
        return repo.findById(id).map(ProjectDto::fromEntity);
    }

    public ProjectDto create(CreateProjectRequest req) {
        var p = new Project();
        p.setName(req.name());
        p.setDescription(req.description());
        p.setStatus(req.status());
        p.setStartDate(req.startDate());
        p.setPlannedEndDate(req.plannedEndDate());
        p.setOwnerId(req.ownerId());
        return ProjectDto.fromEntity(repo.save(p));
    }

    public boolean delete(Long id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }

    public Optional<ProjectDto> patch(Long id, UpdateProjectRequest req) {
        return repo.findById(id).map(p -> {
            if (req.status() != null) p.setStatus(req.status());
            /* ownerId puede llegar vacio para "limpiar"; solo lo ignoro si es null */
            if (req.ownerId() != null) {
                p.setOwnerId(req.ownerId().isBlank() ? null : req.ownerId());
            }
            return ProjectDto.fromEntity(repo.save(p));
        });
    }
}
