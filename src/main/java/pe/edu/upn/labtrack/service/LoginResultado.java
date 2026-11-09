package pe.edu.upn.labtrack.service;

import pe.edu.upn.labtrack.model.Usuario;

public final class LoginResultado {

    public enum Tipo { EXITOSO, FALLIDO, BLOQUEADO }

    private final Tipo tipo;
    private final Usuario usuario;

    private LoginResultado(Tipo tipo, Usuario usuario) {
        this.tipo = tipo;
        this.usuario = usuario;
    }

    public static LoginResultado exitoso(Usuario usuario) {
        return new LoginResultado(Tipo.EXITOSO, usuario);
    }

    public static LoginResultado fallido() {
        return new LoginResultado(Tipo.FALLIDO, null);
    }

    public static LoginResultado bloqueado() {
        return new LoginResultado(Tipo.BLOQUEADO, null);
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
