package com.escuelafutbol.domain.model;

import com.escuelafutbol.commons.Constantes;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Entidad JPA que representa a un jugador inscrito en la escuela.
 * <p>
 * Contiene informacion personal, relacion con tutor, configuracion de cuotas
 * y datos administrativos de temporada.
 * </p>
 */
@Entity
@Table(name = Constantes.TABLA_JUGADORES)
public class Jugador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = Constantes.COLUMNA_TUTOR_ID)
    private Tutor tutor;

    @NotBlank(message = Constantes.MENSAJE_NOMBRE_OBLIGATORIO)
    private String nombre;

    @NotBlank(message = Constantes.MENSAJE_APELLIDOS_OBLIGATORIOS)
    private String apellidos;

    @NotNull(message = Constantes.MENSAJE_FECHA_NACIMIENTO_OBLIGATORIA)
    private LocalDate fechaNacimiento;

    private String categoria;

    private LocalDate fechaInscripcion;

    private BigDecimal cuotaTemporada;

    private String temporadaActual;

    @OneToMany(mappedBy = Constantes.MAPPED_BY_JUGADOR, cascade = CascadeType.ALL)
    private List<Equipacion> equipaciones;

    @OneToMany(mappedBy = Constantes.MAPPED_BY_JUGADOR, cascade = CascadeType.ALL)
    private List<Pago> pagos;

    @Enumerated(EnumType.STRING)
    private EstadoJugador estado = EstadoJugador.PENDIENTE;
    private Integer numeroCuotas = 1;
    private boolean necesitaEquipacion;
    /**
     * Obtiene el identificador unico del jugador.
     *
     * @return id persistido del jugador
     */
    public Long getId() { return id; }

    /**
     * Define el identificador del jugador.
     *
     * @param id identificador a asignar
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Obtiene el tutor responsable del jugador.
     *
     * @return tutor asociado
     */
    public Tutor getTutor() { return tutor; }

    /**
     * Asocia un tutor al jugador.
     *
     * @param tutor tutor responsable
     */
    public void setTutor(Tutor tutor) { this.tutor = tutor; }

    /**
     * Obtiene el nombre del jugador.
     *
     * @return nombre del jugador
     */
    public String getNombre() { return nombre; }

    /**
     * Define el nombre del jugador.
     *
     * @param nombre nombre a asignar
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Obtiene los apellidos del jugador.
     *
     * @return apellidos del jugador
     */
    public String getApellidos() { return apellidos; }

    /**
     * Define los apellidos del jugador.
     *
     * @param apellidos apellidos a asignar
     */
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    /**
     * Obtiene la fecha de nacimiento.
     *
     * @return fecha de nacimiento del jugador
     */
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }

    /**
     * Define la fecha de nacimiento.
     *
     * @param fechaNacimiento fecha de nacimiento a asignar
     */
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    /**
     * Obtiene la categoria deportiva asignada.
     *
     * @return categoria actual
     */
    public String getCategoria() { return categoria; }

    /**
     * Define la categoria deportiva.
     *
     * @param categoria categoria a asignar
     */
    public void setCategoria(String categoria) { this.categoria = categoria; }

    /**
     * Obtiene la fecha de inscripcion del jugador.
     *
     * @return fecha de alta
     */
    public LocalDate getFechaInscripcion() { return fechaInscripcion; }

    /**
     * Define la fecha de inscripcion.
     *
     * @param fechaInscripcion fecha de alta a asignar
     */
    public void setFechaInscripcion(LocalDate fechaInscripcion) { this.fechaInscripcion = fechaInscripcion; }

    /**
     * Obtiene la cuota total de temporada.
     *
     * @return importe de cuota de temporada
     */
    public BigDecimal getCuotaTemporada() { return cuotaTemporada; }

    /**
     * Define la cuota total de temporada.
     *
     * @param cuotaTemporada importe de cuota a asignar
     */
    public void setCuotaTemporada(BigDecimal cuotaTemporada) { this.cuotaTemporada = cuotaTemporada; }

    /**
     * Obtiene la temporada administrativa actual.
     *
     * @return temporada vigente del jugador
     */
    public String getTemporadaActual() { return temporadaActual; }

    /**
     * Define la temporada administrativa actual.
     *
     * @param temporadaActual temporada a asignar
     */
    public void setTemporadaActual(String temporadaActual) { this.temporadaActual = temporadaActual; }

    /**
     * Obtiene las equipaciones asociadas al jugador.
     *
     * @return lista de cargos de equipacion
     */
    public List<Equipacion> getEquipaciones() { return equipaciones; }

    /**
     * Obtiene el historico de pagos del jugador.
     *
     * @return lista de pagos asociados
     */
    public List<Pago> getPagos() { return pagos; }

    /**
     * Define el historico de pagos del jugador.
     *
     * @param pagos coleccion de pagos a asociar
     */
    public void setPagos(List<Pago> pagos) { this.pagos = pagos; }

    /**
     * Indica si el jugador necesita equipacion.
     *
     * @return true si necesita equipacion; false en caso contrario
     */
    public boolean isNecesitaEquipacion() { return necesitaEquipacion; }

    /**
     * Define si el jugador necesita equipacion.
     *
     * @param necesitaEquipacion indicador de necesidad de equipacion
     */
    public void setNecesitaEquipacion(boolean necesitaEquipacion) { this.necesitaEquipacion = necesitaEquipacion; }

    /**
     * Obtiene el estado administrativo del jugador.
     *
     * @return estado actual del jugador
     */
    public EstadoJugador getEstado() { return estado; }

    /**
     * Define el estado administrativo del jugador.
     *
     * @param estado estado a asignar
     */
    public void setEstado(EstadoJugador estado) { this.estado = estado; }

    /**
     * Obtiene el numero de cuotas configurado.
     *
     * @return numero de cuotas pactadas
     */
    public Integer getNumeroCuotas() { return numeroCuotas; }

    /**
     * Define el numero de cuotas para la temporada.
     *
     * @param numeroCuotas numero de cuotas a asignar
     */
    public void setNumeroCuotas(Integer numeroCuotas) { this.numeroCuotas = numeroCuotas; }
}
