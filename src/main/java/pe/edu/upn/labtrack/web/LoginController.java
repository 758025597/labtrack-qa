package pe.edu.upn.labtrack.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.edu.upn.labtrack.model.Usuario;
import pe.edu.upn.labtrack.service.AuthService;
import pe.edu.upn.labtrack.service.LoginResultado;

import java.util.List;

@Controller
public class LoginController {

    private static final String VISTA_LOGIN = "login";
    private static final String REDIRECT_LOGIN = "redirect:/login";
    private static final String ATRIBUTO_CUENTAS_DEMO = "cuentasDemo";

    private final AuthService authService;
    private final List<CuentaDemo> cuentasDemo;

    /**
     * Las cuentas de demostración se arman con las mismas claves configuradas para la carga inicial
     * (REF-03: vienen de la configuración / variables de entorno, no del código) y se muestran en el
     * login solo si {@code labtrack.demo.mostrar-cuentas} está activo.
     */
    public LoginController(AuthService authService,
                           @Value("${labtrack.demo.mostrar-cuentas:true}") boolean mostrarCuentasDemo,
                           @Value("${labtrack.semilla.clave-estudiante}") String claveEstudiante,
                           @Value("${labtrack.semilla.clave-encargado}") String claveEncargado) {
        this.authService = authService;
        this.cuentasDemo = mostrarCuentasDemo
                ? List.of(
                        new CuentaDemo("Ana Torres", "N00123456", claveEstudiante, "Estudiante", false),
                        new CuentaDemo("Luis Rojas", "N00654321", claveEstudiante, "Estudiante", false),
                        new CuentaDemo("María Quispe", "N00111222", claveEstudiante, "Estudiante", false),
                        new CuentaDemo("Encargado de Laboratorio", "ADMIN01", claveEncargado, "Encargado", true))
                : List.of();
    }

    @GetMapping("/")
    public String inicio() {
        return REDIRECT_LOGIN;
    }

    @GetMapping("/login")
    public String formulario(Model model) {
        agregarCuentasDemo(model);
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
        agregarCuentasDemo(model);
        return VISTA_LOGIN;
    }

    @GetMapping("/logout")
    public String salir(HttpSession session) {
        session.invalidate();
        return REDIRECT_LOGIN;
    }

    private void agregarCuentasDemo(Model model) {
        if (!cuentasDemo.isEmpty()) {
            model.addAttribute(ATRIBUTO_CUENTAS_DEMO, cuentasDemo);
        }
    }
}
