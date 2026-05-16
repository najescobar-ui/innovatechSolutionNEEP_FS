package cl.duoc.innovatech.recursos.service;

import cl.duoc.innovatech.recursos.dto.ActualizarRecursoRequest;
import cl.duoc.innovatech.recursos.dto.CrearRecursoRequest;
import cl.duoc.innovatech.recursos.dto.RecursoDto;
import cl.duoc.innovatech.recursos.entity.Recurso;
import cl.duoc.innovatech.recursos.repository.RecursoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RecursoService {

    private final RecursoRepository repo;

    public RecursoService(RecursoRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<RecursoDto> listar() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public RecursoDto obtener(Long id) {
        var r = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recurso no encontrado: " + id));
        return toDto(r);
    }

    @Transactional
    public boolean eliminar(Long id) {
        if (!repo.existsById(id)) return false;
        repo.deleteById(id);
        return true;
    }

    @Transactional
    public Optional<RecursoDto> actualizar(Long id, ActualizarRecursoRequest req) {
        return repo.findById(id).map(r -> {
            if (req.activo() != null) r.setActivo(req.activo());
            return toDto(repo.save(r));
        });
    }

    @Transactional
    public RecursoDto crear(CrearRecursoRequest req) {
        if (repo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email ya registrado: " + req.email());
        }
        var r = new Recurso();
        r.setNombre(req.nombre());
        r.setEmail(req.email());
        r.setRol(req.rol());
        r.setHorasSemanales(req.horasSemanales());
        r.setCompetencias(req.competencias());
        r.setActivo(true);
        return toDto(repo.save(r));
    }

    private RecursoDto toDto(Recurso r) {
        return new RecursoDto(
                r.getId(),
                r.getNombre(),
                r.getEmail(),
                r.getRol(),
                r.getHorasSemanales(),
                r.getCompetencias(),
                r.getActivo()
        );
    }
}
