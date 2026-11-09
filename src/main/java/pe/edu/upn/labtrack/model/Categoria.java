package pe.edu.upn.labtrack.model;

public enum Categoria {
    COMPUTO("Cómputo"),
    ELECTRONICA("Electrónica"),
    REDES("Redes");

    private final String etiqueta;

    Categoria(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** Convierte el valor recibido del filtro; devuelve null si está vacío o no es válido. */
    public static Categoria desde(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        for (Categoria c : values()) {
            if (c.name().equalsIgnoreCase(valor.trim())) {
                return c;
            }
        }
        return null;
    }
}
