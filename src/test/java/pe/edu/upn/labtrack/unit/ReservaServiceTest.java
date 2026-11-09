package pe.edu.upn.labtrack.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import pe.edu.upn.labtrack.model.Categoria;
import pe.edu.upn.labtrack.model.Equipo;
import pe.edu.upn.labtrack.model.EstadoReserva;
import pe.edu.upn.labtrack.model.Reserva;
import pe.edu.upn.labtrack.model.Rol;
import pe.edu.upn.labtrack.model.Usuario;
import pe.edu.upn.labtrack.repository.EquipoRepository;
import pe.edu.upn.labtrack.repository.ReservaRepository;
import pe.edu.upn.labtrack.repository.UsuarioRepository;
import pe.edu.upn.labtrack.service.CatalogoService;
import pe.edu.upn.labtrack.service.ReglaNegocioException;
import pe.edu.upn.labtrack.service.ReservaService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.edu.upn.labtrack.unit.DatosPrueba.h;
import static pe.edu.upn.labtrack.unit.RelojFijoConfig.HOY;
import static pe.edu.upn.labtrack.unit.RelojFijoConfig.MANANA;

@DataJpaTest
@Import({RelojFijoConfig.class, CatalogoService.class, ReservaService.class})
@DisplayName("HU-03 Reserva de equipos - reglas de negocio")
class ReservaServiceTest {

    @Autowired
    private ReservaService reservaService;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private EquipoRepository equipos;
    @Autowired
    private ReservaRepository reservas;

    private Usuario ana;
    private Usuario luis;

    @BeforeEach
    void datos() {
        ana = usuarios.save(new Usuario("N00123456", "Ana Torres", "x", Rol.ESTUDIANTE));
        luis = usuarios.save(new Usuario("N00654321", "Luis Rojas", "x", Rol.ESTUDIANTE));
        equipos.save(new Equipo("EQ-001", "Laptop Dell", Categoria.COMPUTO));
        equipos.save(new Equipo("EQ-003", "Raspberry Pi", Categoria.COMPUTO));
    }

    private Equipo eq(String codigo) {
        return equipos.findByCodigo(codigo).orElseThrow();
    }

    @Test
    @DisplayName("CP-18 Reserva exitosa queda en estado Pendiente")
    void cp18ReservaExitosa() {
        Reserva r = reservaService.crear(ana.getId(), "EQ-001", MANANA, h(10), h(12));
        assertThat(r.getEstado()).isEqualTo(EstadoReserva.PENDIENTE);
        assertThat(reservas.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("CP-19 No se puede reservar un equipo Prestado")
    void cp19EquipoPrestado() {
        DatosPrueba.reserva(reservas, luis, eq("EQ-001"), HOY, h(9), h(11), EstadoReserva.APROBADA);
        assertThatThrownBy(() -> reservaService.crear(ana.getId(), "EQ-001", MANANA, h(10), h(12)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no está disponible");
    }

    @Test
    @DisplayName("CP-20 Fecha u horario vacío")
    void cp20CamposVacios() {
        assertThatThrownBy(() -> reservaService.crear(ana.getId(), "EQ-001", MANANA, h(10), null))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("Debe completar");
    }

    @Test
    @DisplayName("CP-21 Hora de fin menor o igual a la de inicio")
    void cp21HoraFinInvalida() {
        assertThatThrownBy(() -> reservaService.crear(ana.getId(), "EQ-001", MANANA, h(12), h(10)))
                .hasMessageContaining("posterior a la hora de inicio");
        assertThatThrownBy(() -> reservaService.crear(ana.getId(), "EQ-001", MANANA, h(12), h(12)))
                .hasMessageContaining("posterior a la hora de inicio");
    }

    @Test
    @DisplayName("CP-22 Fecha u hora pasada")
    void cp22FechaPasada() {
        assertThatThrownBy(() -> reservaService.crear(ana.getId(), "EQ-001", HOY.minusDays(1), h(10), h(12)))
                .hasMessageContaining("pasada");
        assertThatThrownBy(() -> reservaService.crear(ana.getId(), "EQ-001", HOY, h(9), h(11)))
                .hasMessageContaining("pasada");
    }

    @Test
    @DisplayName("CP-23 Cruce total con reserva aprobada")
    void cp23CruceTotal() {
        DatosPrueba.reserva(reservas, luis, eq("EQ-001"), MANANA, h(10), h(12), EstadoReserva.APROBADA);
        assertThatThrownBy(() -> reservaService.crear(ana.getId(), "EQ-001", MANANA, h(10), h(12)))
                .hasMessageContaining("reserva aprobada que se cruza");
    }

    @Test
    @DisplayName("CP-24 Cruce parcial con reserva aprobada")
    void cp24CruceParcial() {
        DatosPrueba.reserva(reservas, luis, eq("EQ-001"), MANANA, h(10), h(12), EstadoReserva.APROBADA);
        assertThatThrownBy(() -> reservaService.crear(ana.getId(), "EQ-001", MANANA, h(11), h(13)))
                .hasMessageContaining("reserva aprobada que se cruza");
    }

    @Test
    @DisplayName("CP-25 Horario contiguo no es cruce")
    void cp25HorarioContiguo() {
        DatosPrueba.reserva(reservas, luis, eq("EQ-001"), MANANA, h(10), h(12), EstadoReserva.APROBADA);
        Reserva r = reservaService.crear(ana.getId(), "EQ-001", MANANA, h(12), h(14));
        assertThat(r.getId()).isNotNull();
    }

    @Test
    @DisplayName("CP-26 Cruce con reserva solo Pendiente está permitido")
    void cp26CruceConPendiente() {
        DatosPrueba.reserva(reservas, luis, eq("EQ-001"), MANANA, h(10), h(12), EstadoReserva.PENDIENTE);
        Reserva r = reservaService.crear(ana.getId(), "EQ-001", MANANA, h(10), h(12));
        assertThat(r.getEstado()).isEqualTo(EstadoReserva.PENDIENTE);
    }

    @Test
    @DisplayName("CP-27 Un estudiante no puede tener dos equipos a la vez")
    void cp27DosEquiposSimultaneos() {
        DatosPrueba.reserva(reservas, ana, eq("EQ-001"), MANANA, h(10), h(12), EstadoReserva.PENDIENTE);
        assertThatThrownBy(() -> reservaService.crear(ana.getId(), "EQ-003", MANANA, h(10), h(12)))
                .hasMessageContaining("Solo se permite un (1) equipo");
    }

    @Test
    @DisplayName("CP-28 Segunda reserva en horario distinto está permitida")
    void cp28HorarioDistinto() {
        DatosPrueba.reserva(reservas, ana, eq("EQ-001"), MANANA, h(10), h(12), EstadoReserva.APROBADA);
        Reserva r = reservaService.crear(ana.getId(), "EQ-003", MANANA, h(15), h(17));
        assertThat(r.getId()).isNotNull();
    }
}
