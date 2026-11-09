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

    /**
     * REF-08: el cruce de horarios se resuelve en la base de datos con una sola consulta
     * (antes se cargaban todas las reservas y se recorrían con bucles duplicados).
     * Dos bloques se cruzan si inicioA &lt; finB y finA &gt; inicioB (los bloques contiguos no se cruzan).
     */
    @Query("select r from Reserva r where r.equipo = :equipo and r.estado in :estados and r.fecha = :fecha "
            + "and r.horaInicio < :fin and r.horaFin > :inicio and r.id <> :excluirId")
    List<Reserva> cruzadasPorEquipo(@Param("equipo") Equipo equipo,
                                    @Param("estados") Collection<EstadoReserva> estados,
                                    @Param("fecha") LocalDate fecha,
                                    @Param("inicio") LocalTime inicio,
                                    @Param("fin") LocalTime fin,
                                    @Param("excluirId") Long excluirId);

    @Query("select r from Reserva r where r.usuario = :usuario and r.estado in :estados and r.fecha = :fecha "
            + "and r.horaInicio < :fin and r.horaFin > :inicio and r.id <> :excluirId")
    List<Reserva> cruzadasPorUsuario(@Param("usuario") Usuario usuario,
                                     @Param("estados") Collection<EstadoReserva> estados,
                                     @Param("fecha") LocalDate fecha,
                                     @Param("inicio") LocalTime inicio,
                                     @Param("fin") LocalTime fin,
                                     @Param("excluirId") Long excluirId);
}
