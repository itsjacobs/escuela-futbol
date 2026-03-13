package com.escuelafutbol.domain.service;

import com.escuelafutbol.commons.Constantes;
import com.escuelafutbol.data.repositories.JugadorRepository;
import com.escuelafutbol.data.repositories.PagoRepository;
import com.escuelafutbol.domain.dto.PagoDTO;
import com.escuelafutbol.domain.dto.PagoResponseDTO;
import com.escuelafutbol.domain.exception.JugadorNoEncontradoException;
import com.escuelafutbol.domain.exception.PagoNoEncontradoException;
import com.escuelafutbol.domain.exception.ReglaNegocioException;
import com.escuelafutbol.domain.model.EstadoJugador;
import com.escuelafutbol.domain.model.EstadoPago;
import com.escuelafutbol.domain.model.Jugador;
import com.escuelafutbol.domain.model.MetodoPago;
import com.escuelafutbol.domain.model.Pago;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de dominio para la gestión de pagos de jugadores.
 * <p>
 * Encapsula la lógica de negocio de pagos por transferencia y efectivo,
 * así como la progresión de cuotas de temporada.
 * <p>
 * Responsabilidades principales:
 * <ul>
 *   <li>Registrar pagos y controlar su estado ({@code PENDIENTE}/{@code CONFIRMADO})</li>
 *   <li>Confirmar o rechazar transferencias</li>
 *   <li>Generar primera cuota y cuotas sucesivas</li>
 *   <li>Calcular totales pagados y pendientes por jugador</li>
 *   <li>Aplicar control de autorización por propiedad del recurso</li>
 * </ul>
 * <p>
 * EN: Domain service that orchestrates payment lifecycle and installment rules.
 * ES: Servicio de dominio que orquesta el ciclo de vida de pagos y reglas de cuotas.
 */
@Service
public class PagoService {

    private final PagoRepository repository;
    private final JugadorRepository jugadorRepository;

    /**
     * Construye el servicio con los repositorios requeridos.
     *
     * @param repository repositorio de pagos
     * @param jugadorRepository repositorio de jugadores
     */
    public PagoService(PagoRepository repository, JugadorRepository jugadorRepository) {
        this.repository = repository;
        this.jugadorRepository = jugadorRepository;
    }

    /**
     * Convierte la entidad {@link Pago} en su DTO de respuesta para API.
     *
     * @param pago entidad persistida
     * @return DTO con información normalizada para cliente
     */
    private PagoResponseDTO convertirAResponseDTO(Pago pago) {
        return new PagoResponseDTO(
                pago.getId(),
                pago.getJugador().getNombre() + Constantes.ESPACIO + pago.getJugador().getApellidos(),
                pago.getImporte(),
                pago.getFechaPago(),
                pago.getMetodoPago() != null ? pago.getMetodoPago().name() : null,
                pago.getConcepto(),
                pago.getEstado() != null ? pago.getEstado().name() : null,
                pago.getRegistradoPor()
        );
    }

