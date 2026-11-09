package pe.edu.upn.labtrack.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.edu.upn.labtrack.model.Usuario;
import pe.edu.upn.labtrack.service.AuthService;
import pe.edu.upn.labtrack.service.LoginResultado;

@Controller
public class LoginController {

    private static final String VISTA_LOGIN = "login";
    private static final String REDIRECT_LOGIN = "redirect:/login";

    private final AuthService authService;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/")
    public String inicio() {
        return REDIRECT_LOGIN;
    }

    @GetMapping("/login")
    public String formulario() {
        return VISTA_LOGIN;
    }

    @PostMapping("/login")
    public String ingresar(@RequestParam(required = false) String codigo,
                           @RequestParam(required = false) String password,
                           HttpSession session, Model model) {
        LoginResultado resultado = authService.login(codigo, password);
        if (resultado.getTipo() == LoginResultado.Tipo.EXITOSO) {
            Usuario usuario = resultado.getUsuario();
            session.setAttribute(AuthInterceptor.ATRIBUTO_USUARIO, usuario.getId());
            session.setAttribute(AuthInterceptor.ATRIBUTO_ROL, usuario.getRol().name());
            session.setAttribute("nombre", usuario.getNombre());
            return usuario.esEncargado() ? "redirect:/admin/solicitudes" : "redirect:/panel";
        }
        model.addAttribute("codigo", codigo);
        if (resultado.getTipo() == LoginResultado.Tipo.BLOQUEADO) {
            model.addAttribute("alertaSeguridad", "Alerta de seguridad: su cuenta ha sido bloqueada temporalmente por "
                    + AuthService.MAX_INTENTOS + " intentos fallidos consecutivos. Intente nuevamente en "
                    + AuthService.MINUTOS_BLOQUEO + " minutos.");
        } else {
            model.addAttribute("mensajeError", "Credenciales inválidas");
        }
        return VISTA_LOGIN;
    }

    @GetMapping("/logout")
    public String salir(HttpSession session) {
        session.invalidate();
        return REDIRECT_LOGIN;
    }
}
