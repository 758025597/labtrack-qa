package pe.edu.upn.labtrack.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.upn.labtrack.model.Reserva;
import pe.edu.upn.labtrack.service.DevolucionService;
import pe.edu.upn.labtrack.service.ReglaNegocioException;
import pe.edu.upn.labtrack.service.ResultadoDevolucion;
import pe.edu.upn.labtrack.service.SolicitudService;

@Controller
public class AdminController {

    @Autowired
    private SolicitudService solicitudService;

    @Autowired
    private DevolucionService devolucionService;

    @GetMapping("/admin/solicitudes")
    public String solicitudes(Model model) {
        model.addAttribute("solicitudes", solicitudService.pendientes());
        return "admin/solicitudes";
    }

    @PostMapping("/admin/solicitudes/{id}/aprobar")
    public String aprobar(@PathVariable Long id, RedirectAttributes flash) {
        try {
            Reserva r = solicitudService.aprobar(id);
            flash.addFlashAttribute("mensajeExito", "Solicitud aprobada: " + r.getEquipo().getCodigo()
                    + " para " + r.getUsuario().getNombre() + ".");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/admin/solicitudes";
    }

    @PostMapping("/admin/solicitudes/{id}/rechazar")
    public String rechazar(@PathVariable Long id, @RequestParam(required = false) String motivo,
                           RedirectAttributes flash) {
        try {
            solicitudService.rechazar(id, motivo);
            flash.addFlashAttribute("mensajeExito", "Solicitud rechazada.");
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/admin/solicitudes";
    }

    @GetMapping("/admin/devoluciones")
    public String devoluciones(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("reservas", devolucionService.activas(q));
        model.addAttribute("q", q);
        return "admin/devoluciones";
    }

    @PostMapping("/admin/devoluciones/{id}")
    public String devolver(@PathVariable Long id, RedirectAttributes flash) {
        try {
            ResultadoDevolucion resultado = devolucionService.marcarDevolucion(id);
            Reserva r = resultado.getReserva();
            String mensaje = "Devolución registrada. El equipo " + r.getEquipo().getCodigo() + " está Disponible.";
            if (resultado.isPenalizado()) {
                mensaje += " El estudiante " + r.getUsuario().getCodigo() + " fue Penalizado por devolución tardía.";
            }
            flash.addFlashAttribute("mensajeExito", mensaje);
        } catch (ReglaNegocioException ex) {
            flash.addFlashAttribute("mensajeError", ex.getMessage());
        }
        return "redirect:/admin/devoluciones";
    }
}
