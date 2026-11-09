package pe.edu.upn.labtrack.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.upn.labtrack.model.EstadoReserva;
import pe.edu.upn.labtrack.model.Reserva;
import pe.edu.upn.labtrack.repository.ReservaRepository;

import java.util.List;

@Service
public class SolicitudService {

    @Autowired
    private ReservaRepository reservaRepository;

    public List<Reserva> pendientes() {
        return reservaRepository.findByEstadoOrderByFechaAscHoraInicioAsc(EstadoReserva.PENDIENTE);
    }

    public Reserva aprobar(Long id) {
        Reserva reserva = reservaRepository.findById(id).get();
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ReglaNegocioException("La solicitud ya fue atendida.");
        }
        reserva.setEstado(EstadoReserva.APROBADA);
        return reservaRepository.save(reserva);
    }

    public Reserva rechazar(Long id, String motivo) {
        Reserva reserva = reservaRepository.findById(id).get();
        if (reserva.getEstado() != EstadoReserva.PENDIENTE) {
            throw new ReglaNegocioException("La solicitud ya fue atendida.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaNegocioException("Debe ingresar el motivo del rechazo.");
        }
        reserva.setEstado(EstadoReserva.RECHAZADA);
        reserva.setMotivoRechazo(motivo.trim());
        return reservaRepository.save(reserva);
    }
}
