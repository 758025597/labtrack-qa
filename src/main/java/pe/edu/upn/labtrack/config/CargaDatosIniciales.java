package pe.edu.upn.labtrack.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pe.edu.upn.labtrack.model.Categoria;
import pe.edu.upn.labtrack.model.Equipo;
import pe.edu.upn.labtrack.model.EstadoReserva;
import pe.edu.upn.labtrack.model.Reserva;
import pe.edu.upn.labtrack.model.Rol;
import pe.edu.upn.labtrack.model.Usuario;
import pe.edu.upn.labtrack.repository.EquipoRepository;
import pe.edu.upn.labtrack.repository.ReservaRepository;
import pe.edu.upn.labtrack.repository.UsuarioRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Carga los datos de demostración y de prueba al iniciar la aplicación. */
@Component
public class CargaDatosIniciales implements CommandLineRunner {

    public static final int USUARIOS_CARGA = 150;

    private static final String ADMIN_PASSWORD = "Admin2026*";
    private static final String STUDENT_PASSWORD = "Lab2026*";
    private static final String LOAD_TEST_PASSWORD = "Carga2026*";

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private Clock clock;

    @Override
    public void run(String... args) {
        cargarUsuarios();
        cargarEquipos();
        cargarReservaDemo();
    }

    public void cargarUsuarios() {
        if (usuarioRepository.count() > 0) {
            return;
        }
        usuarioRepository.save(new Usuario("ADMIN01", "Encargado de Laboratorio", ADMIN_PASSWORD, Rol.ENCARGADO));
        usuarioRepository.save(new Usuario("N00123456", "Ana Torres", STUDENT_PASSWORD, Rol.ESTUDIANTE));
        usuarioRepository.save(new Usuario("N00654321", "Luis Rojas", STUDENT_PASSWORD, Rol.ESTUDIANTE));
        usuarioRepository.save(new Usuario("N00111222", "María Quispe", STUDENT_PASSWORD, Rol.ESTUDIANTE));
        // Usuarios para la prueba de carga con JMeter (N00000001 ... N00000150)
        for (int i = 1; i <= USUARIOS_CARGA; i++) {
            String codigo = String.format("N%08d", i);
            usuarioRepository.save(new Usuario(codigo, "Usuario Carga " + i, LOAD_TEST_PASSWORD, Rol.ESTUDIANTE));
        }
    }

    /** Inserta los equipos que falten (idempotente). */
    public void cargarEquipos() {
        guardarSiNoExiste("EQ-001", "Laptop Dell Latitude 5440", Categoria.COMPUTO);
        guardarSiNoExiste("EQ-002", "Laptop Lenovo ThinkPad E14", Categoria.COMPUTO);
        guardarSiNoExiste("EQ-003", "Raspberry Pi 4 Model B", Categoria.COMPUTO);
        guardarSiNoExiste("EQ-004", "Proyector Epson PowerLite", Categoria.COMPUTO);
        guardarSiNoExiste("EQ-005", "Osciloscopio Rigol DS1054Z", Categoria.ELECTRONICA);
        guardarSiNoExiste("EQ-006", "Multímetro Fluke 117", Categoria.ELECTRONICA);
        guardarSiNoExiste("EQ-007", "Kit Arduino Uno R3", Categoria.ELECTRONICA);
        guardarSiNoExiste("EQ-008", "Fuente de alimentación DC 30V", Categoria.ELECTRONICA);
        guardarSiNoExiste("EQ-009", "Router Cisco ISR 4321", Categoria.REDES);
        guardarSiNoExiste("EQ-010", "Switch Cisco Catalyst 2960", Categoria.REDES);
        guardarSiNoExiste("EQ-011", "Kit de ponchado RJ45", Categoria.REDES);
        guardarSiNoExiste("EQ-012", "Access Point Ubiquiti UniFi", Categoria.REDES);
    }

    private void guardarSiNoExiste(String codigo, String nombre, Categoria categoria) {
        if (equipoRepository.findByCodigo(codigo).isEmpty()) {
            equipoRepository.save(new Equipo(codigo, nombre, categoria));
        }
    }

    /** Deja un equipo prestado (EQ-002) para que la demo muestre ambos estados. */
    private void cargarReservaDemo() {
        if (reservaRepository.count() > 0) {
            return;
        }
        Usuario maria = usuarioRepository.findByCodigo("N00111222").orElseThrow();
        Equipo laptop = equipoRepository.findByCodigo("EQ-002").orElseThrow();
        LocalDate hoy = LocalDate.now(clock);
        Reserva reserva = new Reserva(maria, laptop, hoy, LocalTime.MIN, LocalTime.of(23, 59), LocalDateTime.now(clock));
        reserva.setEstado(EstadoReserva.APROBADA);
        reservaRepository.save(reserva);
    }
}
