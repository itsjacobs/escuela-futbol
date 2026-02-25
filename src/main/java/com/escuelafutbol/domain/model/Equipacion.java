package com.escuelafutbol.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "equipaciones")
public class Equipacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "jugador_id")
    private Jugador jugador;

    private LocalDate fechaPago;

    private BigDecimal importe;

    @Enumerated(EnumType.STRING)
    private Motivo motivo;

    private String temporada;

    public enum Motivo {
        NUEVA_INSCRIPCION, ROTURA, TALLA_PEQUEÑA, OTRO
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Jugador getJugador() { return jugador; }
    public void setJugador(Jugador jugador) { this.jugador = jugador; }
    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
    public Motivo getMotivo() { return motivo; }
    public void setMotivo(Motivo motivo) { this.motivo = motivo; }
    public String getTemporada() { return temporada; }
    public void setTemporada(String temporada) { this.temporada = temporada; }
}