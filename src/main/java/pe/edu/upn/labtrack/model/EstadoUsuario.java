package pe.edu.upn.labtrack.model;

public enum EstadoUsuario {
    ACTIVO("Activo"),
    PENALIZADO("Penalizado");

    private final String etiqueta;

    EstadoUsuario(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
