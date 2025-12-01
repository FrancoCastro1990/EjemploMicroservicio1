package cl.duoc.ejemplo.microservicio.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.ejemplo.microservicio.entity.Evento;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {
}
