package com.escuelafutbol.domain.model;

import com.escuelafutbol.commons.Constantes;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad JPA que representa un pago registrado para un jugador.
 * <p>
 * Almacena datos economicos, estado del cobro y metadatos basicos de auditoria.
 * </p>
 */
@Entity
@Table(name = Constantes.TABLA_PAGOS)
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = Constantes.COLUMNA_JUGADOR_ID)
    private Jugador jugador;

    private BigDecimal importe;

    private LocalDate fechaPago;

    @Enumerated(EnumType.STRING)
    private EstadoPago estado = EstadoPago.PENDIENTE;

    private String concepto;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    private String registradoPor;

    /**
     * Obtiene el identificador unico del pago.
     *
     * @return id persistido del pago
     */
    public Long getId() { return id; }

    /**
     * Define el identificador del pago.
     *
     * @param id identificador a asignar
     */
    public void setId(Long id) { this.id = id; }

    /**
     * Obtiene el jugador asociado al pago.
     *
     * @return jugador titular del pago
     */
    public Jugador getJugador() { return jugador; }

    /**
     * Asocia el pago a un jugador.
     *
     * @param jugador jugador titular del pago
     */
    public void setJugador(Jugador jugador) { this.jugador = jugador; }

    /**
     * Devuelve el importe abonado.
     *
     * @return cantidad monetaria del pago
     */
    public BigDecimal getImporte() { return importe; }

    /**
     * Establece el importe del pago.
     *
     * @param importe cantidad monetaria abonada
     */
    public void setImporte(BigDecimal importe) { this.importe = importe; }

    /**
     * Obtiene la fecha efectiva del pago.
     *
     * @return fecha de registro o ejecucion del pago
     */
    public LocalDate getFechaPago() { return fechaPago; }

    /**
     * Define la fecha del pago.
     *
     * @param fechaPago fecha efectiva del abono
     */
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }

    /**
     * Obtiene el metodo de pago utilizado.
     *
     * @return metodo de pago registrado
     */
    public MetodoPago getMetodoPago() { return metodoPago; }

    /**
     * Define el metodo de pago.
     *
     * @param metodoPago metodo utilizado para abonar
     */
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    /**
     * Obtiene el concepto del pago.
     *
     * @return descripcion del concepto liquidado
     */
    public String getConcepto() { return concepto; }

    /**
     * Define el concepto del pago.
     *
     * @param concepto descripcion funcional del cobro
     */
    public void setConcepto(String concepto) { this.concepto = concepto; }

    /**
     * Obtiene quien registro el pago.
     *
     * @return identificador o nombre del registrador
     */
    public String getRegistradoPor() { return registradoPor; }

    /**
     * Define el actor que registro el pago.
     *
     * @param registradoPor usuario o sistema que registra
     */
    public void setRegistradoPor(String registradoPor) { this.registradoPor = registradoPor; }

    /**
     * Obtiene el estado del pago.
     *
     * @return estado actual del flujo de pago
     */
    public EstadoPago getEstado() { return estado; }

    /**
     * Define el estado del pago.
     *
     * @param estado nuevo estado de pago
     */
    public void setEstado(EstadoPago estado) { this.estado = estado; }
}
