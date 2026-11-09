package pe.edu.upn.labtrack.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upn.labtrack.model.Usuario;
import pe.edu.upn.labtrack.repository.UsuarioRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    public static final int MAX_INTENTOS = 3;
    public static final int MINUTOS_BLOQUEO = 15;

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    /**
     * HU-01. REF-01: la búsqueda usa una consulta parametrizada de JPA (sin concatenar SQL).
     * REF-04: un ingreso correcto reinicia el contador, así solo se bloquea con 3 fallos CONSECUTIVOS.
     */
    @Transactional
    public LoginResultado login(String codigo, String password) {
        if (esVacio(codigo) || esVacio(password)) {
            return LoginResultado.fallido();
        }
        Optional<Usuario> encontrado = usuarioRepository.findByCodigo(codigo.trim());
        if (encontrado.isEmpty()) {
            return LoginResultado.fallido();
        }
        Usuario usuario = encontrado.get();
        LocalDateTime ahora = LocalDateTime.now(clock);
        if (usuario.estaBloqueado(ahora)) {
            return LoginResultado.bloqueado();
        }
        if (passwordEncoder.matches(password, usuario.getPassword())) {
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
            usuarioRepository.save(usuario);
            log.info("Inicio de sesión correcto: {}", usuario.getCodigo());
            return LoginResultado.exitoso(usuario);
        }
        return registrarIntentoFallido(usuario, ahora);
    }

    private LoginResultado registrarIntentoFallido(Usuario usuario, LocalDateTime ahora) {
        int intentos = usuario.getIntentosFallidos() + 1;
        if (intentos >= MAX_INTENTOS) {
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(ahora.plusMinutes(MINUTOS_BLOQUEO));
            usuarioRepository.save(usuario);
            log.warn("Cuenta {} bloqueada temporalmente por {} intentos fallidos", usuario.getCodigo(), MAX_INTENTOS);
            return LoginResultado.bloqueado();
        }
        usuario.setIntentosFallidos(intentos);
        usuarioRepository.save(usuario);
        return LoginResultado.fallido();
    }

    private static boolean esVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
