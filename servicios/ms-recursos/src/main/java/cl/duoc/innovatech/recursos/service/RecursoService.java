package cl.duoc.innovatech.recursos.service;

import cl.duoc.innovatech.recursos.dto.CrearRecursoRequest;
import cl.duoc.innovatech.recursos.dto.RecursoDto;
import cl.duoc.innovatech.recursos.entity.Recurso;
import cl.duoc.innovatech.recursos.repository.RecursoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RecursoService {

    private final RecursoRepository repository;

    public RecursoService(RecursoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RecursoDto> listar() {
        return repository.findAll().stream().map(RecursoService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public RecursoDto obtener(Long id) {
        return repository.findById(id)
                .map(RecursoService::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Recurso no encontrado: " + id));
    }

    @Transactional
    public RecursoDto crear(CrearRecursoRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email ya registrado: " + request.email());
        }
        var entity = new Recurso(
                null,
                request.nombre(),
                request.email(),
                request.rol(),
                request.horasSemanales(),
                request.competencias(),
                true
        );
        return toDto(repository.save(entity));
    }

    private static RecursoDto toDto(Recurso entity) {
        return new RecursoDto(
                entity.getId(),
                entity.getNombre(),
                entity.getEmail(),
                entity.getRol(),
                entity.getHorasSemanales(),
                entity.getCompetencias(),
                entity.getActivo()
        );
    }
}