    /**
     * Registra un pago manual para un jugador autorizado.
     * <p>
     * Valida la existencia del jugador, el método de pago y las reglas de seguridad:
     * efectivo sólo permitido para administradores.
     *
     * @param pagoDTO datos de pago recibidos
     * @param emailUsuario email del usuario autenticado
     * @param esAdmin indica si el usuario autenticado es administrador
     * @return pago registrado en formato DTO
     * @throws ReglaNegocioException si faltan datos obligatorios
     * @throws JugadorNoEncontradoException si el jugador no existe
     * @throws AccessDeniedException si un usuario no admin intenta registrar efectivo
     */
    @Transactional
    public PagoResponseDTO registrarPago(PagoDTO pagoDTO, String emailUsuario, boolean esAdmin) {
        if (pagoDTO.jugadorId() == null) {
            throw new ReglaNegocioException(Constantes.MENSAJE_JUGADOR_ID_OBLIGATORIO);
        }

        Jugador jugador = obtenerJugadorAutorizado(pagoDTO.jugadorId(), emailUsuario, esAdmin);

        Pago pago = new Pago();
        pago.setJugador(jugador);
        pago.setImporte(pagoDTO.importe());
        pago.setFechaPago(LocalDate.now());
        MetodoPago metodoPago = MetodoPago.valueOf(pagoDTO.metodoPago());
        if (MetodoPago.EFECTIVO == metodoPago && !esAdmin) {
            throw new AccessDeniedException(Constantes.MENSAJE_SOLO_ADMIN_EFECTIVO);
        }
        pago.setMetodoPago(metodoPago);
        pago.setConcepto(pagoDTO.concepto());
        pago.setRegistradoPor(pagoDTO.registradoPor());

        if (MetodoPago.EFECTIVO == metodoPago) {
            pago.setEstado(EstadoPago.CONFIRMADO);
            pago.setFechaPago(LocalDate.now());
        } else {
            pago.setEstado(EstadoPago.PENDIENTE);
        }

        return convertirAResponseDTO(repository.save(pago));
    }

    /**
     * Confirma un pago pendiente y activa al jugador si estaba en estado inicial.
     *
     * @param pagoId identificador del pago
     * @return pago confirmado en formato DTO
     * @throws PagoNoEncontradoException si no existe el pago
     */
    @Transactional
    public PagoResponseDTO confirmarPago(Long pagoId) {
        Pago pago = repository.findById(pagoId)
                .orElseThrow(() -> new PagoNoEncontradoException(Constantes.MENSAJE_PAGO_NO_ENCONTRADO));

        pago.setEstado(EstadoPago.CONFIRMADO);
        pago.setFechaPago(LocalDate.now());
        repository.save(pago);

        Jugador jugador = pago.getJugador();

        if (jugador.getEstado() == EstadoJugador.PENDIENTE) {
            jugador.setEstado(EstadoJugador.ACTIVO);
            jugadorRepository.save(jugador);
        }


        return convertirAResponseDTO(pago);
    }

    /**
     * Genera la primera cuota pendiente para un jugador autorizado.
     * <p>
     * Reglas aplicadas:
     * <ul>
     *   <li>El número de cuotas debe ser mayor que cero</li>
     *   <li>Si ya hay parte de cuota confirmada, sólo se permiten hasta 2 cuotas restantes</li>
     *   <li>Si no queda pendiente, no se genera nueva cuota</li>
     * </ul>
     *
     * @param jugadorId identificador del jugador
     * @param numeroCuotas número de cuotas solicitado
     * @param emailTutor email del tutor/usuario que genera la cuota
     * @param esAdmin indicador de rol administrador
     * @return primer pago de cuota pendiente generado
     * @throws ReglaNegocioException si falla una regla de negocio de cuotas
     * @throws JugadorNoEncontradoException si el jugador no existe
     * @throws AccessDeniedException si el usuario no está autorizado sobre el jugador
     */
    @Transactional
    public PagoResponseDTO generarPrimeraCuota(Long jugadorId, Integer numeroCuotas, String emailTutor, boolean esAdmin) {
        Jugador jugador = obtenerJugadorAutorizado(jugadorId, emailTutor, esAdmin);

        if (numeroCuotas == null || numeroCuotas <= 0) {
            throw new ReglaNegocioException(Constantes.MENSAJE_NUMERO_CUOTAS_INVALIDO);
        }

        long cuotasConfirmadas = contarCuotasConfirmadas(jugadorId);
        BigDecimal subtotalPendiente = calcularSubtotalPendienteCuota(jugadorId, jugador.getCuotaTemporada());

        if (subtotalPendiente.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException(Constantes.MENSAJE_CUOTAS_COMPLETADAS);
        }

        boolean esCuotaCompletaSinPagosPrevios = subtotalPendiente.compareTo(jugador.getCuotaTemporada()) == 0;
        if (!esCuotaCompletaSinPagosPrevios && numeroCuotas > 2) {
            throw new ReglaNegocioException(Constantes.MENSAJE_MAXIMO_DOS_CUOTAS_RESTANTES);
        }

        int totalCuotasTemporada = (int) cuotasConfirmadas + numeroCuotas;
        jugador.setNumeroCuotas(totalCuotasTemporada);
        jugadorRepository.save(jugador);

        BigDecimal importeCuota = calcularImporteCuota(subtotalPendiente, numeroCuotas);
        long siguienteNumeroCuota = cuotasConfirmadas + 1;
        Pago pago = crearPagoCuotaPendiente(jugador, emailTutor, importeCuota, siguienteNumeroCuota);

        return convertirAResponseDTO(repository.save(pago));
    }

