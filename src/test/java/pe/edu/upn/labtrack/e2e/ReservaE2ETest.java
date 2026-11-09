package pe.edu.upn.labtrack.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import pe.edu.upn.labtrack.model.EstadoReserva;
import pe.edu.upn.labtrack.model.Reserva;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HU-03 Reserva de equipos - flujo crítico (Selenium)")
class ReservaE2ETest extends BaseE2ETest {

    private static final By MENSAJE_ERROR = By.id("mensaje-error");

    private void abrirFormulario(String codigoEquipo) {
        login(ANA, CLAVE_ESTUDIANTE);
        abrir("/catalogo");
        driver.findElement(By.id("reservar-" + codigoEquipo)).click();
    }

    @Test
    @DisplayName("CP-18 Reserva exitosa de equipo disponible")
    void cp18ReservaExitosa() {
        abrirFormulario("EQ-001");
        completarReserva(manana(), "10:00", "12:00");

        assertThat(texto(By.id("mensaje-exito"))).contains("Reserva registrada correctamente").contains("Pendiente");
        List<Reserva> reservas = reservaRepository.findAll();
        assertThat(reservas).hasSize(1);
        assertThat(reservas.get(0).getEstado()).isEqualTo(EstadoReserva.PENDIENTE);
    }

    @Test
    @DisplayName("CP-19 No se puede reservar un equipo Prestado")
    void cp19EquipoPrestado() {
        crearPrestamoActivo(MARIA, "EQ-002");
        login(ANA, CLAVE_ESTUDIANTE);
        abrir("/catalogo");
        assertThat(estadoEnCatalogo("EQ-002")).isEqualTo("Prestado");
        assertThat(existe(By.id("reservar-EQ-002"))).isFalse();

        abrir("/reservas/nueva?equipo=EQ-002");
        assertThat(texto(MENSAJE_ERROR)).contains("no está disponible");
        assertThat(existe(By.id("btn-reservar"))).isFalse();
    }

    @Test
    @DisplayName("CP-20 Reserva sin hora de fin")
    void cp20CamposVacios() {
        abrirFormulario("EQ-001");
        completarReserva(manana(), "10:00", null);
        assertThat(texto(MENSAJE_ERROR)).contains("Debe completar");
        assertThat(reservaRepository.count()).isZero();
    }

    @Test
    @DisplayName("CP-21 Hora de fin anterior a la hora de inicio")
    void cp21HorarioInvalido() {
        abrirFormulario("EQ-001");
        completarReserva(manana(), "12:00", "10:00");
        assertThat(texto(MENSAJE_ERROR)).contains("posterior a la hora de inicio");
        assertThat(reservaRepository.count()).isZero();
    }

    @Test
    @DisplayName("CP-23 Cruce total con una reserva aprobada")
    void cp23CruceTotal() {
        crearReserva(LUIS, "EQ-001", manana(), LocalTime.of(10, 0), LocalTime.of(12, 0), EstadoReserva.APROBADA);
        abrirFormulario("EQ-001");
        completarReserva(manana(), "10:00", "12:00");
        assertThat(texto(MENSAJE_ERROR)).contains("reserva aprobada que se cruza");
        assertThat(reservaRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("CP-24 Cruce parcial con una reserva aprobada")
    void cp24CruceParcial() {
        crearReserva(LUIS, "EQ-001", manana(), LocalTime.of(10, 0), LocalTime.of(12, 0), EstadoReserva.APROBADA);
        abrirFormulario("EQ-001");
        completarReserva(manana(), "11:00", "13:00");
        assertThat(texto(MENSAJE_ERROR)).contains("reserva aprobada que se cruza");
    }

    @Test
    @DisplayName("CP-25 Horario contiguo está permitido")
    void cp25HorarioContiguo() {
        crearReserva(LUIS, "EQ-001", manana(), LocalTime.of(10, 0), LocalTime.of(12, 0), EstadoReserva.APROBADA);
        abrirFormulario("EQ-001");
        completarReserva(manana(), "12:00", "14:00");
        assertThat(texto(By.id("mensaje-exito"))).contains("Reserva registrada correctamente");
        assertThat(reservaRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("CP-27 Un estudiante no puede reservar dos equipos a la vez")
    void cp27DosEquiposSimultaneos() {
        crearReserva(ANA, "EQ-001", manana(), LocalTime.of(10, 0), LocalTime.of(12, 0), EstadoReserva.PENDIENTE);
        abrirFormulario("EQ-003");
        completarReserva(manana(), "10:00", "12:00");
        assertThat(texto(MENSAJE_ERROR)).contains("Solo se permite un (1) equipo");
        assertThat(reservaRepository.count()).isEqualTo(1);
    }
}
