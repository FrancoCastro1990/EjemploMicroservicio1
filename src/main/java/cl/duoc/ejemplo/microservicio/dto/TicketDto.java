package cl.duoc.ejemplo.microservicio.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import cl.duoc.ejemplo.microservicio.entity.EstadoTicket;
import cl.duoc.ejemplo.microservicio.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDto {
    private Long id;
    private Long eventoId;
    private String eventoNombre;
    private String usuarioId;
    private String usuarioNombre;
    private String codigoTicket;
    private LocalDateTime fechaCompra;
    private BigDecimal precio;
    private EstadoTicket estado;
    private String rutaArchivo;
    private LocalDateTime fechaModificacion;

    public static TicketDto fromEntity(Ticket ticket) {
        return TicketDto.builder()
                .id(ticket.getId())
                .eventoId(ticket.getEvento().getId())
                .eventoNombre(ticket.getEvento().getNombre())
                .usuarioId(ticket.getUsuarioId())
                .usuarioNombre(ticket.getUsuarioNombre())
                .codigoTicket(ticket.getCodigoTicket())
                .fechaCompra(ticket.getFechaCompra())
                .precio(ticket.getPrecio())
                .estado(ticket.getEstado())
                .rutaArchivo(ticket.getRutaArchivo())
                .fechaModificacion(ticket.getFechaModificacion())
                .build();
    }
}
