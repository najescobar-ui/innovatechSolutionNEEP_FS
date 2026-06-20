package cl.duoc.innovatech.resources.service;

import cl.duoc.innovatech.resources.dto.UpdateResourceRequest;
import cl.duoc.innovatech.resources.dto.CreateResourceRequest;
import cl.duoc.innovatech.resources.dto.ResourceDto;
import cl.duoc.innovatech.resources.entity.Resource;
import cl.duoc.innovatech.resources.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ResourceService {

    private final ResourceRepository repo;

    public ResourceService(ResourceRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<ResourceDto> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public ResourceDto get(Long id) {
        var r = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recurso no encontrado: " + id));
        return toDto(r);
    }

    @Transactional
    public boolean delete(Long id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }

    @Transactional
    public Optional<ResourceDto> update(Long id, UpdateResourceRequest req) {
        return repo.findById(id).map(r -> {
            if (req.active() != null) r.setActive(req.active());
            return toDto(repo.save(r));
        });
    }

    @Transactional
    public ResourceDto create(CreateResourceRequest req) {
        if (repo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email ya registrado: " + req.email());
        }
        var r = new Resource();
        r.setName(req.name());
        r.setEmail(req.email());
        r.setRole(req.role());
        r.setWeeklyHours(req.weeklyHours());
        r.setSkills(req.skills());
        r.setActive(true);
        return toDto(repo.save(r));
    }

    private ResourceDto toDto(Resource r) {
        return new ResourceDto(
                r.getId(),
                r.getName(),
                r.getEmail(),
                r.getRole(),
                r.getWeeklyHours(),
                r.getSkills(),
                r.getActive()
        );
    }
}
