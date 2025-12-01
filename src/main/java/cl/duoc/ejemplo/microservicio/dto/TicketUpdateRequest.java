package cl.duoc.ejemplo.microservicio.dto;

import cl.duoc.ejemplo.microservicio.entity.EstadoTicket;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketUpdateRequest {
    private String usuarioNombre;
    private EstadoTicket estado;
}
