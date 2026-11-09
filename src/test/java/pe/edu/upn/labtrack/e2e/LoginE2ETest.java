package pe.edu.upn.labtrack.e2e;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HU-01 Autenticación segura (Selenium)")
class LoginE2ETest extends BaseE2ETest {

    private static final By MENSAJE_ERROR = By.id("mensaje-error");
    private static final By ALERTA_SEGURIDAD = By.id("alerta-seguridad");

    @Test
    @DisplayName("CP-01 Inicio de sesión con credenciales válidas")
    void cp01LoginExitoso() {
        login(ANA, CLAVE_ESTUDIANTE);
        assertThat(texto(By.id("titulo-panel"))).isEqualTo("Mi panel de préstamos");
        assertThat(driver.getCurrentUrl()).endsWith("/panel");
    }

    @Test
    @DisplayName("CP-02 Contraseña incorrecta muestra 'Credenciales inválidas'")
    void cp02ClaveIncorrecta() {
        login(ANA, "Clave999");
        assertThat(texto(MENSAJE_ERROR)).isEqualTo("Credenciales inválidas");
        assertThat(driver.getCurrentUrl()).endsWith("/login");
    }

    @Test
    @DisplayName("CP-03 Código de estudiante no registrado")
    void cp03CodigoInexistente() {
        login("N99999999", CLAVE_ESTUDIANTE);
        assertThat(texto(MENSAJE_ERROR)).isEqualTo("Credenciales inválidas");
    }

    @Test
    @DisplayName("CP-04 Campos de login vacíos")
    void cp04CamposVacios() {
        login("", "");
        assertThat(texto(MENSAJE_ERROR)).isEqualTo("Credenciales inválidas");
        assertThat(existe(By.id("titulo-panel"))).isFalse();
    }

    @Test
    @DisplayName("CP-05 Bloqueo tras 3 intentos fallidos consecutivos")
    void cp05BloqueoTresIntentos() {
        login(ANA, "Mala1");
        assertThat(existe(ALERTA_SEGURIDAD)).isFalse();
        login(ANA, "Mala2");
        assertThat(existe(ALERTA_SEGURIDAD)).isFalse();
        login(ANA, "Mala3");
        assertThat(texto(ALERTA_SEGURIDAD)).contains("bloqueada temporalmente");
    }

    @Test
    @DisplayName("CP-06 Dos fallos y un ingreso correcto reinician el contador")
    void cp06ReinicioDeContador() {
        login(ANA, "Mala1");
        login(ANA, "Mala2");
        login(ANA, CLAVE_ESTUDIANTE);
        assertThat(texto(By.id("titulo-panel"))).isEqualTo("Mi panel de préstamos");

        driver.findElement(By.id("link-salir")).click();
        login(ANA, "Mala3");
        assertThat(texto(MENSAJE_ERROR)).isEqualTo("Credenciales inválidas");
        assertThat(existe(ALERTA_SEGURIDAD)).as("no debe bloquear: los fallos no fueron consecutivos").isFalse();
    }

    @Test
    @DisplayName("CP-08 Intento de inyección SQL en el login")
    void cp08InyeccionSql() {
        login("' OR '1'='1", "' OR '1'='1");
        assertThat(texto(MENSAJE_ERROR)).isEqualTo("Credenciales inválidas");
        assertThat(existe(By.id("titulo-panel"))).isFalse();
    }
}
