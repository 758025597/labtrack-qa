package pe.edu.upn.labtrack.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.edu.upn.labtrack.config.SeguridadConfig;
import pe.edu.upn.labtrack.model.Rol;
import pe.edu.upn.labtrack.model.Usuario;
import pe.edu.upn.labtrack.repository.UsuarioRepository;
import pe.edu.upn.labtrack.service.AuthService;
import pe.edu.upn.labtrack.service.LoginResultado.Tipo;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static pe.edu.upn.labtrack.unit.RelojFijoConfig.AHORA;
import static pe.edu.upn.labtrack.unit.RelojFijoConfig.HOY;

@DataJpaTest
@Import({RelojFijoConfig.class, SeguridadConfig.class, AuthService.class})
@DisplayName("HU-01 Autenticación segura")
class AuthServiceTest {

    private static final String CODIGO = "N00123456";
    private static final String CLAVE = "Lab2026*";

    @Autowired
    private AuthService authService;
    @Autowired
    private UsuarioRepository usuarios;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void datos() {
        usuarios.save(new Usuario(CODIGO, "Ana Torres", passwordEncoder.encode(CLAVE), Rol.ESTUDIANTE));
    }

    private Usuario ana() {
        return usuarios.findByCodigo(CODIGO).orElseThrow();
    }

    @Test
    @DisplayName("CP-01 Credenciales válidas")
    void cp01Exitoso() {
        assertThat(authService.login(CODIGO, CLAVE).getTipo()).isEqualTo(Tipo.EXITOSO);
    }

    @Test
    @DisplayName("CP-02 / CP-03 / CP-04 Credenciales incorrectas, inexistentes o vacías")
    void cp02a04Fallidos() {
        assertThat(authService.login(CODIGO, "Clave999").getTipo()).isEqualTo(Tipo.FALLIDO);
        assertThat(authService.login("N99999999", CLAVE).getTipo()).isEqualTo(Tipo.FALLIDO);
        assertThat(authService.login("", "").getTipo()).isEqualTo(Tipo.FALLIDO);
        assertThat(authService.login(null, null).getTipo()).isEqualTo(Tipo.FALLIDO);
    }

    @Test
    @DisplayName("CP-05 Tres intentos fallidos consecutivos bloquean la cuenta")
    void cp05Bloqueo() {
        authService.login(CODIGO, "x1");
        authService.login(CODIGO, "x2");
        assertThat(authService.login(CODIGO, "x3").getTipo()).isEqualTo(Tipo.BLOQUEADO);
        assertThat(ana().getBloqueadoHasta()).isEqualTo(LocalDateTime.of(HOY, AHORA).plusMinutes(AuthService.MINUTOS_BLOQUEO));
    }

    @Test
    @DisplayName("CP-06 Un ingreso correcto reinicia el contador (REF-04)")
    void cp06ReinicioDeContador() {
        authService.login(CODIGO, "x1");
        authService.login(CODIGO, "x2");
        assertThat(authService.login(CODIGO, CLAVE).getTipo()).isEqualTo(Tipo.EXITOSO);
        assertThat(ana().getIntentosFallidos()).isZero();
        assertThat(authService.login(CODIGO, "x3").getTipo()).isEqualTo(Tipo.FALLIDO);
    }

    @Test
    @DisplayName("CP-07 Cuenta bloqueada no ingresa aun con la clave correcta; al vencer el bloqueo sí")
    void cp07BloqueoTemporal() {
        Usuario u = ana();
        u.setBloqueadoHasta(LocalDateTime.of(HOY, AHORA).plusMinutes(5));
        usuarios.save(u);
        assertThat(authService.login(CODIGO, CLAVE).getTipo()).isEqualTo(Tipo.BLOQUEADO);

        u.setBloqueadoHasta(LocalDateTime.of(HOY, AHORA).minusMinutes(1));
        usuarios.save(u);
        assertThat(authService.login(CODIGO, CLAVE).getTipo()).isEqualTo(Tipo.EXITOSO);
    }

    @Test
    @DisplayName("CP-08 Inyección SQL no permite ingresar (REF-01)")
    void cp08InyeccionSql() {
        assertThat(authService.login("' OR '1'='1", "' OR '1'='1").getTipo()).isEqualTo(Tipo.FALLIDO);
        assertThat(authService.login(CODIGO + "' --", "cualquiera").getTipo()).isEqualTo(Tipo.FALLIDO);
    }

    @Test
    @DisplayName("CP-09 La contraseña distingue mayúsculas y minúsculas")
    void cp09MayusculasMinusculas() {
        assertThat(authService.login(CODIGO, "lab2026*").getTipo()).isEqualTo(Tipo.FALLIDO);
    }

    @Test
    @DisplayName("REF-02 La contraseña se almacena con hash BCrypt")
    void ref02ClaveConHash() {
        assertThat(ana().getPassword()).isNotEqualTo(CLAVE).startsWith("$2");
    }
}
