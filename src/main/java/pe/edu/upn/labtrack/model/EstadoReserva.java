package pe.edu.upn.labtrack.model;

public enum EstadoReserva {
    PENDIENTE("Pendiente"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada"),
    DEVUELTA("Devuelta");

    private final String etiqueta;

    EstadoReserva(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
