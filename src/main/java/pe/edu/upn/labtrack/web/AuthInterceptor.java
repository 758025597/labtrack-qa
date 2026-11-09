package pe.edu.upn.labtrack.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import pe.edu.upn.labtrack.model.Rol;

import java.io.IOException;

/**
 * Exige sesión iniciada para las páginas privadas.
 * REF-05: además, las rutas /admin/** solo están permitidas para el rol ENCARGADO (CP-31).
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATRIBUTO_USUARIO = "usuarioId";
    public static final String ATRIBUTO_ROL = "rol";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(ATRIBUTO_USUARIO) == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        boolean rutaAdmin = request.getRequestURI().startsWith(request.getContextPath() + "/admin");
        if (rutaAdmin && !Rol.ENCARGADO.name().equals(session.getAttribute(ATRIBUTO_ROL))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }
}
