package pe.edu.upn.labtrack.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

/** Expone el nombre y rol del usuario conectado a todas las vistas (barra de navegación). */
@ControllerAdvice
public class SesionAdvice {

    @ModelAttribute
    public void datosDeSesion(HttpSession session, Model model) {
        model.addAttribute("nombreSesion", session.getAttribute("nombre"));
        model.addAttribute("rolSesion", session.getAttribute("rol"));
    }
}