    /**
     * Rechaza un pago pendiente.
     * <p>
     * Si el pago corresponde al alta inicial sin historial confirmado,
     * elimina también al jugador y sus pagos asociados; en caso contrario,
     * elimina únicamente el pago rechazado.
     *
     * @param pagoId identificador del pago a rechazar
     * @throws PagoNoEncontradoException si no existe el pago
     */
    @Transactional
    public void rechazarPago(Long pagoId) {
        Pago pago = repository.findById(pagoId)
                .orElseThrow(() -> new PagoNoEncontradoException(Constantes.MENSAJE_PAGO_NO_ENCONTRADO_CON_ID + pagoId));

        Jugador jugador = pago.getJugador();
        String concepto = pago.getConcepto() != null ? pago.getConcepto() : Constantes.CADENA_VACIA;

        boolean tieneEquipacionConfirmada = repository.findByJugadorId(jugador.getId())
                .stream()
                .anyMatch(p -> p.getEstado() == EstadoPago.CONFIRMADO
                        && p.getConcepto() != null
                        && p.getConcepto().startsWith(Constantes.PREFIJO_CONCEPTO_EQUIPACION_RESUMEN));

        boolean esPrimerPagoSinHistorial = jugador.getEstado() == EstadoJugador.PENDIENTE
                || (!tieneEquipacionConfirmada && concepto.startsWith(Constantes.PREFIJO_CONCEPTO_CUOTA_1))
                || concepto.startsWith(Constantes.PREFIJO_CONCEPTO_EQUIPACION_RESUMEN);

        if (esPrimerPagoSinHistorial && !tieneEquipacionConfirmada) {
            repository.deleteAll(jugador.getPagos());
            jugadorRepository.delete(jugador);
        } else {
            repository.delete(pago);
        }
    }

