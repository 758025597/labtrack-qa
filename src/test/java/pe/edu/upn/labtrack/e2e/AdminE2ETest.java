package pe.edu.upn.labtrack.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import pe.edu.upn.labtrack.model.EstadoReserva;
import pe.edu.upn.labtrack.model.Reserva;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HU-04 Gestión de solicitudes - panel del encargado (Selenium)")
class AdminE2ETest extends BaseE2ETest {

    private static final LocalTime DIEZ = LocalTime.of(10, 0);
    private static final LocalTime DOCE = LocalTime.of(12, 0);

    @Test
    @DisplayName("CP-30 El panel muestra la tabla de solicitudes con sus acciones")
    void cp30PanelDeSolicitudes() {
        crearReserva(ANA, "EQ-001", manana(), DIEZ, DOCE, EstadoReserva.PENDIENTE);
        crearReserva(LUIS, "EQ-003", manana(), DIEZ, DOCE, EstadoReserva.PENDIENTE);
        crearReserva(MARIA, "EQ-005", manana(), DIEZ, DOCE, EstadoReserva.PENDIENTE);

        login(ENCARGADO, CLAVE_ENCARGADO);

        List<String> cabeceras = driver.findElements(By.cssSelector("#tabla-solicitudes th")).stream()
                .map(WebElement::getText).toList();
        assertThat(cabeceras).containsExactly("Nombre del estudiante", "Equipo", "Fecha/Hora solicitada", "Acciones");
        List<WebElement> filas = driver.findElements(By.cssSelector("#tabla-solicitudes .fila-solicitud"));
        assertThat(filas).hasSize(3);
        for (WebElement fila : filas) {
            assertThat(fila.findElements(By.cssSelector(".btn-aprobar"))).hasSize(1);
            assertThat(fila.findElements(By.cssSelector(".btn-rechazar"))).hasSize(1);
        }
    }

    @Test
    @DisplayName("CP-31 Un estudiante no puede acceder al panel del encargado")
    void cp31ControlDeAcceso() {
        crearReserva(LUIS, "EQ-003", manana(), DIEZ, DOCE, EstadoReserva.PENDIENTE);
        login(ANA, CLAVE_ESTUDIANTE);
        abrir("/admin/solicitudes");
        assertThat(existe(By.id("tabla-solicitudes"))).isFalse();
        assertThat(texto(By.id("acceso-denegado"))).isEqualTo("Acceso denegado");
    }

    @Test
    @DisplayName("CP-32 Aprobar: la reserva pasa a Aprobada y el equipo a Prestado")
    void cp32AprobarSolicitud() {
        Reserva r = crearReserva(ANA, "EQ-001", hoy(), LocalTime.MIN, LocalTime.of(23, 59), EstadoReserva.PENDIENTE);
        login(ENCARGADO, CLAVE_ENCARGADO);
        driver.findElement(By.id("aprobar-" + r.getId())).click();

        assertThat(texto(By.id("mensaje-exito"))).contains("Solicitud aprobada");
        assertThat(recargar(r).getEstado()).isEqualTo(EstadoReserva.APROBADA);

        abrir("/catalogo");
        assertThat(estadoEnCatalogo("EQ-001")).isEqualTo("Prestado");
    }

    @Test
    @DisplayName("CP-33 Rechazar sin justificación no está permitido")
    void cp33RechazarSinMotivo() {
        Reserva r = crearReserva(ANA, "EQ-001", manana(), DIEZ, DOCE, EstadoReserva.PENDIENTE);
        login(ENCARGADO, CLAVE_ENCARGADO);
        driver.findElement(By.id("rechazar-" + r.getId())).click();

        assertThat(texto(By.id("mensaje-error"))).contains("Debe ingresar el motivo del rechazo");
        assertThat(recargar(r).getEstado()).isEqualTo(EstadoReserva.PENDIENTE);
    }

    @Test
    @DisplayName("CP-34 Rechazar con justificación")
    void cp34RechazarConMotivo() {
        Reserva r = crearReserva(ANA, "EQ-001", manana(), DIEZ, DOCE, EstadoReserva.PENDIENTE);
        login(ENCARGADO, CLAVE_ENCARGADO);
        escribir(By.id("motivo-" + r.getId()), "Equipo en mantenimiento");
        driver.findElement(By.id("rechazar-" + r.getId())).click();

        assertThat(texto(By.id("mensaje-exito"))).contains("Solicitud rechazada");
        Reserva guardada = recargar(r);
        assertThat(guardada.getEstado()).isEqualTo(EstadoReserva.RECHAZADA);
        assertThat(guardada.getMotivoRechazo()).isEqualTo("Equipo en mantenimiento");
    }
}
