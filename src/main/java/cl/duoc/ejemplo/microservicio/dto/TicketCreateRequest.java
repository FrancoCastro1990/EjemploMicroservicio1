package cl.duoc.ejemplo.microservicio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketCreateRequest {
    private Long eventoId;
    private String usuarioId;
    private String usuarioNombre;
}
