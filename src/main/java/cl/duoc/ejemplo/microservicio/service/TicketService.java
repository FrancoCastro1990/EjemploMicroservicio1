package cl.duoc.ejemplo.microservicio.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.ejemplo.microservicio.dto.EventoEstadisticasDto;
import cl.duoc.ejemplo.microservicio.dto.TicketCreateRequest;
import cl.duoc.ejemplo.microservicio.dto.TicketDto;
import cl.duoc.ejemplo.microservicio.dto.TicketUpdateRequest;
import cl.duoc.ejemplo.microservicio.entity.EstadoTicket;
import cl.duoc.ejemplo.microservicio.entity.Evento;
import cl.duoc.ejemplo.microservicio.entity.Ticket;
import cl.duoc.ejemplo.microservicio.repository.EventoRepository;
import cl.duoc.ejemplo.microservicio.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EventoRepository eventoRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final EfsService efsService;
    private final AwsS3Service awsS3Service;

    private static final String S3_BUCKET = "microservicio-eventos-s3";

    @Transactional
    public TicketDto generarTicket(TicketCreateRequest request) {
        log.info("Generando ticket para evento {} y usuario {}", request.getEventoId(), request.getUsuarioId());

        Evento evento = eventoRepository.findById(request.getEventoId())
                .orElseThrow(() -> new RuntimeException("Evento no encontrado: " + request.getEventoId()));

        long ticketsVendidos = ticketRepository.countByEventoId(evento.getId());
        if (ticketsVendidos >= evento.getCapacidadTotal()) {
            throw new RuntimeException("El evento ha alcanzado su capacidad maxima");
        }

        String codigoTicket = generarCodigoTicket(evento.getId(), request.getUsuarioId());

        Ticket ticket = Ticket.builder()
                .evento(evento)
                .usuarioId(request.getUsuarioId())
                .usuarioNombre(request.getUsuarioNombre())
                .codigoTicket(codigoTicket)
                .precio(evento.getPrecioBase())
                .estado(EstadoTicket.RESERVADO)
                .build();

        ticket = ticketRepository.save(ticket);

        byte[] pdfBytes = pdfGeneratorService.generateTicketPdf(ticket, evento);

        String s3Key = String.format("%d/%s/%s.pdf", evento.getId(), request.getUsuarioId(), codigoTicket);

        try {
            String efsFilename = String.format("temp/%s.pdf", codigoTicket);
            File efsFile = efsService.saveToEfs(efsFilename, pdfBytes);
            log.info("PDF guardado en EFS: {}", efsFile.getAbsolutePath());

            awsS3Service.uploadBytes(S3_BUCKET, s3Key, pdfBytes, "application/pdf");
            log.info("PDF subido a S3: {}", s3Key);

            efsService.deleteFromEfs(efsFilename);
        } catch (Exception e) {
            log.error("Error al procesar archivo del ticket", e);
            throw new RuntimeException("Error al procesar archivo del ticket", e);
        }

        ticket.setRutaArchivo(s3Key);
        ticket = ticketRepository.save(ticket);

        return TicketDto.fromEntity(ticket);
    }

    public List<TicketDto> obtenerTicketsPorEvento(Long eventoId) {
        return ticketRepository.findByEventoId(eventoId).stream()
                .map(TicketDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TicketDto> obtenerTicketsPorUsuario(String usuarioId) {
        return ticketRepository.findByUsuarioId(usuarioId).stream()
                .map(TicketDto::fromEntity)
                .collect(Collectors.toList());
    }

    public byte[] descargarTicket(String codigoTicket) {
        Ticket ticket = ticketRepository.findByCodigoTicket(codigoTicket)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + codigoTicket));

        if (ticket.getRutaArchivo() == null) {
            throw new RuntimeException("El ticket no tiene archivo asociado");
        }

        return awsS3Service.downloadAsBytes(S3_BUCKET, ticket.getRutaArchivo());
    }

    @Transactional
    public TicketDto modificarTicket(Long ticketId, TicketUpdateRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + ticketId));

        if (request.getUsuarioNombre() != null) {
            ticket.setUsuarioNombre(request.getUsuarioNombre());
        }

        if (request.getEstado() != null) {
            ticket.setEstado(request.getEstado());
        }

        ticket = ticketRepository.save(ticket);
        return TicketDto.fromEntity(ticket);
    }

    @Transactional
    public void eliminarTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket no encontrado: " + ticketId));

        if (ticket.getRutaArchivo() != null) {
            try {
                awsS3Service.deleteObject(S3_BUCKET, ticket.getRutaArchivo());
                log.info("Archivo eliminado de S3: {}", ticket.getRutaArchivo());
            } catch (Exception e) {
                log.warn("No se pudo eliminar archivo de S3: {}", ticket.getRutaArchivo(), e);
            }
        }

        ticketRepository.delete(ticket);
        log.info("Ticket eliminado: {}", ticketId);
    }

    public EventoEstadisticasDto obtenerEstadisticas(Long eventoId) {
        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado: " + eventoId));

        long totalTickets = ticketRepository.countByEventoId(eventoId);
        long ticketsConfirmados = ticketRepository.countByEventoIdAndEstado(eventoId, EstadoTicket.CONFIRMADO);
        long ticketsReservados = ticketRepository.countByEventoIdAndEstado(eventoId, EstadoTicket.RESERVADO);
        long ticketsCancelados = ticketRepository.countByEventoIdAndEstado(eventoId, EstadoTicket.CANCELADO);
        long ticketsUsados = ticketRepository.countByEventoIdAndEstado(eventoId, EstadoTicket.USADO);

        BigDecimal ingresoTotal = ticketRepository.sumIngresosByEventoId(eventoId);
        if (ingresoTotal == null) {
            ingresoTotal = BigDecimal.ZERO;
        }

        int capacidadDisponible = evento.getCapacidadTotal() - (int) (ticketsConfirmados + ticketsReservados + ticketsUsados);

        return EventoEstadisticasDto.builder()
                .eventoId(evento.getId())
                .nombreEvento(evento.getNombre())
                .totalTickets(totalTickets)
                .ticketsConfirmados(ticketsConfirmados)
                .ticketsReservados(ticketsReservados)
                .ticketsCancelados(ticketsCancelados)
                .ticketsUsados(ticketsUsados)
                .ingresoTotal(ingresoTotal)
                .capacidadTotal(evento.getCapacidadTotal())
                .capacidadDisponible(capacidadDisponible)
                .build();
    }

    private String generarCodigoTicket(Long eventoId, String usuarioId) {
        String uniquePart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("TKT-%d-%s-%s", eventoId, usuarioId.substring(0, Math.min(usuarioId.length(), 4)).toUpperCase(), uniquePart);
    }
}
