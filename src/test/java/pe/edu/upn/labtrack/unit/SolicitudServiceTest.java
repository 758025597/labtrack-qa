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
import pe.edu.upn.labtrack.service.ReglaNegocioException;
import pe.edu.upn.labtrack.service.ValidadorCruces;
import pe.edu.upn.labtrack.service.SolicitudService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.edu.upn.labtrack.unit.DatosPrueba.h;
import static pe.edu.upn.labtrack.unit.RelojFijoConfig.MANANA;

@DataJpaTest
@Import({RelojFijoConfig.class, SolicitudService.class, ValidadorCruces.class})
@DisplayName("HU-04 Gestión de solicitudes")
class SolicitudServiceTest {

    @Autowired
    private SolicitudService solicitudService;
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

    @Test
    @DisplayName("CP-30 Lista solo las solicitudes pendientes")
    void cp30ListaPendientes() {
        DatosPrueba.reserva(reservas, ana, laptop, MANANA, h(10), h(12), EstadoReserva.PENDIENTE);
        DatosPrueba.reserva(reservas, ana, laptop, MANANA, h(14), h(15), EstadoReserva.RECHAZADA);
        assertThat(solicitudService.pendientes()).hasSize(1);
    }

    @Test
    @DisplayName("CP-32 Aprobar cambia el estado a Aprobada")
    void cp32Aprobar() {
        Reserva r = DatosPrueba.reserva(reservas, ana, laptop, MANANA, h(10), h(12), EstadoReserva.PENDIENTE);
        solicitudService.aprobar(r.getId());
        assertThat(reservas.findById(r.getId()).orElseThrow().getEstado()).isEqualTo(EstadoReserva.APROBADA);
    }

    @Test
    @DisplayName("CP-29 Dos solicitudes del mismo equipo y horario no pueden aprobarse ambas (REF-06)")
    void cp29NoAprobarDosVeces() {
        Usuario luis = usuarios.save(new Usuario("N00654321", "Luis Rojas", "x", Rol.ESTUDIANTE));
        Reserva deAna = DatosPrueba.reserva(reservas, ana, laptop, MANANA, h(10), h(12), EstadoReserva.PENDIENTE);
        Reserva deLuis = DatosPrueba.reserva(reservas, luis, laptop, MANANA, h(11), h(13), EstadoReserva.PENDIENTE);

        solicitudService.aprobar(deAna.getId());
        assertThatThrownBy(() -> solicitudService.aprobar(deLuis.getId()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("reserva aprobada que se cruza");
        assertThat(reservas.findById(deLuis.getId()).orElseThrow().getEstado()).isEqualTo(EstadoReserva.PENDIENTE);
    }

    @Test
    @DisplayName("CP-33 Rechazar sin motivo no está permitido")
    void cp33RechazarSinMotivo() {
        Reserva r = DatosPrueba.reserva(reservas, ana, laptop, MANANA, h(10), h(12), EstadoReserva.PENDIENTE);
        assertThatThrownBy(() -> solicitudService.rechazar(r.getId(), "   "))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("motivo");
        assertThat(reservas.findById(r.getId()).orElseThrow().getEstado()).isEqualTo(EstadoReserva.PENDIENTE);
    }

    @Test
    @DisplayName("CP-34 Rechazar con motivo guarda la justificación")
    void cp34RechazarConMotivo() {
        Reserva r = DatosPrueba.reserva(reservas, ana, laptop, MANANA, h(10), h(12), EstadoReserva.PENDIENTE);
        solicitudService.rechazar(r.getId(), "Equipo en mantenimiento");
        Reserva guardada = reservas.findById(r.getId()).orElseThrow();
        assertThat(guardada.getEstado()).isEqualTo(EstadoReserva.RECHAZADA);
        assertThat(guardada.getMotivoRechazo()).isEqualTo("Equipo en mantenimiento");
    }
}
