package ${package}.service;

import ${package}.dto.CreateSampleRequest;
import ${package}.dto.SampleDto;
import ${package}.dto.UpdateSampleRequest;
import ${package}.entity.Sample;
import ${package}.exception.NotFoundException;
import ${package}.repository.SampleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SampleService {

    private final SampleRepository repo;

    public SampleService(SampleRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<SampleDto> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public SampleDto get(Long id) {
        var s = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Sample not found: " + id));
        return toDto(s);
    }

    @Transactional
    public SampleDto create(CreateSampleRequest req) {
        var s = new Sample();
        s.setName(req.name());
        s.setDescription(req.description());
        s.setActive(true);
        return toDto(repo.save(s));
    }

    @Transactional
    public Optional<SampleDto> update(Long id, UpdateSampleRequest req) {
        return repo.findById(id).map(s -> {
            if (req.description() != null) s.setDescription(req.description());
            if (req.active() != null) s.setActive(req.active());
            return toDto(repo.save(s));
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }

    private SampleDto toDto(Sample s) {
        return new SampleDto(s.getId(), s.getName(), s.getDescription(), s.getActive());
    }
}
