package com.escuelafutbol.domain.model;

import com.escuelafutbol.commons.Constantes;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidad JPA que representa a un tutor registrado en la plataforma.
 * <p>
 * Modela datos de identidad, credenciales, rol y relacion con jugadores
 * bajo su responsabilidad.
 * </p>
 */
@Entity
@Table(name = Constantes.TABLA_TUTORES)
public class Tutor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = Constantes.MENSAJE_NOMBRE_OBLIGATORIO)
    private String nombre;

    @NotBlank(message = Constantes.MENSAJE_APELLIDOS_OBLIGATORIOS)
    private String apellidos;

    @Email(message = Constantes.MENSAJE_EMAIL_NO_VALIDO)
    @NotBlank(message = Constantes.MENSAJE_EMAIL_OBLIGATORIO)
    @Column(unique = true)
    private String email;

    @NotBlank(message = Constantes.MENSAJE_CONTRASENA_OBLIGATORIA)
    private String password;

    private String telefono;

    private LocalDateTime fechaRegistro;

    @Enumerated(EnumType.STRING)
    private Rol rol;

    @OneToMany(mappedBy = Constantes.MAPPED_BY_TUTOR, cascade = CascadeType.ALL)
    private List<Jugador> jugadores;

    /**
     * Obtiene el identificador unico del tutor.
     *
     * @return id persistido
     */
    public Long getId() {
        return id;
    }

    /**
     * Define el identificador del tutor.
     *
     * @param id identificador a asignar
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre del tutor.
     *
     * @return nombre del tutor
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Define el nombre del tutor.
     *
     * @param nombre nombre a asignar
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene los apellidos del tutor.
     *
     * @return apellidos del tutor
     */
    public String getApellidos() {
        return apellidos;
    }

    /**
     * Define los apellidos del tutor.
     *
     * @param apellidos apellidos a asignar
     */
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    /**
     * Obtiene el correo de acceso del tutor.
     *
     * @return email registrado
     */
    public String getEmail() {
        return email;
    }

    /**
     * Define el correo de acceso del tutor.
     *
     * @param email direccion de correo unica
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtiene la contrasena almacenada.
     *
     * @return hash o valor de contrasena persistido
     */
    public String getPassword() {
        return password;
    }

    /**
     * Define la contrasena del tutor.
     *
     * @param password contrasena a persistir
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Obtiene el telefono de contacto.
     *
     * @return telefono del tutor
     */
    public String getTelefono() {
        return telefono;
    }

    /**
     * Define el telefono de contacto.
     *
     * @param telefono telefono a asignar
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    /**
     * Obtiene la fecha y hora de registro.
     *
     * @return fecha de alta del tutor
     */
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    /**
     * Define la fecha y hora de registro.
     *
     * @param fechaRegistro instante de alta
     */
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    /**
     * Obtiene el rol funcional del tutor.
     *
     * @return rol asignado
     */
    public Rol getRol() {
        return rol;
    }

    /**
     * Define el rol funcional del tutor.
     *
     * @param rol rol a asignar
     */
    public void setRol(Rol rol) {
        this.rol = rol;
    }

    /**
     * Obtiene la lista de jugadores asociados al tutor.
     *
     * @return jugadores bajo tutela
     */
    public List<Jugador> getJugadores() {
        return jugadores;
    }

    /**
     * Define la lista de jugadores asociados al tutor.
     *
     * @param jugadores coleccion de jugadores a asociar
     */
    public void setJugadores(List<Jugador> jugadores) {
        this.jugadores = jugadores;
    }
}
