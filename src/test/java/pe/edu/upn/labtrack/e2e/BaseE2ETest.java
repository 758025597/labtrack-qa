package pe.edu.upn.labtrack.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import pe.edu.upn.labtrack.config.CargaDatosIniciales;
import pe.edu.upn.labtrack.model.Equipo;
import pe.edu.upn.labtrack.model.EstadoReserva;
import pe.edu.upn.labtrack.model.EstadoUsuario;
import pe.edu.upn.labtrack.model.Reserva;
import pe.edu.upn.labtrack.model.Usuario;
import pe.edu.upn.labtrack.repository.EquipoRepository;
import pe.edu.upn.labtrack.repository.ReservaRepository;
import pe.edu.upn.labtrack.repository.UsuarioRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Base de las pruebas Selenium: levanta LabTrack en un puerto aleatorio,
 * reinicia los datos antes de cada caso y abre Chrome (headless por defecto).
 * Para ver el navegador: mvn verify -Pe2e -De2e.visible=true
 */
@Tag("e2e")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseE2ETest {

    protected static final String ANA = "N00123456";
    protected static final String LUIS = "N00654321";
    protected static final String MARIA = "N00111222";
    protected static final String CLAVE_ESTUDIANTE = "Lab2026*";
    protected static final String ENCARGADO = "ADMIN01";
    protected static final String CLAVE_ENCARGADO = "Admin2026*";

    @LocalServerPort
    protected int port;

    @Autowired
    protected ReservaRepository reservaRepository;
    @Autowired
    protected UsuarioRepository usuarioRepository;
    @Autowired
    protected EquipoRepository equipoRepository;
    @Autowired
    protected CargaDatosIniciales cargaDatos;
    @Autowired
    protected Clock clock;

    protected WebDriver driver;
    protected WebDriverWait espera;

    @BeforeEach
    void prepararEscenario() {
        reservaRepository.deleteAll();
        List<Usuario> usuarios = usuarioRepository.findAll();
        for (Usuario u : usuarios) {
            u.setIntentosFallidos(0);
            u.setBloqueadoHasta(null);
            u.setEstado(EstadoUsuario.ACTIVO);
        }
        usuarioRepository.saveAll(usuarios);
        cargaDatos.cargarEquipos();

        ChromeOptions opciones = new ChromeOptions();
        if (!Boolean.getBoolean("e2e.visible")) {
            opciones.addArguments("--headless=new");
        }
        opciones.addArguments("--window-size=1366,900", "--no-sandbox", "--disable-dev-shm-usage", "--lang=es-PE");
        driver = new ChromeDriver(opciones);
        espera = new WebDriverWait(driver, Duration.ofSeconds(5));
    }

    @AfterEach
    void cerrarNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ---------- acciones ----------

    protected void abrir(String ruta) {
        driver.get("http://localhost:" + port + ruta);
    }

    protected void login(String codigo, String clave) {
        abrir("/login");
        escribir(By.id("codigo"), codigo);
        escribir(By.id("password"), clave);
        driver.findElement(By.id("btn-ingresar")).click();
    }

    protected void escribir(By localizador, String texto) {
        WebElement campo = driver.findElement(localizador);
        campo.clear();
        if (texto != null) {
            campo.sendKeys(texto);
        }
    }

    /** Asigna el valor de un input date/time por JavaScript (evita depender del formato regional del navegador). */
    protected void fijarValor(By localizador, String valor) {
        WebElement campo = driver.findElement(localizador);
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", campo, valor);
    }

    protected void completarReserva(LocalDate fecha, String inicio, String fin) {
        if (fecha != null) {
            fijarValor(By.id("fecha"), fecha.toString());
        }
        if (inicio != null) {
            fijarValor(By.id("hora-inicio"), inicio);
        }
        if (fin != null) {
            fijarValor(By.id("hora-fin"), fin);
        }
        driver.findElement(By.id("btn-reservar")).click();
    }

    protected void filtrarCategoria(String valor) {
        new Select(driver.findElement(By.id("filtro-categoria"))).selectByValue(valor);
        driver.findElement(By.id("btn-filtrar")).click();
    }

    protected String texto(By localizador) {
        return espera.until(ExpectedConditions.visibilityOfElementLocated(localizador)).getText().trim();
    }

    protected boolean existe(By localizador) {
        return !driver.findElements(localizador).isEmpty();
    }

    protected String estadoEnCatalogo(String codigoEquipo) {
        return driver.findElement(By.cssSelector("tr[data-codigo='" + codigoEquipo + "'] .col-estado")).getText().trim();
    }

    // ---------- datos ----------

    protected LocalDate hoy() {
        return LocalDate.now(clock);
    }

    protected LocalDate manana() {
        return hoy().plusDays(1);
    }

    protected Reserva crearReserva(String codigoUsuario, String codigoEquipo, LocalDate fecha,
                                   LocalTime inicio, LocalTime fin, EstadoReserva estado) {
        Usuario u = usuarioRepository.findByCodigo(codigoUsuario).orElseThrow();
        Equipo e = equipoRepository.findByCodigo(codigoEquipo).orElseThrow();
        Reserva r = new Reserva(u, e, fecha, inicio, fin, LocalDateTime.now(clock));
        r.setEstado(estado);
        return reservaRepository.save(r);
    }

    /** Reserva aprobada que cubre todo el día de hoy: el equipo queda "Prestado" en este momento. */
    protected Reserva crearPrestamoActivo(String codigoUsuario, String codigoEquipo) {
        return crearReserva(codigoUsuario, codigoEquipo, hoy(), LocalTime.MIN, LocalTime.of(23, 59), EstadoReserva.APROBADA);
    }

    protected Reserva recargar(Reserva r) {
        return reservaRepository.findById(r.getId()).orElseThrow();
    }
}
