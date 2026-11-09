package pe.edu.upn.labtrack.unit;

import pe.edu.upn.labtrack.model.Equipo;
import pe.edu.upn.labtrack.model.EstadoReserva;
import pe.edu.upn.labtrack.model.Reserva;
import pe.edu.upn.labtrack.model.Usuario;
import pe.edu.upn.labtrack.repository.ReservaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

final class DatosPrueba {

    private DatosPrueba() {
    }

    static LocalTime h(int hora) {
        return LocalTime.of(hora, 0);
    }

    static LocalTime h(int hora, int minuto) {
        return LocalTime.of(hora, minuto);
    }

    static Reserva reserva(ReservaRepository repo, Usuario u, Equipo e, LocalDate fecha,
                           LocalTime inicio, LocalTime fin, EstadoReserva estado) {
        Reserva r = new Reserva(u, e, fecha, inicio, fin, LocalDateTime.of(RelojFijoConfig.HOY, LocalTime.of(8, 0)));
        r.setEstado(estado);
        return repo.save(r);
    }
}
