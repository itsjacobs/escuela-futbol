package com.escuelafutbol.domain.service;

import com.escuelafutbol.commons.Constantes;
import com.escuelafutbol.commons.TemporadaUtils;
import com.escuelafutbol.data.repositories.EquipacionRepository;
import com.escuelafutbol.data.repositories.JugadorRepository;
import com.escuelafutbol.domain.dto.EquipacionDTO;
import com.escuelafutbol.domain.dto.EquipacionResponseDTO;
import com.escuelafutbol.domain.exception.JugadorNoEncontradoException;
import com.escuelafutbol.domain.exception.ReglaNegocioException;
import com.escuelafutbol.domain.model.Equipacion;
import com.escuelafutbol.domain.model.Jugador;
import com.escuelafutbol.domain.model.MotivoEquipacion;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de dominio para la gestión de equipaciones por jugador y temporada.
 * <p>
 * Centraliza la lógica de negocio relacionada con:
 * <ul>
 *   <li>Alta de registros de equipación</li>
 *   <li>Consulta de equipaciones por jugador</li>
 *   <li>Verificación de equipación en una temporada concreta</li>
 *   <li>Control de autorización por propiedad del recurso (anti-IDOR)</li>
 * </ul>
 * <p>
 * Este servicio trabaja sobre {@link EquipacionRepository} para persistencia y
 * {@link JugadorRepository} para validación de existencia/autorización del jugador.
 */
@Service
public class EquipacionService {

    private final EquipacionRepository repository;
    private final JugadorRepository jugadorRepository;

    public EquipacionService(EquipacionRepository repository, JugadorRepository jugadorRepository) {
        this.repository = repository;
        this.jugadorRepository = jugadorRepository;
    }

    /**
     * Convierte una entidad {@link Equipacion} en su DTO de salida para API.
     * <p>
     * Compone el nombre completo del jugador y expone únicamente los campos
     * necesarios para la capa de presentación.
     *
     * @param e entidad de equipación persistida
     * @return DTO de respuesta con los datos normalizados para cliente
     */
    private EquipacionResponseDTO convertirAResponseDTO(Equipacion e) {
        return new EquipacionResponseDTO(
                e.getId(),
                e.getJugador().getNombre() + Constantes.ESPACIO + e.getJugador().getApellidos(),
                e.getImporte(),
                e.getFechaPago(),
                e.getMotivo().name(),
                e.getTemporada()
        );
    }

    /**
     * Registra una nueva equipación para un jugador en la temporada actual.
     * <p>
     * Flujo de negocio:
     * <ol>
     *   <li>Valida que el jugador exista</li>
     *   <li>Calcula la temporada activa mediante {@link TemporadaUtils}</li>
     *   <li>Impide duplicados de equipación para el mismo jugador/temporada</li>
     *   <li>Construye y persiste la entidad de equipación</li>
     * </ol>
     *
     * @param dto datos de entrada (jugador, motivo e importe)
     * @return DTO con la equipación registrada
     * @throws JugadorNoEncontradoException si el jugador no existe
     * @throws ReglaNegocioException si ya existe equipación para ese jugador en la temporada actual
     * @see TemporadaUtils#calcularTemporadaActual()
     */
    public EquipacionResponseDTO registrarEquipacion(EquipacionDTO dto) {
        Jugador jugador = jugadorRepository.findById(dto.jugadorId())
                .orElseThrow(() -> new JugadorNoEncontradoException(Constantes.MENSAJE_JUGADOR_NO_ENCONTRADO));

        String temporada = TemporadaUtils.calcularTemporadaActual();

        if (repository.existsByJugadorIdAndTemporada(jugador.getId(), temporada)) {
            throw new ReglaNegocioException(Constantes.MENSAJE_EQUIPACION_DUPLICADA + temporada);
        }

        Equipacion equipacion = new Equipacion();
        equipacion.setJugador(jugador);
        equipacion.setImporte(dto.importe());
        equipacion.setMotivo(MotivoEquipacion.valueOf(dto.motivo()));
        equipacion.setTemporada(temporada);
        equipacion.setFechaPago(java.time.LocalDate.now());

        repository.save(equipacion);

        return convertirAResponseDTO(equipacion);
    }

