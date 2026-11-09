package pe.edu.upn.labtrack.service;

import pe.edu.upn.labtrack.model.Reserva;

public class ResultadoDevolucion {

    private final Reserva reserva;
    private final boolean penalizado;

    public ResultadoDevolucion(Reserva reserva, boolean penalizado) {
        this.reserva = reserva;
        this.penalizado = penalizado;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public boolean isPenalizado() {
        return penalizado;
    }
}
