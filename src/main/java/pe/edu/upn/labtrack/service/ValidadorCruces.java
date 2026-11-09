package pe.edu.upn.labtrack.service;

import org.springframework.stereotype.Component;
import pe.edu.upn.labtrack.model.Equipo;
import pe.edu.upn.labtrack.model.EstadoReserva;
import pe.edu.upn.labtrack.model.Usuario;
import pe.edu.upn.labtrack.repository.ReservaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

/**
 * REF-08: reglas de cruce de horarios en un solo lugar, reutilizadas al crear y al aprobar reservas.
 */
@Component
public class ValidadorCruces {

    public static final String MSG_CRUCE_EQUIPO = "El equipo ya tiene una reserva aprobada que se cruza con ese horario.";
    public static final String MSG_CRUCE_ESTUDIANTE =
            "Ya tienes un equipo reservado en ese horario. Solo se permite un (1) equipo de forma simultánea.";

    /** Id que nunca existe: se usa cuando la reserva aún no se ha guardado. */
    public static final long SIN_EXCLUIR = -1L;

    private static final Set<EstadoReserva> SOLO_APROBADAS = EnumSet.of(EstadoReserva.APROBADA);
    private static final Set<EstadoReserva> VIGENTES = EnumSet.of(EstadoReserva.PENDIENTE, EstadoReserva.APROBADA);

    private final ReservaRepository reservaRepository;

    public ValidadorCruces(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    /** CA-9: no puede cruzarse con una reserva APROBADA del mismo equipo. */
    public void validarEquipo(Equipo equipo, LocalDate fecha, LocalTime inicio, LocalTime fin, long excluirId) {
        if (!reservaRepository.cruzadasPorEquipo(equipo, SOLO_APROBADAS, fecha, inicio, fin, excluirId).isEmpty()) {
            throw new ReglaNegocioException(MSG_CRUCE_EQUIPO);
        }
    }

    /** CA-10: un estudiante no puede tener más de un equipo reservado al mismo tiempo. */
    public void validarEstudiante(Usuario usuario, LocalDate fecha, LocalTime inicio, LocalTime fin, long excluirId) {
        if (!reservaRepository.cruzadasPorUsuario(usuario, VIGENTES, fecha, inicio, fin, excluirId).isEmpty()) {
            throw new ReglaNegocioException(MSG_CRUCE_ESTUDIANTE);
        }
    }
}
