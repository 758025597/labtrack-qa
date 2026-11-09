package pe.edu.upn.labtrack.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import pe.edu.upn.labtrack.model.Categoria;
import pe.edu.upn.labtrack.model.Equipo;
import pe.edu.upn.labtrack.model.EstadoEquipo;
import pe.edu.upn.labtrack.model.EstadoReserva;
import pe.edu.upn.labtrack.model.EstadoUsuario;
import pe.edu.upn.labtrack.model.Reserva;
import pe.edu.upn.labtrack.model.Rol;
import pe.edu.upn.labtrack.model.Usuario;
import pe.edu.upn.labtrack.repository.EquipoRepository;
import pe.edu.upn.labtrack.repository.ReservaRepository;
import pe.edu.upn.labtrack.repository.UsuarioRepository;
import pe.edu.upn.labtrack.service.CatalogoService;
import pe.edu.upn.labtrack.service.DevolucionService;
import pe.edu.upn.labtrack.service.ReglaNegocioException;
import pe.edu.upn.labtrack.service.ResultadoDevolucion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.edu.upn.labtrack.unit.DatosPrueba.h;
import static pe.edu.upn.labtrack.unit.RelojFijoConfig.HOY;

/** El reloj fijo marca las 10:00; las reservas se arman alrededor de esa hora. */
@DataJpaTest
@Import({RelojFijoConfig.class, DevolucionService.class, CatalogoService.class})
@DisplayName("HU-05 Devolución y penalización automática")
class DevolucionServiceTest {

    @Autowired
    private DevolucionService devolucionService;
    @Autowired
    private CatalogoService catalogoService;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private EquipoRepository equipos;
    @Autowired
    private ReservaRepository reservas;

    private Usuario ana;
    private Equipo laptop;

    @BeforeEach
    void datos() {
        ana = usuarios.save(new Usuario("N00123456", "Ana Torres", "x", Rol.ESTUDIANTE));
        laptop = equipos.save(new Equipo("EQ-001", "Laptop Dell", Categoria.COMPUTO));
    }

    private EstadoUsuario estadoDeAna() {
        return usuarios.findById(ana.getId()).orElseThrow().getEstado();
    }

    @Test
    @DisplayName("CP-36 / CP-37 Marcar devolución deja el equipo Disponible")
    void cp36y37EquipoDisponible() {
        Reserva r = DatosPrueba.reserva(reservas, ana, laptop, HOY, h(9), h(11), EstadoReserva.APROBADA);
        assertThat(catalogoService.estadoActual(laptop)).isEqualTo(EstadoEquipo.PRESTADO);

        devolucionService.marcarDevolucion(r.getId());

        assertThat(reservas.findById(r.getId()).orElseThrow().getEstado()).isEqualTo(EstadoReserva.DEVUELTA);
        assertThat(catalogoService.estadoActual(laptop)).isEqualTo(EstadoEquipo.DISPONIBLE);
    }

    @Test
    @DisplayName("CP-38 Devolución antes de la hora de fin: no penaliza")
    void cp38DevolucionATiempo() {
        Reserva r = DatosPrueba.reserva(reservas, ana, laptop, HOY, h(9), h(11), EstadoReserva.APROBADA);
        ResultadoDevolucion resultado = devolucionService.marcarDevolucion(r.getId());
        assertThat(resultado.isPenalizado()).isFalse();
        assertThat(estadoDeAna()).isEqualTo(EstadoUsuario.ACTIVO);
    }

    @Test
    @DisplayName("CP-39 Devolución después de la hora de fin: penaliza")
    void cp39DevolucionTardia() {
        Reserva r = DatosPrueba.reserva(reservas, ana, laptop, HOY, h(8), h(9, 45), EstadoReserva.APROBADA);
        ResultadoDevolucion resultado = devolucionService.marcarDevolucion(r.getId());
        assertThat(resultado.isPenalizado()).isTrue();
        assertThat(estadoDeAna()).isEqualTo(EstadoUsuario.PENALIZADO);
    }

    @Test
    @DisplayName("CP-40 Devolución exactamente a la hora de fin: no penaliza")
    void cp40DevolucionEnElLimite() {
        Reserva r = DatosPrueba.reserva(reservas, ana, laptop, HOY, h(8), h(10), EstadoReserva.APROBADA);
        ResultadoDevolucion resultado = devolucionService.marcarDevolucion(r.getId());
        assertThat(resultado.isPenalizado()).isFalse();
        assertThat(estadoDeAna()).isEqualTo(EstadoUsuario.ACTIVO);
    }

    @Test
    @DisplayName("CP-41 Reserva inexistente o ya devuelta no aparece / no se puede devolver")
    void cp41ReservaNoActiva() {
        Reserva r = DatosPrueba.reserva(reservas, ana, laptop, HOY, h(8), h(9), EstadoReserva.DEVUELTA);
        assertThat(devolucionService.activas("N00123456")).isEmpty();
        assertThatThrownBy(() -> devolucionService.marcarDevolucion(r.getId()))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