    /**
     * Obtiene todas las equipaciones de un jugador autorizando por propietario o admin.
     * <p>
     * Si el usuario no es administrador, sólo puede consultar equipaciones de jugadores
     * asociados a su cuenta (email del tutor).
     *
     * @param jugadorId identificador del jugador consultado
     * @param emailUsuario email del usuario autenticado
     * @param esAdmin indica si el usuario autenticado tiene rol administrador
     * @return listado de equipaciones del jugador en formato DTO
     * @throws JugadorNoEncontradoException si el jugador no existe
     * @throws AccessDeniedException si el usuario no tiene permisos sobre el jugador
     */
    public List<EquipacionResponseDTO> findByJugadorIdParaUsuario(Long jugadorId, String emailUsuario, boolean esAdmin) {
        Jugador jugador = obtenerJugadorAutorizado(jugadorId, emailUsuario, esAdmin);
        return repository.findByJugadorId(jugador.getId())
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    /**
     * Recupera todas las equipaciones registradas en el sistema.
     * <p>
     * Método pensado para contexto administrativo (el control de acceso se aplica
     * en capa API/configuración de seguridad).
     *
     * @return listado completo de equipaciones en formato DTO
     */
    public List<EquipacionResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    /**
     * Indica si un jugador dispone de equipación en una temporada concreta,
     * aplicando control de autorización por usuario.
     *
     * @param jugadorId identificador del jugador
     * @param temporada temporada a consultar (por ejemplo, "2026-2027")
     * @param emailUsuario email del usuario autenticado
     * @param esAdmin indica si el usuario autenticado es administrador
     * @return {@code true} si existe al menos una equipación para jugador/temporada; {@code false} en caso contrario
     * @throws JugadorNoEncontradoException si el jugador no existe
     * @throws AccessDeniedException si el usuario no está autorizado sobre el jugador
     */
    public boolean tieneEquipacionEnTemporadaParaUsuario(Long jugadorId, String temporada, String emailUsuario, boolean esAdmin) {
        Jugador jugador = obtenerJugadorAutorizado(jugadorId, emailUsuario, esAdmin);
        return repository.existsByJugadorIdAndTemporada(jugador.getId(), temporada);
    }

    /**
     * Elimina una equipación por identificador.
     * <p>
     * La validación de permisos y el alcance funcional de este borrado se controlan
     * desde la capa API/seguridad.
     *
     * @param id identificador de la equipación a eliminar
     */
    public void delete(Long id) {
        repository.deleteById(id);
    }

    /**
     * Recupera un jugador garantizando que el usuario autenticado tiene permisos
     * para operar sobre él.
     * <p>
     * Reglas de autorización:
     * <ul>
     *   <li>Si es admin, el acceso es directo</li>
     *   <li>Si no es admin, el jugador debe pertenecer al tutor autenticado</li>
     * </ul>
     *
     * @param jugadorId identificador del jugador
     * @param emailUsuario email del usuario autenticado
     * @param esAdmin bandera de rol administrador
     * @return entidad de jugador autorizada
     * @throws JugadorNoEncontradoException si el jugador no existe
     * @throws AccessDeniedException si no se cumple la regla de autorización
     */
    private Jugador obtenerJugadorAutorizado(Long jugadorId, String emailUsuario, boolean esAdmin) {
        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new JugadorNoEncontradoException(Constantes.MENSAJE_JUGADOR_NO_ENCONTRADO));
        if (!esAdmin && (jugador.getTutor() == null || jugador.getTutor().getEmail() == null
                || !jugador.getTutor().getEmail().equalsIgnoreCase(emailUsuario))) {
            throw new AccessDeniedException(Constantes.MENSAJE_ACCESO_DENEGADO_EQUIPACION);
        }
        return jugador;
    }
}
