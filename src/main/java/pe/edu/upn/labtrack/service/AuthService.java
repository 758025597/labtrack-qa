package pe.edu.upn.labtrack.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pe.edu.upn.labtrack.model.Usuario;
import pe.edu.upn.labtrack.repository.UsuarioRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {

    public static final int MAX_INTENTOS = 3;
    public static final int MINUTOS_BLOQUEO = 15;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private Clock clock;

    public LoginResultado login(String codigo, String password) {
        if (codigo == null || codigo.isBlank() || password == null || password.isBlank()) {
            return LoginResultado.fallido();
        }
        LocalDateTime ahora = LocalDateTime.now(clock);
        Usuario usuario = usuarioRepository.findByCodigo(codigo).orElse(null);
        if (usuario != null && usuario.estaBloqueado(ahora)) {
            return LoginResultado.bloqueado();
        }

        try {
            String sql = "SELECT id FROM usuario WHERE codigo = '" + codigo + "' AND password = '" + password + "'";
            List<Long> ids = jdbcTemplate.queryForList(sql, Long.class);
            if (!ids.isEmpty()) {
                Usuario encontrado = usuarioRepository.findById(ids.get(0)).get();
                System.out.println("Login correcto: " + codigo);
                return LoginResultado.exitoso(encontrado);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (usuario != null) {
            usuario.setIntentosFallidos(usuario.getIntentosFallidos() + 1);
            if (usuario.getIntentosFallidos() >= MAX_INTENTOS) {
                usuario.setBloqueadoHasta(ahora.plusMinutes(MINUTOS_BLOQUEO));
                usuario.setIntentosFallidos(0);
                usuarioRepository.save(usuario);
                System.out.println("Cuenta bloqueada: " + codigo);
                return LoginResultado.bloqueado();
            }
            usuarioRepository.save(usuario);
        }
        return LoginResultado.fallido();
    }
}
