package cl.duoc.ejemplo.microservicio.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoEstadisticasDto {
    private Long eventoId;
    private String nombreEvento;
    private Long totalTickets;
    private Long ticketsConfirmados;
    private Long ticketsReservados;
    private Long ticketsCancelados;
    private Long ticketsUsados;
    private BigDecimal ingresoTotal;
    private Integer capacidadTotal;
    private Integer capacidadDisponible;
}
