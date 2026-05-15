package cl.duoc.innovatech.proyectos.service;

import cl.duoc.innovatech.proyectos.dto.CrearProyectoRequest;
import cl.duoc.innovatech.proyectos.dto.ProyectoDto;
import cl.duoc.innovatech.proyectos.entity.Proyecto;
import cl.duoc.innovatech.proyectos.repository.ProyectoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProyectoService {

    private final ProyectoRepository repository;

    public ProyectoService(ProyectoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ProyectoDto> listAll() {
        return repository.findAll().stream()
                .map(ProyectoDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProyectoDto> findById(Long id) {
        return repository.findById(id).map(ProyectoDto::fromEntity);
    }

    public ProyectoDto create(CrearProyectoRequest req) {
        Proyecto entity = new Proyecto(
                null,
                req.nombre(),
                req.descripcion(),
                req.estado(),
                req.fechaInicio(),
                req.fechaFinPlanificada(),
                req.responsableId()
        );
        return ProyectoDto.fromEntity(repository.save(entity));
    }
}
