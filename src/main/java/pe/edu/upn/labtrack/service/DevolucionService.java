package pe.edu.upn.labtrack.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upn.labtrack.model.EstadoReserva;
import pe.edu.upn.labtrack.model.EstadoUsuario;
import pe.edu.upn.labtrack.model.Reserva;
import pe.edu.upn.labtrack.model.Usuario;
import pe.edu.upn.labtrack.repository.ReservaRepository;
import pe.edu.upn.labtrack.repository.UsuarioRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DevolucionService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock clock;

    public DevolucionService(ReservaRepository reservaRepository, UsuarioRepository usuarioRepository, Clock clock) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.clock = clock;
    }

    /** Reservas activas (aprobadas y aún no devueltas), filtradas por código/nombre de estudiante o código de equipo. */
    public List<Reserva> activas(String busqueda) {
        List<Reserva> activas = reservaRepository.findByEstadoOrderByFechaAscHoraInicioAsc(EstadoReserva.APROBADA);
        if (busqueda == null || busqueda.isBlank()) {
            return activas;
        }
        String q = busqueda.trim().toLowerCase();
        List<Reserva> filtradas = new ArrayList<>();
        for (Reserva r : activas) {
            if (r.getUsuario().getCodigo().toLowerCase().contains(q)
                    || r.getUsuario().getNombre().toLowerCase().contains(q)
                    || r.getEquipo().getCodigo().toLowerCase().contains(q)) {
                filtradas.add(r);
            }
        }
        return filtradas;
    }

    @Transactional
    public ResultadoDevolucion marcarDevolucion(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReglaNegocioException("La reserva no existe."));
        if (reserva.getEstado() != EstadoReserva.APROBADA) {
            throw new ReglaNegocioException("La reserva no está activa.");
        }
        LocalDateTime ahora = LocalDateTime.now(clock);
        reserva.setEstado(EstadoReserva.DEVUELTA);
        reserva.setFechaDevolucion(ahora);
        reservaRepository.save(reserva);

        // Regla CA-16: devolución posterior a la hora de fin => estudiante penalizado
        boolean penalizado = ahora.isAfter(reserva.fin());
        if (penalizado) {
            Usuario estudiante = reserva.getUsuario();
            estudiante.setEstado(EstadoUsuario.PENALIZADO);
            usuarioRepository.save(estudiante);
        }
        return new ResultadoDevolucion(reserva, penalizado);
    }
}
