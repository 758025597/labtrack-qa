package pe.edu.upn.labtrack.model;

public enum EstadoEquipo {
    DISPONIBLE("Disponible"),
    PRESTADO("Prestado");

    private final String etiqueta;

    EstadoEquipo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
