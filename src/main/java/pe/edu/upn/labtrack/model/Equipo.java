package pe.edu.upn.labtrack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "equipo")
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código de inventario, por ejemplo EQ-001. */
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Categoria categoria;

    protected Equipo() {
        // requerido por JPA
    }

    public Equipo(String codigo, String nombre, Categoria categoria) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.categoria = categoria;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public Categoria getCategoria() {
        return categoria;
    }
}
