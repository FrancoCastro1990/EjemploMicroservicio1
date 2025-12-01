package cl.duoc.ejemplo.microservicio.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ejemplo.microservicio.dto.EventoDto;
import cl.duoc.ejemplo.microservicio.entity.Evento;
import cl.duoc.ejemplo.microservicio.repository.EventoRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoRepository eventoRepository;

    @GetMapping
    public ResponseEntity<List<EventoDto>> listarEventos() {
        List<EventoDto> eventos = eventoRepository.findAll().stream()
                .map(EventoDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(eventos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoDto> obtenerEvento(@PathVariable Long id) {
        return eventoRepository.findById(id)
                .map(EventoDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<EventoDto> crearEvento(@RequestBody EventoDto eventoDto) {
        Evento evento = eventoDto.toEntity();
        evento = eventoRepository.save(evento);
        return ResponseEntity.ok(EventoDto.fromEntity(evento));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoDto> actualizarEvento(@PathVariable Long id, @RequestBody EventoDto eventoDto) {
        return eventoRepository.findById(id)
                .map(evento -> {
                    evento.setNombre(eventoDto.getNombre());
                    evento.setDescripcion(eventoDto.getDescripcion());
                    evento.setFechaEvento(eventoDto.getFechaEvento());
                    evento.setUbicacion(eventoDto.getUbicacion());
                    evento.setCapacidadTotal(eventoDto.getCapacidadTotal());
                    evento.setPrecioBase(eventoDto.getPrecioBase());
                    evento = eventoRepository.save(evento);
                    return ResponseEntity.ok(EventoDto.fromEntity(evento));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEvento(@PathVariable Long id) {
        if (eventoRepository.existsById(id)) {
            eventoRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
