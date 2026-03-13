package com.escuelafutbol.domain.service;

import com.escuelafutbol.commons.Constantes;
import com.escuelafutbol.commons.TemporadaUtils;
import com.escuelafutbol.data.repositories.EquipacionRepository;
import com.escuelafutbol.data.repositories.JugadorRepository;
import com.escuelafutbol.data.repositories.PagoRepository;
import com.escuelafutbol.data.repositories.TutorRepository;
import com.escuelafutbol.domain.dto.JugadorAdminResponseDTO;
import com.escuelafutbol.domain.dto.JugadorDTO;
import com.escuelafutbol.domain.dto.JugadorResponseDTO;
import com.escuelafutbol.domain.exception.InscripcionInvalidaException;
import com.escuelafutbol.domain.exception.JugadorNoEncontradoException;
import com.escuelafutbol.domain.exception.TutorNoEncontradoException;
import com.escuelafutbol.domain.model.EstadoJugador;
import com.escuelafutbol.domain.model.EstadoPago;
import com.escuelafutbol.domain.model.Jugador;
import com.escuelafutbol.domain.model.MetodoPago;
import com.escuelafutbol.domain.model.Pago;
import com.escuelafutbol.domain.model.Tutor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de dominio para la gestión integral de jugadores.
 * <p>
 * Centraliza la lógica de negocio relacionada con:
 * <ul>
 *   <li>Inscripción de jugadores en temporada</li>
 *   <li>Cálculo de categoría y cuota anual</li>
 *   <li>Creación del primer pago asociado a la inscripción</li>
 *   <li>Consultas para panel tutor y panel admin</li>
 *   <li>Controles de autorización por propiedad del recurso (anti-IDOR)</li>
 * </ul>
 * <p>
 * EN: Domain service for player lifecycle and enrollment business rules.
 * ES: Servicio de dominio para el ciclo de vida del jugador y reglas de inscripción.
 */
@Service
public class JugadorService {

    private final JugadorRepository jugadorRepository;
    private final TutorRepository tutorRepository;
    private final PagoRepository pagoRepository;
    private final EquipacionRepository equipacionRepository;

    /**
     * Crea una instancia del servicio con los repositorios necesarios.
     *
     * @param jugadorRepository repositorio principal de jugadores
     * @param tutorRepository repositorio de tutores para resolución de propietario
     * @param pagoRepository repositorio de pagos para operaciones derivadas
     * @param equipacionRepository repositorio de equipaciones para borrado asociado
     */
    public JugadorService(JugadorRepository jugadorRepository, TutorRepository tutorRepository,
                          PagoRepository pagoRepository, EquipacionRepository equipacionRepository) {
        this.jugadorRepository = jugadorRepository;
        this.tutorRepository = tutorRepository;
        this.pagoRepository = pagoRepository;
        this.equipacionRepository = equipacionRepository;
    }

    /**
     * Registra un nuevo jugador para el tutor autenticado.
     * <p>
     * Flujo funcional:
     * <ol>
     *   <li>Localiza el tutor por email</li>
     *   <li>Calcula categoría y cuota de temporada</li>
     *   <li>Persiste el jugador en estado inicial {@code PENDIENTE}</li>
     *   <li>Genera el primer pago pendiente (equipación o primera cuota)</li>
     * </ol>
     *
     * @param dto datos de inscripción del jugador
     * @param emailTutor email del tutor autenticado que realiza el alta
     * @return DTO de jugador con datos agregados de cuota/pendiente
     * @throws TutorNoEncontradoException si no existe tutor para el email indicado
     * @throws InscripcionInvalidaException si el año/categoría no es válido
     */
    @Transactional
    public JugadorResponseDTO save(JugadorDTO dto, String emailTutor) {
        Tutor tutor = tutorRepository.findByEmail(emailTutor)
                .orElseThrow(() -> new TutorNoEncontradoException(Constantes.MENSAJE_TUTOR_NO_ENCONTRADO));

        String categoria = calcularCategoria(dto.fechaNacimiento());
        BigDecimal cuota = calcularCuota(categoria);
        String temporada = TemporadaUtils.calcularTemporadaActual();

        Jugador jugador = new Jugador();
        jugador.setNombre(dto.nombre());
        jugador.setApellidos(dto.apellidos());
        jugador.setFechaNacimiento(dto.fechaNacimiento());
        jugador.setCategoria(categoria);
        jugador.setNecesitaEquipacion(dto.necesitaEquipacion());
        jugador.setTutor(tutor);
        jugador.setEstado(EstadoJugador.PENDIENTE);
        int numeroCuotas = obtenerNumeroCuotas(dto);
        jugador.setNumeroCuotas(numeroCuotas);
        jugador.setFechaInscripcion(LocalDate.now());
        jugador.setCuotaTemporada(cuota);
        jugador.setTemporadaActual(temporada);
        jugador = jugadorRepository.save(jugador);

        Pago pago = new Pago();
        pago.setJugador(jugador);
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setMetodoPago(MetodoPago.TRANSFERENCIA);
        pago.setFechaPago(LocalDate.now());
        pago.setRegistradoPor(emailTutor);

        BigDecimal importe;
        String concepto;

        if (dto.necesitaEquipacion()) {
            importe = Constantes.IMPORTE_EQUIPACION;
            concepto = Constantes.PREFIJO_CONCEPTO_EQUIPACION + construirNombreCompletoConcepto(jugador);
        } else {
            importe = cuota.divide(BigDecimal.valueOf(numeroCuotas), 2, RoundingMode.HALF_UP);
            concepto = Constantes.PREFIJO_CONCEPTO_CUOTA_1 + construirNombreCompletoConcepto(jugador);
        }

        pago.setImporte(importe);
        pago.setConcepto(concepto);
        pagoRepository.save(pago);

        return convertirAResponseDTO(jugador);
    }

