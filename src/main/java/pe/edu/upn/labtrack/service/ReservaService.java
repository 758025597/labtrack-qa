package pe.edu.upn.labtrack.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.upn.labtrack.model.Equipo;
import pe.edu.upn.labtrack.model.EstadoEquipo;
import pe.edu.upn.labtrack.model.EstadoReserva;
import pe.edu.upn.labtrack.model.Reserva;
import pe.edu.upn.labtrack.model.Usuario;
import pe.edu.upn.labtrack.repository.EquipoRepository;
import pe.edu.upn.labtrack.repository.ReservaRepository;
import pe.edu.upn.labtrack.repository.UsuarioRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private CatalogoService catalogoService;

    @Autowired
    private Clock clock;

    public Reserva crear(Long usuarioId, String codigoEquipo, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        if (fecha == null || inicio == null || fin == null) {
            throw new ReglaNegocioException("Debe completar la fecha, la hora de inicio y la hora de fin.");
        }
        if (!fin.isAfter(inicio)) {
            throw new ReglaNegocioException("La hora de fin debe ser posterior a la hora de inicio.");
        }
        LocalDateTime ahora = LocalDateTime.now(clock);
        if (LocalDateTime.of(fecha, inicio).isBefore(ahora)) {
            throw new ReglaNegocioException("No se puede reservar en una fecha u hora pasada.");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId).get();
        Equipo equipo = buscarEquipo(codigoEquipo);
        if (catalogoService.estadoActual(equipo) == EstadoEquipo.PRESTADO) {
            throw new ReglaNegocioException("El equipo no está disponible para reserva.");
        }

        // Regla CA-9: cruce con reservas aprobadas del equipo
        List<Reserva> aprobadas = reservaRepository.findByEquipoAndEstado(equipo, EstadoReserva.APROBADA);
        for (int i = 0; i < aprobadas.size(); i++) {
            Reserva r = aprobadas.get(i);
            if (r.getFecha().equals(fecha)) {
                if (r.getHoraInicio().isBefore(fin)) {
                    if (r.getHoraFin().isAfter(inicio)) {
                        throw new ReglaNegocioException("El equipo ya tiene una reserva aprobada que se cruza con ese horario.");
                    }
                }
            }
        }

        // Regla CA-10: el estudiante no puede tener mas de un equipo a la vez
        List<Reserva> delEstudiante = reservaRepository.findByUsuarioAndEstadoIn(usuario,
                Arrays.asList(EstadoReserva.PENDIENTE, EstadoReserva.APROBADA));
        for (int i = 0; i < delEstudiante.size(); i++) {
            Reserva r = delEstudiante.get(i);
            if (r.getFecha().equals(fecha)) {
                if (r.getHoraInicio().isBefore(fin)) {
                    if (r.getHoraFin().isAfter(inicio)) {
                        throw new ReglaNegocioException("Ya tienes un equipo reservado en ese horario. Solo se permite un (1) equipo de forma simultánea.");
                    }
                }
            }
        }

        Reserva reserva = new Reserva(usuario, equipo, fecha, inicio, fin, ahora);
        return reservaRepository.save(reserva);
    }

    public Equipo buscarEquipo(String codigoEquipo) {
        return equipoRepository.findByCodigo(codigoEquipo)
                .orElseThrow(() -> new ReglaNegocioException("El equipo no existe."));
    }

    public List<Reserva> delUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).get();
        return reservaRepository.findByUsuarioOrderByFechaDescHoraInicioDesc(usuario);
    }
}
