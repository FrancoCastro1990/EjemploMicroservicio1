package cl.duoc.ejemplo.microservicio.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import cl.duoc.ejemplo.microservicio.entity.Evento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoDto {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaEvento;
    private String ubicacion;
    private Integer capacidadTotal;
    private BigDecimal precioBase;

    public static EventoDto fromEntity(Evento evento) {
        return EventoDto.builder()
                .id(evento.getId())
                .nombre(evento.getNombre())
                .descripcion(evento.getDescripcion())
                .fechaEvento(evento.getFechaEvento())
                .ubicacion(evento.getUbicacion())
                .capacidadTotal(evento.getCapacidadTotal())
                .precioBase(evento.getPrecioBase())
                .build();
    }

    public Evento toEntity() {
        return Evento.builder()
                .nombre(this.nombre)
                .descripcion(this.descripcion)
                .fechaEvento(this.fechaEvento)
                .ubicacion(this.ubicacion)
                .capacidadTotal(this.capacidadTotal)
                .precioBase(this.precioBase)
                .build();
    }
}