    /**
     * Elimina un jugador y sus datos dependientes.
     * <p>
     * Se eliminan primero pagos y equipaciones para mantener consistencia
     * referencial, y posteriormente se elimina la entidad jugador.
     *
     * @param id identificador del jugador
     * @throws JugadorNoEncontradoException si el jugador no existe
     */
    @Transactional
    public void delete(Long id) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(() -> new JugadorNoEncontradoException(Constantes.MENSAJE_JUGADOR_NO_ENCONTRADO));
        pagoRepository.deleteAll(jugador.getPagos());
        equipacionRepository.deleteAll(jugador.getEquipaciones());
        jugadorRepository.delete(jugador);
    }

    /**
     * Recupera un jugador por id aplicando control de acceso por usuario.
     * <p>
     * Si el usuario no es admin, solo puede consultar jugadores asociados
     * a su propio tutor.
     *
     * @param id identificador del jugador
     * @param emailUsuario email del usuario autenticado
     * @param esAdmin indica si el usuario tiene permisos de administrador
     * @return DTO de jugador con datos para panel
     * @throws JugadorNoEncontradoException si no existe jugador con ese id
     * @throws AccessDeniedException si el usuario no tiene permisos sobre el recurso
     */
    @Transactional(readOnly = true)
    public JugadorResponseDTO findByIdParaUsuario(Long id, String emailUsuario, boolean esAdmin) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(() -> new JugadorNoEncontradoException(Constantes.MENSAJE_JUGADOR_NO_ENCONTRADO));
        validarAccesoJugador(jugador, emailUsuario, esAdmin);
        return convertirAResponseDTO(jugador);
    }

    /**
     * Lista todos los jugadores del sistema en formato tutor-response.
     *
     * @return listado completo de jugadores
     */
    @Transactional(readOnly = true)
    public List<JugadorResponseDTO> findAll() {
        return jugadorRepository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    /**
     * Lista jugadores filtrados por categoría.
     *
     * @param categoria categoría deportiva a consultar
     * @return jugadores de la categoría indicada
     */
    @Transactional(readOnly = true)
    public List<JugadorResponseDTO> findByCategoria(String categoria) {
        return jugadorRepository.findByCategoriaConPagos(categoria)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    /**
     * Lista jugadores por id de tutor.
     *
     * @param tutorId identificador del tutor
     * @return jugadores asociados al tutor
     */
    @Transactional(readOnly = true)
    public List<JugadorResponseDTO> findByTutorId(Long tutorId) {
        return jugadorRepository.findByTutorId(tutorId)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    /**
     * Lista jugadores activos del tutor autenticado.
     *
     * @param emailTutor email del tutor autenticado
     * @return jugadores en estado activo del tutor
     */
    @Transactional(readOnly = true)
    public List<JugadorResponseDTO> findByTutor(String emailTutor) {
        return jugadorRepository.findByTutorEmailAndEstadoConPagos(emailTutor, EstadoJugador.ACTIVO)
                .stream().map(this::convertirAResponseDTO).toList();
    }

    /**
     * Obtiene la vista administrativa de todos los jugadores.
     *
     * @return listado administrativo completo de jugadores
     */
    @Transactional(readOnly = true)
    public List<JugadorAdminResponseDTO> findAllAdmin() {
        return jugadorRepository.findAllConPagos()
                .stream().map(this::convertirAAdminResponseDTO).toList();
    }

    /**
     * Obtiene la vista administrativa de jugadores por categoría.
     *
     * @param categoria categoría deportiva
     * @return listado administrativo filtrado por categoría
     */
    @Transactional(readOnly = true)
    public List<JugadorAdminResponseDTO> findByCategoriaAdmin(String categoria) {
        return jugadorRepository.findByCategoriaConPagos(categoria)
                .stream().map(this::convertirAAdminResponseDTO).toList();
    }


    /**
     * Calcula la categoría deportiva en función del año de nacimiento.
     *
     * @param fecha fecha de nacimiento del jugador
     * @return nombre de categoría correspondiente
     * @throws InscripcionInvalidaException si el año no está permitido en la temporada
     */
    public String calcularCategoria(LocalDate fecha) {
        return switch (fecha.getYear()) {
            case 2008, 2009, 2010 -> Constantes.CATEGORIA_JUVENIL;
            case 2011, 2012 -> Constantes.CATEGORIA_CADETE;
            case 2013, 2014 -> Constantes.CATEGORIA_INFANTIL;
            case 2015, 2016 -> Constantes.CATEGORIA_ALEVIN;
            case 2017, 2018 -> Constantes.CATEGORIA_BENJAMIN;
            case 2019, 2020 -> Constantes.CATEGORIA_PREBENJAMIN;
            case 2021, 2022 -> Constantes.CATEGORIA_DEBUTANTE;
            default -> throw new InscripcionInvalidaException(Constantes.MENSAJE_ANIO_NO_PERMITIDO);
        };
    }

    /**
     * Calcula la cuota anual según categoría.
     *
     * @param categoria categoría deportiva
     * @return cuota anual aplicable
     * @throws InscripcionInvalidaException si la categoría no está contemplada
     */
    private BigDecimal calcularCuota(String categoria) {
        return switch (categoria) {
            case Constantes.CATEGORIA_PREBENJAMIN,
                 Constantes.CATEGORIA_BENJAMIN,
                 Constantes.CATEGORIA_DEBUTANTE,
                 Constantes.CATEGORIA_ALEVIN -> Constantes.CUOTA_CATEGORIA_BAJA;
            case Constantes.CATEGORIA_INFANTIL,
                 Constantes.CATEGORIA_CADETE,
                 Constantes.CATEGORIA_JUVENIL -> Constantes.CUOTA_CATEGORIA_ALTA;
            default -> throw new InscripcionInvalidaException(Constantes.MENSAJE_CATEGORIA_NO_VALIDA);
        };
    }


    /**
     * Convierte entidad de jugador a DTO de respuesta para tutor.
     * <p>
     * Incluye agregados calculados: total pagado, pendiente, equipación confirmada
     * y presencia de cuota pendiente.
     *
     * @param jugador entidad de jugador
     * @return DTO con métricas de pagos y datos de inscripción
     */
    private JugadorResponseDTO convertirAResponseDTO(Jugador jugador) {
        List<Pago> pagos = obtenerPagosSeguros(jugador);
        BigDecimal cuota = obtenerCuotaTemporada(jugador);
        BigDecimal totalCuotaPagada = calcularTotalCuotaPagada(pagos);
        BigDecimal pendiente = cuota.subtract(totalCuotaPagada);
        BigDecimal totalPagado = calcularTotalPagadoConfirmado(pagos);

        boolean equipacionConfirmada = pagos.stream()
                .anyMatch(p -> p.getEstado() == EstadoPago.CONFIRMADO
                        && p.getConcepto() != null
                        && p.getConcepto().startsWith(Constantes.PREFIJO_CONCEPTO_EQUIPACION_RESUMEN));

        boolean tieneCuotaPendiente = pagos.stream()
                .anyMatch(p -> p.getEstado() == EstadoPago.PENDIENTE
                        && p.getConcepto() != null
                        && p.getConcepto().startsWith(Constantes.PREFIJO_CONCEPTO_CUOTA));

        return new JugadorResponseDTO(
                jugador.getId(), jugador.getNombre(), jugador.getApellidos(),
                jugador.getFechaNacimiento(), jugador.getCategoria(),
                jugador.getFechaInscripcion(), jugador.getTemporadaActual(),
                cuota, totalPagado, pendiente,
                jugador.isNecesitaEquipacion(), equipacionConfirmada,
                tieneCuotaPendiente, jugador.getNumeroCuotas()
        );
    }

    /**
     * Convierte entidad de jugador a DTO administrativo enriquecido con datos del tutor.
     *
     * @param jugador entidad de jugador
     * @return DTO para panel administrativo
     */
    private JugadorAdminResponseDTO convertirAAdminResponseDTO(Jugador jugador) {
        List<Pago> pagos = obtenerPagosSeguros(jugador);
        BigDecimal cuota = obtenerCuotaTemporada(jugador);
        BigDecimal totalCuotaPagada = calcularTotalCuotaPagada(pagos);
        BigDecimal pendiente = cuota.subtract(totalCuotaPagada);
        BigDecimal totalPagado = calcularTotalPagadoConfirmado(pagos);

        return new JugadorAdminResponseDTO(
                jugador.getId(),
                jugador.getNombre(),
                jugador.getApellidos(),
                jugador.getFechaNacimiento(),
                jugador.getCategoria(),
                jugador.getFechaInscripcion(),
                jugador.getTemporadaActual(),
                cuota,
                totalPagado,
                pendiente,
                jugador.getTutor().getNombre(),
                jugador.getTutor().getApellidos(),
                jugador.getTutor().getEmail(),
                jugador.getTutor().getTelefono()
        );
    }

    /**
     * Obtiene número de cuotas solicitado o aplica valor por defecto.
     *
     * @param dto petición de inscripción
     * @return número de cuotas efectivo a almacenar
     */
    private int obtenerNumeroCuotas(JugadorDTO dto) {
        return dto.numeroCuotas() != null ? dto.numeroCuotas() : Constantes.NUMERO_CUOTAS_POR_DEFECTO;
    }

    /**
     * Devuelve colección de pagos segura evitando valores nulos.
     *
     * @param jugador entidad de jugador
     * @return lista de pagos, vacía si no hay colección inicializada
     */
    private List<Pago> obtenerPagosSeguros(Jugador jugador) {
        return jugador.getPagos() == null ? List.of() : jugador.getPagos();
    }

    /**
     * Devuelve cuota de temporada evitando nulos.
     *
     * @param jugador entidad de jugador
     * @return cuota anual o {@link BigDecimal#ZERO} si no está informada
     */
    private BigDecimal obtenerCuotaTemporada(Jugador jugador) {
        return jugador.getCuotaTemporada() != null ? jugador.getCuotaTemporada() : BigDecimal.ZERO;
    }

    /**
     * Suma importes confirmados únicamente de conceptos de cuota.
     *
     * @param pagos lista de pagos del jugador
     * @return total confirmado asociado a cuotas
     */
    private BigDecimal calcularTotalCuotaPagada(List<Pago> pagos) {
        return pagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.CONFIRMADO)
                .filter(p -> p.getConcepto() != null && p.getConcepto().startsWith(Constantes.PREFIJO_CONCEPTO_CUOTA))
                .map(Pago::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Suma todos los importes confirmados (cuotas y equipación).
     *
     * @param pagos lista de pagos del jugador
     * @return total pagado confirmado
     */
    private BigDecimal calcularTotalPagadoConfirmado(List<Pago> pagos) {
        return pagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.CONFIRMADO)
                .map(Pago::getImporte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Construye el sufijo de concepto de pago con formato
     * {@code NOMBRE APELLIDOS-CATEGORIA} en mayúsculas.
     *
     * @param jugador entidad de jugador
     * @return texto de concepto normalizado
     */
    private String construirNombreCompletoConcepto(Jugador jugador) {
        String nombre = normalizarTexto(jugador.getNombre());
        String apellidos = normalizarTexto(jugador.getApellidos());
        String categoria = normalizarTexto(jugador.getCategoria());
        return (nombre + Constantes.ESPACIO + apellidos + Constantes.GUION + categoria).trim().toUpperCase();
    }

    /**
     * Normaliza un texto para su uso en conceptos de pago.
     *
     * @param valor texto de entrada potencialmente nulo
     * @return texto recortado o cadena vacia si el valor es nulo
     */
    private String normalizarTexto(String valor) {
        return valor != null ? valor.trim() : Constantes.CADENA_VACIA;
    }

    /**
     * Valida autorización sobre un jugador concreto.
     * <p>
     * Regla: admin siempre permitido; tutor solo si el email coincide
     * con el propietario del jugador.
     *
     * @param jugador recurso objetivo
     * @param emailUsuario email del usuario autenticado
     * @param esAdmin indicador de rol administrador
     * @throws AccessDeniedException si no se cumple la regla de acceso
     */
    private void validarAccesoJugador(Jugador jugador, String emailUsuario, boolean esAdmin) {
        if (esAdmin) {
            return;
        }

        if (jugador.getTutor() == null || jugador.getTutor().getEmail() == null
                || !jugador.getTutor().getEmail().equalsIgnoreCase(emailUsuario)) {
            throw new AccessDeniedException(Constantes.MENSAJE_ACCESO_DENEGADO_JUGADOR);
        }
    }
}
