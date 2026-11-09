package pe.edu.upn.labtrack.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upn.labtrack.model.Equipo;
import pe.edu.upn.labtrack.model.EstadoEquipo;
import pe.edu.upn.labtrack.model.Reserva;
import pe.edu.upn.labtrack.model.Usuario;
import pe.edu.upn.labtrack.repository.EquipoRepository;
import pe.edu.upn.labtrack.repository.ReservaRepository;
import pe.edu.upn.labtrack.repository.UsuarioRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EquipoRepository equipoRepository;
    private final CatalogoService catalogoService;
    private final ValidadorCruces validadorCruces;
    private final Clock clock;

    public ReservaService(ReservaRepository reservaRepository, UsuarioRepository usuarioRepository,
                          EquipoRepository equipoRepository, CatalogoService catalogoService,
                          ValidadorCruces validadorCruces, Clock clock) {
        this.reservaRepository = reservaRepository;
        this.usuarioRepository = usuarioRepository;
        this.equipoRepository = equipoRepository;
        this.catalogoService = catalogoService;
        this.validadorCruces = validadorCruces;
        this.clock = clock;
    }

    /** HU-03: registra una solicitud de reserva en estado Pendiente. */
    @Transactional
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
        Usuario usuario = buscarUsuario(usuarioId);
        Equipo equipo = buscarEquipo(codigoEquipo);
        if (catalogoService.estadoActual(equipo) == EstadoEquipo.PRESTADO) {
            throw new ReglaNegocioException("El equipo no está disponible para reserva.");
        }
        validadorCruces.validarEquipo(equipo, fecha, inicio, fin, ValidadorCruces.SIN_EXCLUIR);
        validadorCruces.validarEstudiante(usuario, fecha, inicio, fin, ValidadorCruces.SIN_EXCLUIR);

        return reservaRepository.save(new Reserva(usuario, equipo, fecha, inicio, fin, ahora));
    }

    public Equipo buscarEquipo(String codigoEquipo) {
        return equipoRepository.findByCodigo(codigoEquipo)
                .orElseThrow(() -> new ReglaNegocioException("El equipo no existe."));
    }

    public List<Reserva> delUsuario(Long usuarioId) {
        return reservaRepository.findByUsuarioOrderByFechaDescHoraInicioDesc(buscarUsuario(usuarioId));
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ReglaNegocioException("El usuario no existe."));
    }
}
