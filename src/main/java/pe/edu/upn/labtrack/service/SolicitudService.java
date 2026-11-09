package pe.edu.upn.labtrack.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upn.labtrack.model.EstadoReserva;
import pe.edu.upn.labtrack.model.Reserva;
import pe.edu.upn.labtrack.repository.ReservaRepository;

import java.util.List;

@Service
public class SolicitudService {

    private static final String MSG_YA_ATENDIDA = "La solicitud ya fue atendida.";

    private final ReservaRepository reservaRepository;
    private final ValidadorCruces validadorCruces;

    public SolicitudService(ReservaRepository reservaRepository, ValidadorCruces validadorCruces) {
        this.reservaRepository = reservaRepository;
        this.validadorCruces = validadorCruces;
    }

    public List<Reserva> pendientes() {
        return reservaRepository.findByEstadoOrderByFechaAscHoraInicioAsc(EstadoReserva.PENDIENTE);
    }

    /**
     * CA-12. REF-06: antes de aprobar se vuelve a validar el cruce con otras reservas aprobadas,
     * así dos solicitudes pendientes del mismo equipo y horario no pueden quedar aprobadas a la vez (CP-29).
     */
    @Transactional
    public Reserva aprobar(Long id) {
        Reserva reserva = buscarPendiente(id);
        validadorCruces.validarEquipo(reserva.getEquipo(), reserva.getFecha(),
                reserva.getHoraInicio(), reserva.getHoraFin(), reserva.getId());
        reserva.setEstado(EstadoReserva.APROBADA);
        return reservaRepository.save(reserva);
    }

    /** CA-13: el rechazo exige un motivo. */
    @Transactional
    public Reserva rechazar(Long id, String motivo) {
        Reserva reserva = buscarPendiente(id);
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaNegocioException("Debe ingresar el motivo del rechazo.");
        }
        reserva.setEstado(EstadoReserva.RECHAZADA);
        reserva.setMotivoRechazo(motivo.trim());
        return reservaRepository.save(reserva);
    }

    private Reserva buscarPendiente(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("La solicitud no existe."));
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ReglaNegocioException(MSG_YA_ATENDIDA);
        }
        return reserva;
    }
}