    /**
     * Recupera todos los pagos pendientes del sistema (vista administrativa).
     *
     * @return listado de pagos pendientes en DTO
     */
    @Transactional
    public List<PagoResponseDTO> getPendientes() {
        return repository.findByEstadoConJugador(EstadoPago.PENDIENTE)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    /**
     * Recupera todos los pagos registrados.
     *
     * @return listado completo de pagos en DTO
     */
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    /**
     * Recupera pagos de un jugador aplicando autorización por usuario.
     *
     * @param jugadorId identificador del jugador
     * @param emailUsuario email del usuario autenticado
     * @param esAdmin indicador de rol administrador
     * @return pagos del jugador en formato DTO
     * @throws JugadorNoEncontradoException si el jugador no existe
     * @throws AccessDeniedException si no hay permisos sobre el jugador
     */
    @Transactional(readOnly = true)
    public List<PagoResponseDTO> findByJugadorId(Long jugadorId, String emailUsuario, boolean esAdmin) {
        Jugador jugador = obtenerJugadorAutorizado(jugadorId, emailUsuario, esAdmin);
        return repository.findByJugadorId(jugador.getId())
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    /**
     * Calcula el total confirmado pagado por un jugador autorizado.
     *
     * @param jugadorId identificador del jugador
     * @param emailUsuario email del usuario autenticado
     * @param esAdmin indicador de rol administrador
     * @return suma total de importes confirmados
     * @throws JugadorNoEncontradoException si el jugador no existe
     * @throws AccessDeniedException si no hay permisos sobre el jugador
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalPagado(Long jugadorId, String emailUsuario, boolean esAdmin) {
        Jugador jugador = obtenerJugadorAutorizado(jugadorId, emailUsuario, esAdmin);
        return repository.findByJugadorId(jugador.getId())
                .stream()
                .filter(p -> p.getEstado() == EstadoPago.CONFIRMADO)
                .map(Pago::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el importe pendiente de cuota de un jugador autorizado.
     *
     * @param jugadorId identificador del jugador
     * @param emailUsuario email del usuario autenticado
     * @param esAdmin indicador de rol administrador
     * @return importe pendiente de cuota
     * @throws JugadorNoEncontradoException si el jugador no existe
     * @throws AccessDeniedException si no hay permisos sobre el jugador
     */
    @Transactional(readOnly = true)
    public BigDecimal getPendienteJugador(Long jugadorId, String emailUsuario, boolean esAdmin) {
        Jugador jugador = obtenerJugadorAutorizado(jugadorId, emailUsuario, esAdmin);
        return calcularPendienteCuotaConfirmada(jugador);
    }

    /**
     * Elimina un pago por identificador.
     *
     * @param id identificador del pago
     */
    public void delete(Long id) {
        repository.deleteById(id);
    }

    /**
     * Registra un pago en efectivo (confirmado de forma inmediata).
     * <p>
     * Valida importe positivo y que no supere el pendiente actual del jugador.
     *
     * @param jugadorId identificador del jugador
     * @param importe importe entregado en efectivo
     * @param registradoPor usuario que registra el pago
     * @return pago en efectivo confirmado
     * @throws JugadorNoEncontradoException si el jugador no existe
     * @throws ReglaNegocioException si el importe es inválido o supera el pendiente
     */
    @Transactional
    public PagoResponseDTO registrarPagoEfectivo(Long jugadorId, BigDecimal importe, String registradoPor) {
        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new JugadorNoEncontradoException(Constantes.MENSAJE_JUGADOR_NO_ENCONTRADO));

        if (importe == null || importe.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException(Constantes.MENSAJE_IMPORTE_MAYOR_CERO);
        }

        BigDecimal pendiente = calcularPendienteCuotaConfirmada(jugador);
        if (importe.compareTo(pendiente) > 0) {
            throw new ReglaNegocioException(Constantes.MENSAJE_IMPORTE_EFECTIVO_SUPERA_PENDIENTE);
        }

        long siguienteNumeroCuota = contarCuotasConfirmadas(jugadorId) + 1;

        Pago pago = new Pago();
        pago.setJugador(jugador);
        pago.setImporte(importe);
        pago.setFechaPago(LocalDate.now());
        pago.setMetodoPago(MetodoPago.EFECTIVO);
        pago.setConcepto(construirConceptoCuota(jugador, siguienteNumeroCuota));
        pago.setRegistradoPor(registradoPor);
        pago.setEstado(EstadoPago.CONFIRMADO);

        return convertirAResponseDTO(repository.save(pago));
    }

    /**
     * Genera la siguiente cuota pendiente para un jugador autorizado.
     * <p>
     * Considera cuotas ya confirmadas, cuotas restantes y pendiente de temporada.
     *
     * @param jugadorId identificador del jugador
     * @param emailTutor email del usuario que solicita la operación
     * @param esAdmin indicador de rol administrador
     * @return siguiente cuota pendiente generada
     * @throws JugadorNoEncontradoException si el jugador no existe
     * @throws ReglaNegocioException si no quedan cuotas por generar
     * @throws AccessDeniedException si no hay permisos sobre el jugador
     */
    @Transactional
    public PagoResponseDTO generarSiguienteCuota(Long jugadorId, String emailTutor, boolean esAdmin) {
        Jugador jugador = obtenerJugadorAutorizado(jugadorId, emailTutor, esAdmin);

        int totalCuotas = jugador.getNumeroCuotas() != null ? jugador.getNumeroCuotas() : Constantes.NUMERO_CUOTAS_POR_DEFECTO;

        long cuotasConfirmadas = contarCuotasConfirmadas(jugadorId);

        if (cuotasConfirmadas >= totalCuotas) {
            throw new ReglaNegocioException(Constantes.MENSAJE_CUOTAS_COMPLETADAS);
        }

        BigDecimal subtotalPendiente = calcularSubtotalPendienteCuota(jugadorId, jugador.getCuotaTemporada());
        int cuotasRestantes = (int) (totalCuotas - cuotasConfirmadas);

        if (subtotalPendiente.compareTo(BigDecimal.ZERO) <= 0 || cuotasRestantes <= 0) {
            throw new ReglaNegocioException(Constantes.MENSAJE_CUOTAS_COMPLETADAS);
        }

        BigDecimal importeCuota = calcularImporteCuota(subtotalPendiente, cuotasRestantes);
        Pago pago = crearPagoCuotaPendiente(jugador, emailTutor, importeCuota, cuotasConfirmadas + 1);

        return convertirAResponseDTO(repository.save(pago));
    }

    /**
     * Cuenta cuántas cuotas confirmadas tiene un jugador.
     *
     * @param jugadorId identificador del jugador
     * @return número de cuotas confirmadas
     */
    private long contarCuotasConfirmadas(Long jugadorId) {
        return repository.findByJugadorId(jugadorId).stream()
                .filter(p -> p.getEstado() == EstadoPago.CONFIRMADO)
                .filter(p -> p.getConcepto() != null && p.getConcepto().startsWith(Constantes.PREFIJO_CONCEPTO_CUOTA))
                .count();
    }

    /**
     * Calcula el subtotal pendiente de cuota (sin incluir equipación).
     *
     * @param jugadorId identificador del jugador
     * @param cuotaTemporada cuota anual total del jugador
     * @return subtotal pendiente de cuota
     */
    private BigDecimal calcularSubtotalPendienteCuota(Long jugadorId, BigDecimal cuotaTemporada) {
        return cuotaTemporada.subtract(calcularTotalCuotaConfirmada(jugadorId));
    }

    /**
     * Suma todos los importes confirmados asociados a conceptos de cuota.
     *
     * @param jugadorId identificador del jugador
     * @return total confirmado de cuotas
     */
    private BigDecimal calcularTotalCuotaConfirmada(Long jugadorId) {
        return repository.findByJugadorId(jugadorId).stream()
                .filter(p -> p.getEstado() == EstadoPago.CONFIRMADO)
                .filter(p -> p.getConcepto() != null && p.getConcepto().startsWith(Constantes.PREFIJO_CONCEPTO_CUOTA))
                .map(Pago::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula el importe unitario de cuota con redondeo a dos decimales.
     *
     * @param cuotaTemporada importe total a repartir
     * @param totalCuotas número de cuotas destino
     * @return importe de cada cuota
     */
    private BigDecimal calcularImporteCuota(BigDecimal cuotaTemporada, int totalCuotas) {
        return cuotaTemporada.divide(BigDecimal.valueOf(totalCuotas), 2, java.math.RoundingMode.HALF_UP);
    }

    /**
     * Construye una entidad de pago pendiente para cuota por transferencia.
     *
     * @param jugador jugador asociado
     * @param registradoPor usuario que registra el pago
     * @param importe importe de la cuota
     * @param numeroCuota ordinal de la cuota
     * @return entidad de pago pendiente lista para persistir
     */
    private Pago crearPagoCuotaPendiente(Jugador jugador, String registradoPor, BigDecimal importe, long numeroCuota) {
        Pago pago = new Pago();
        pago.setJugador(jugador);
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setMetodoPago(MetodoPago.TRANSFERENCIA);
        pago.setFechaPago(LocalDate.now());
        pago.setRegistradoPor(registradoPor);
        pago.setImporte(importe);
        pago.setConcepto(construirConceptoCuota(jugador, numeroCuota));
        return pago;
    }

    /**
     * Construye el concepto de cuota con formato
     * {@code CUOTAx-NOMBRE APELLIDOS-CATEGORIA}.
     *
     * @param jugador jugador asociado
     * @param numeroCuota ordinal de la cuota
     * @return concepto normalizado para transferencia
     */
    private String construirConceptoCuota(Jugador jugador, long numeroCuota) {
        return Constantes.PREFIJO_CONCEPTO_CUOTA + numeroCuota + Constantes.GUION + construirNombreCompletoConcepto(jugador);
    }

    /**
     * Obtiene un jugador validando autorización por propiedad del recurso.
     *
     * @param jugadorId identificador del jugador
     * @param emailUsuario email del usuario autenticado
     * @param esAdmin indicador de rol administrador
     * @return jugador autorizado
     * @throws JugadorNoEncontradoException si no existe jugador
     * @throws AccessDeniedException si no hay permisos sobre el jugador
     */
    private Jugador obtenerJugadorAutorizado(Long jugadorId, String emailUsuario, boolean esAdmin) {
        Jugador jugador = jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new JugadorNoEncontradoException(Constantes.MENSAJE_JUGADOR_NO_ENCONTRADO));
        validarAccesoJugador(jugador, emailUsuario, esAdmin);
        return jugador;
    }

    /**
     * Verifica si el usuario puede operar sobre pagos del jugador.
     *
     * @param jugador jugador objetivo
     * @param emailUsuario email del usuario autenticado
     * @param esAdmin indicador de rol administrador
     * @throws AccessDeniedException si el tutor autenticado no es propietario
     */
    private void validarAccesoJugador(Jugador jugador, String emailUsuario, boolean esAdmin) {
        if (esAdmin) {
            return;
        }

        if (jugador.getTutor() == null || jugador.getTutor().getEmail() == null
                || !jugador.getTutor().getEmail().equalsIgnoreCase(emailUsuario)) {
            throw new AccessDeniedException(Constantes.MENSAJE_ACCESO_DENEGADO_PAGOS);
        }
    }

    /**
     * Calcula el pendiente de cuota confirmado de un jugador.
     *
     * @param jugador jugador objetivo
     * @return importe de cuota pendiente
     */
    private BigDecimal calcularPendienteCuotaConfirmada(Jugador jugador) {
        BigDecimal cuota = jugador.getCuotaTemporada() != null ? jugador.getCuotaTemporada() : BigDecimal.ZERO;
        return cuota.subtract(calcularTotalCuotaConfirmada(jugador.getId()));
    }

    /**
     * Construye el tramo de concepto con formato
     * {@code NOMBRE APELLIDOS-CATEGORIA} en mayúsculas.
     *
     * @param jugador jugador objetivo
     * @return texto normalizado para concepto
     */
    private String construirNombreCompletoConcepto(Jugador jugador) {
        String nombre = jugador.getNombre() != null ? jugador.getNombre().trim() : Constantes.CADENA_VACIA;
        String apellidos = jugador.getApellidos() != null ? jugador.getApellidos().trim() : Constantes.CADENA_VACIA;
        String categoria = jugador.getCategoria() != null ? jugador.getCategoria().trim() : Constantes.CADENA_VACIA;
        return (nombre + Constantes.ESPACIO + apellidos + Constantes.GUION + categoria).trim().toUpperCase();
    }
}
