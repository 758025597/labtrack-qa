package pe.edu.upn.labtrack.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import pe.edu.upn.labtrack.model.EstadoReserva;
import pe.edu.upn.labtrack.model.Reserva;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HU-05 Devolución de equipos (Selenium)")
class DevolucionE2ETest extends BaseE2ETest {

    private void marcarDevolucionDe(Reserva r) {
        login(ENCARGADO, CLAVE_ENCARGADO);
        abrir("/admin/devoluciones");
        escribir(By.id("buscar"), ANA);
        driver.findElement(By.id("btn-buscar")).click();
        driver.findElement(By.id("devolver-" + r.getId())).click();
    }

    @Test
    @DisplayName("CP-36 Buscar la reserva activa y marcar la devolución")
    void cp36MarcarDevolucion() {
        Reserva r = crearPrestamoActivo(ANA, "EQ-001");
        marcarDevolucionDe(r);

        assertThat(texto(By.id("mensaje-exito"))).contains("Devolución registrada");
        assertThat(recargar(r).getEstado()).isEqualTo(EstadoReserva.DEVUELTA);
    }

    @Test
    @DisplayName("CP-37 El equipo vuelve inmediatamente a 'Disponible'")
    void cp37EquipoDisponible() {
        Reserva r = crearPrestamoActivo(ANA, "EQ-001");
        login(ENCARGADO, CLAVE_ENCARGADO);
        abrir("/catalogo");
        assertThat(estadoEnCatalogo("EQ-001")).isEqualTo("Prestado");

        marcarDevolucionDe(r);
        abrir("/catalogo");
        assertThat(estadoEnCatalogo("EQ-001")).isEqualTo("Disponible");
    }
}
