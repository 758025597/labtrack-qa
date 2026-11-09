package pe.edu.upn.labtrack.service;

/** Error de validación o de regla de negocio que se muestra al usuario. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
