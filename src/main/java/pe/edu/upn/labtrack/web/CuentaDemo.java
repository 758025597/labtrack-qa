package pe.edu.upn.labtrack.web;

/** Cuenta de demostración que se muestra en la pantalla de inicio de sesión para facilitar las pruebas. */
public class CuentaDemo {

    private final String nombre;
    private final String codigo;
    private final String clave;
    private final String rol;
    private final boolean encargado;

    public CuentaDemo(String nombre, String codigo, String clave, String rol, boolean encargado) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.clave = clave;
        this.rol = rol;
        this.encargado = encargado;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getClave() {
        return clave;
    }

    public String getRol() {
        return rol;
    }

    public boolean isEncargado() {
        return encargado;
    }
}
