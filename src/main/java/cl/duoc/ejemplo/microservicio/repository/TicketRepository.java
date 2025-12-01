package cl.duoc.ejemplo.microservicio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import cl.duoc.ejemplo.microservicio.entity.EstadoTicket;
import cl.duoc.ejemplo.microservicio.entity.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByEventoId(Long eventoId);

    List<Ticket> findByUsuarioId(String usuarioId);

    List<Ticket> findByEventoIdAndUsuarioId(Long eventoId, String usuarioId);

    Optional<Ticket> findByCodigoTicket(String codigoTicket);

    long countByEventoIdAndEstado(Long eventoId, EstadoTicket estado);

    long countByEventoId(Long eventoId);

    @Query("SELECT COALESCE(SUM(t.precio), 0) FROM Ticket t WHERE t.evento.id = :eventoId AND t.estado IN ('CONFIRMADO', 'USADO')")
    java.math.BigDecimal sumIngresosByEventoId(@Param("eventoId") Long eventoId);
}
