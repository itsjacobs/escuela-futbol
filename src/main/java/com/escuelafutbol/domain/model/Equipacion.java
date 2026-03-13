package com.escuelafutbol.domain.model;

import com.escuelafutbol.commons.Constantes;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad JPA para registrar cargos de equipacion de un jugador.
 * <p>
 * Persiste el importe, la fecha y el motivo por el que se ha generado
 * el cobro de equipacion para una temporada concreta.
 * </p>
 */
@Entity
@Table(name = Constantes.TABLA_EQUIPACIONES)
public class Equipacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = Constantes.COLUMNA_JUGADOR_ID)
    private Jugador jugador;

    private LocalDate fechaPago;

    private BigDecimal importe;

    @Enumerated(EnumType.STRING)
    private MotivoEquipacion motivo;

    private String temporada;

    /**
     * Obtiene el identificador unico del registro de equipacion.
     *
     * @return id persistido
     */
    public Long getId() { return id; }

    /**
     * Define el identificador del registro.
     *
     * @param id identificador a asignar
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Obtiene el jugador al que pertenece la equipacion.
     *
     * @return jugador asociado
     */
    public Jugador getJugador() { return jugador; }

    /**
     * Asocia el registro de equipacion a un jugador.
     *
     * @param jugador jugador propietario del cargo
     */
    public void setJugador(Jugador jugador) { this.jugador = jugador; }

    /**
     * Obtiene la fecha de pago de la equipacion.
     *
     * @return fecha de abono
     */
    public LocalDate getFechaPago() { return fechaPago; }

    /**
     * Define la fecha del pago de equipacion.
     *
     * @param fechaPago fecha efectiva del pago
     */
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }

    /**
     * Obtiene el importe cobrado por equipacion.
     *
     * @return importe monetario
     */
    public BigDecimal getImporte() { return importe; }

    /**
     * Define el importe de equipacion.
     *
     * @param importe cantidad cobrada
     */
    public void setImporte(BigDecimal importe) { this.importe = importe; }

    /**
     * Obtiene el motivo del cargo de equipacion.
     *
     * @return motivo funcional del cobro
     */
    public MotivoEquipacion getMotivo() { return motivo; }

    /**
     * Define el motivo del cobro.
     *
     * @param motivo motivo funcional de equipacion
     */
    public void setMotivo(MotivoEquipacion motivo) { this.motivo = motivo; }

    /**
     * Obtiene la temporada asociada al cargo.
     *
     * @return temporada del registro
     */
    public String getTemporada() { return temporada; }

    /**
     * Define la temporada del cargo de equipacion.
     *
     * @param temporada etiqueta de temporada
     */
    public void setTemporada(String temporada) { this.temporada = temporada; }
}
