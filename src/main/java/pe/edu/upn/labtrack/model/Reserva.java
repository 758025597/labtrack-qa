package pe.edu.upn.labtrack.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado = EstadoReserva.PENDIENTE;

    @Column(length = 255)
    private String motivoRechazo;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaDevolucion;

    protected Reserva() {
        // requerido por JPA
    }

    public Reserva(Usuario usuario, Equipo equipo, LocalDate fecha, LocalTime horaInicio,
                   LocalTime horaFin, LocalDateTime fechaSolicitud) {
        this.usuario = usuario;
        this.equipo = equipo;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime inicio() {
        return LocalDateTime.of(fecha, horaInicio);
    }

    public LocalDateTime fin() {
        return LocalDateTime.of(fecha, horaFin);
    }

    /** Dos bloques se cruzan si comparten algún minuto. Bloques contiguos (12:00-14:00 y 10:00-12:00) NO se cruzan. */
    public boolean seCruzaCon(LocalDate otraFecha, LocalTime otroInicio, LocalTime otroFin) {
        return fecha.equals(otraFecha) && horaInicio.isBefore(otroFin) && horaFin.isAfter(otroInicio);
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public LocalDateTime getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDateTime fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }
}
