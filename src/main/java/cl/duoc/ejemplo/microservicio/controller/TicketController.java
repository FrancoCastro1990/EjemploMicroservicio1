package cl.duoc.ejemplo.microservicio.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ejemplo.microservicio.dto.EventoEstadisticasDto;
import cl.duoc.ejemplo.microservicio.dto.TicketCreateRequest;
import cl.duoc.ejemplo.microservicio.dto.TicketDto;
import cl.duoc.ejemplo.microservicio.dto.TicketUpdateRequest;
import cl.duoc.ejemplo.microservicio.service.TicketService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketDto> generarTicket(@RequestBody TicketCreateRequest request) {
        TicketDto ticket = ticketService.generarTicket(request);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/evento/{eventoId}")
    public ResponseEntity<List<TicketDto>> obtenerTicketsPorEvento(@PathVariable Long eventoId) {
        List<TicketDto> tickets = ticketService.obtenerTicketsPorEvento(eventoId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<TicketDto>> obtenerTicketsPorUsuario(@PathVariable String usuarioId) {
        List<TicketDto> tickets = ticketService.obtenerTicketsPorUsuario(usuarioId);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{codigoTicket}/download")
    public ResponseEntity<byte[]> descargarTicket(@PathVariable String codigoTicket) {
        byte[] pdfBytes = ticketService.descargarTicket(codigoTicket);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + codigoTicket + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PutMapping("/{ticketId}")
    public ResponseEntity<TicketDto> modificarTicket(
            @PathVariable Long ticketId,
            @RequestBody TicketUpdateRequest request) {
        TicketDto ticket = ticketService.modificarTicket(ticketId, request);
        return ResponseEntity.ok(ticket);
    }

    @DeleteMapping("/{ticketId}")
    public ResponseEntity<Void> eliminarTicket(@PathVariable Long ticketId) {
        ticketService.eliminarTicket(ticketId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estadisticas/evento/{eventoId}")
    public ResponseEntity<EventoEstadisticasDto> obtenerEstadisticas(@PathVariable Long eventoId) {
        EventoEstadisticasDto estadisticas = ticketService.obtenerEstadisticas(eventoId);
        return ResponseEntity.ok(estadisticas);
    }
}
