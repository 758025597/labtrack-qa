package pe.edu.upn.labtrack.service;

import org.springframework.stereotype.Service;
import pe.edu.upn.labtrack.model.Categoria;
import pe.edu.upn.labtrack.model.Equipo;
import pe.edu.upn.labtrack.model.EstadoEquipo;
import pe.edu.upn.labtrack.model.EstadoReserva;
import pe.edu.upn.labtrack.repository.EquipoRepository;
import pe.edu.upn.labtrack.repository.ReservaRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CatalogoService {

    private final EquipoRepository equipoRepository;
    private final ReservaRepository reservaRepository;
    private final Clock clock;

    public CatalogoService(EquipoRepository equipoRepository, ReservaRepository reservaRepository, Clock clock) {
        this.equipoRepository = equipoRepository;
        this.reservaRepository = reservaRepository;
        this.clock = clock;
    }

    public List<EquipoVista> listar(Categoria categoria) {
        List<Equipo> equipos = categoria == null
                ? equipoRepository.findAllByOrderByCodigoAsc()
                : equipoRepository.findByCategoriaOrderByCodigoAsc(categoria);
        Set<Long> prestados = idsPrestados();
        List<EquipoVista> resultado = new ArrayList<>();
        for (Equipo e : equipos) {
            resultado.add(new EquipoVista(e, prestados.contains(e.getId()) ? EstadoEquipo.PRESTADO : EstadoEquipo.DISPONIBLE));
        }
        return resultado;
    }

    public EstadoEquipo estadoActual(Equipo equipo) {
        return idsPrestados().contains(equipo.getId()) ? EstadoEquipo.PRESTADO : EstadoEquipo.DISPONIBLE;
    }

    private Set<Long> idsPrestados() {
        LocalDateTime ahora = LocalDateTime.now(clock);
        return new HashSet<>(reservaRepository.idsEquiposPrestados(
                EstadoReserva.APROBADA, ahora.toLocalDate(), ahora.toLocalTime()));
    }
}
