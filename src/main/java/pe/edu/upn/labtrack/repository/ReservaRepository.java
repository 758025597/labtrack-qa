package pe.edu.upn.labtrack.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upn.labtrack.model.Equipo;
import pe.edu.upn.labtrack.model.EstadoReserva;
import pe.edu.upn.labtrack.model.Reserva;
import pe.edu.upn.labtrack.model.Usuario;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByEquipoAndEstado(Equipo equipo, EstadoReserva estado);

    List<Reserva> findByUsuarioAndEstadoIn(Usuario usuario, Collection<EstadoReserva> estados);

    List<Reserva> findByEstadoOrderByFechaAscHoraInicioAsc(EstadoReserva estado);

    List<Reserva> findByUsuarioOrderByFechaDescHoraInicioDesc(Usuario usuario);

    /**
     * Equipos que están prestados en este momento: tienen una reserva aprobada,
     * no devuelta, cuyo bloque horario ya empezó.
     */
    @Query("select distinct r.equipo.id from Reserva r where r.estado = :estado "
            + "and (r.fecha < :hoy or (r.fecha = :hoy and r.horaInicio <= :ahora))")
    List<Long> idsEquiposPrestados(@Param("estado") EstadoReserva estado,
                                   @Param("hoy") LocalDate hoy,
                                   @Param("ahora") LocalTime ahora);
}
