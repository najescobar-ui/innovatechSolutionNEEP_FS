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

    private final ProyectoRepository repo;

    public ProyectoService(ProyectoRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<ProyectoDto> listAll() {
        return repo.findAll().stream().map(ProyectoDto::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public Optional<ProyectoDto> findById(Long id) {
        return repo.findById(id).map(ProyectoDto::fromEntity);
    }

    public ProyectoDto create(CrearProyectoRequest req) {
        var p = new Proyecto();
        p.setNombre(req.nombre());
        p.setDescripcion(req.descripcion());
        p.setEstado(req.estado());
        p.setFechaInicio(req.fechaInicio());
        p.setFechaFinPlanificada(req.fechaFinPlanificada());
        p.setResponsableId(req.responsableId());
        return ProyectoDto.fromEntity(repo.save(p));
    }
}
