package pe.edu.upn.labtrack.service;

import pe.edu.upn.labtrack.model.Equipo;
import pe.edu.upn.labtrack.model.EstadoEquipo;

/** Equipo con su estado calculado en el momento de la consulta. */
public class EquipoVista {

    private final Equipo equipo;
    private final EstadoEquipo estado;

    public EquipoVista(Equipo equipo, EstadoEquipo estado) {
        this.equipo = equipo;
        this.estado = estado;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public EstadoEquipo getEstado() {
        return estado;
    }

    public boolean isDisponible() {
        return estado == EstadoEquipo.DISPONIBLE;
    }
}
