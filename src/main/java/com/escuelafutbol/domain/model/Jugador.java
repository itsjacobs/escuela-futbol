package com.escuelafutbol.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "jugadores")
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tutor_id")
    private Tutor tutor;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    private LocalDate fechaNacimiento;

    private String categoria;

    private LocalDate fechaInscripcion;

    private BigDecimal cuotaTemporada;

    private String temporadaActual;

    @OneToMany(mappedBy = "jugador", cascade = CascadeType.ALL)
    private List<Equipacion> equipaciones;

    @OneToMany(mappedBy = "jugador", cascade = CascadeType.ALL)
    private List<Pago> pagos;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Tutor getTutor() { return tutor; }
    public void setTutor(Tutor tutor) { this.tutor = tutor; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public LocalDate getFechaInscripcion() { return fechaInscripcion; }
    public void setFechaInscripcion(LocalDate fechaInscripcion) { this.fechaInscripcion = fechaInscripcion; }
    public BigDecimal getCuotaTemporada() { return cuotaTemporada; }
    public void setCuotaTemporada(BigDecimal cuotaTemporada) { this.cuotaTemporada = cuotaTemporada; }
    public String getTemporadaActual() { return temporadaActual; }
    public void setTemporadaActual(String temporadaActual) { this.temporadaActual = temporadaActual; }
    public List<Equipacion> getEquipaciones() { return equipaciones; }
    public void setEquipaciones(List<Equipacion> equipaciones) { this.equipaciones = equipaciones; }
    public List<Pago> getPagos() { return pagos; }
    public void setPagos(List<Pago> pagos) { this.pagos = pagos; }
}