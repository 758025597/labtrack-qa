package pe.edu.upn.labtrack.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.upn.labtrack.model.Categoria;
import pe.edu.upn.labtrack.model.Equipo;
import pe.edu.upn.labtrack.model.EstadoEquipo;
import pe.edu.upn.labtrack.repository.UsuarioRepository;
import pe.edu.upn.labtrack.service.CatalogoService;
import pe.edu.upn.labtrack.service.EquipoVista;
import pe.edu.upn.labtrack.service.ReglaNegocioException;
import pe.edu.upn.labtrack.service.ReservaService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
public class EstudianteController {

    @Autowired
    private CatalogoService catalogoService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/panel")
    public String panel(HttpSession session, Model model) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        model.addAttribute("usuario", usuarioRepository.findById(usuarioId).get());
        model.addAttribute("reservas", reservaService.delUsuario(usuarioId));
        return "panel";
    }

    @GetMapping("/catalogo")
    public String catalogo(@RequestParam(required = false) String categoria, Model model) {
        Categoria filtro = Categoria.desde(categoria);
        List<EquipoVista> equipos = catalogoService.listar(filtro);
        model.addAttribute("equipos", equipos);
        model.addAttribute("categorias", Categoria.values());
        model.addAttribute("categoriaSeleccionada", filtro == null ? "" : filtro.name());
        if (equipos.isEmpty() && filtro != null) {
            model.addAttribute("mensajeVacio", "No hay equipos disponibles en esta categoría");
        }
        return "catalogo";
    }

    @GetMapping("/reservas/nueva")
    public String nuevaReserva(@RequestParam String equipo, Model model) {
        try {
            Equipo e = reservaService.buscarEquipo(equipo);
            model.addAttribute("equipo", e);
            if (catalogoService.estadoActual(e) == EstadoEquipo.PRESTADO) {
                model.addAttribute("mensajeError", "El equipo no está disponible para reserva.");
                model.addAttribute("bloqueado", true);
            }
        } catch (ReglaNegocioException ex) {
            model.addAttribute("mensajeError", ex.getMessage());
            model.addAttribute("bloqueado", true);
        }
        return "reservar";
    }

    @PostMapping("/reservas")
    public String reservar(@RequestParam String equipo,
                           @RequestParam(required = false) String fecha,
                           @RequestParam(required = false) String horaInicio,
                           @RequestParam(required = false) String horaFin,
                           HttpSession session, Model model, RedirectAttributes flash) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        try {
            reservaService.crear(usuarioId, equipo, aFecha(fecha), aHora(horaInicio), aHora(horaFin));
            flash.addFlashAttribute("mensajeExito", "Reserva registrada correctamente. Estado: Pendiente de aprobación.");
            return "redirect:/panel";
        } catch (ReglaNegocioException ex) {
            System.out.println("Reserva rechazada: " + ex.getMessage());
            model.addAttribute("mensajeError", ex.getMessage());
            model.addAttribute("equipo", reservaService.buscarEquipo(equipo));
            model.addAttribute("fecha", fecha);
            model.addAttribute("horaInicio", horaInicio);
            model.addAttribute("horaFin", horaFin);
            return "reservar";
        }
    }

    private LocalDate aFecha(String valor) {
        try {
            return valor == null || valor.isBlank() ? null : LocalDate.parse(valor);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalTime aHora(String valor) {
        try {
            return valor == null || valor.isBlank() ? null : LocalTime.parse(valor);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
