package com.escuelafutbol.commons;

import java.math.BigDecimal;

/**
 * Contenedor central de constantes de aplicación.
 * <p>
 * Agrupa rutas HTTP, nombres de vistas, mensajes de error,
 * prefijos de negocio, literales de seguridad y claves de consulta.
 * <p>
 * EN: Central constant holder for routes, messages, security keys, and domain literals.
 * ES: Contenedor central de constantes para rutas, mensajes, claves de seguridad y literales de dominio.
 */
public final class Constantes {

	/**
	 * Constructor privado para evitar instanciación de clase utilitaria.
	 */
	private Constantes() {
	}

	public static final String ESPACIO = " ";
	public static final String GUION = "-";
	public static final String CADENA_VACIA = "";

	public static final String HEADER_AUTHORIZATION = "Authorization";
	public static final String PREFIJO_BEARER = "Bearer ";
	public static final int LONGITUD_PREFIJO_BEARER = 7;
	public static final String CLAIM_ROL = "rol";
	public static final String COOKIE_JWT_TOKEN = "jwt_token";
	public static final String COOKIE_PATH_ROOT = "/";
	public static final String COOKIE_SAMESITE_LAX = "Lax";

	public static final String MENSAJE_TUTOR_NO_ENCONTRADO = "Tutor no encontrado";
	public static final String MENSAJE_JUGADOR_NO_ENCONTRADO = "Jugador no encontrado";
	public static final String MENSAJE_PAGO_NO_ENCONTRADO = "Pago no encontrado";
	public static final String MENSAJE_PAGO_NO_ENCONTRADO_CON_ID = "Pago no encontrado: ";
	public static final String MENSAJE_USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
	public static final String MENSAJE_USUARIO_NO_ENCONTRADO_CON_EMAIL = "Usuario no encontrado: ";
	public static final String MENSAJE_CONTRASENA_INCORRECTA = "Contraseña incorrecta";
	public static final String MENSAJE_JUGADOR_ID_OBLIGATORIO = "jugadorId es obligatorio";
	public static final String MENSAJE_CUOTAS_COMPLETADAS = "Ya se han pagado todas las cuotas";
	public static final String MENSAJE_NUMERO_CUOTAS_INVALIDO = "El numero de cuotas debe ser mayor que cero";
	public static final String MENSAJE_MAXIMO_DOS_CUOTAS_RESTANTES =
			"Si ya existe un pago confirmado de cuota, solo puedes elegir un máximo de 2 cuotas restantes";
	public static final String MENSAJE_ANIO_NO_PERMITIDO = "El año seleccionado no se puede inscribir en esta temporada";
	public static final String MENSAJE_CATEGORIA_NO_VALIDA = "Categoria no valida para calcular cuota";
	public static final String MENSAJE_EQUIPACION_DUPLICADA = "El jugador ya tiene equipación en la temporada ";
	public static final String MENSAJE_SOLICITUD_NO_VALIDA = "Solicitud no valida";
	public static final String MENSAJE_TOKEN_INVALIDADO = "Token invalidado";
	public static final String MENSAJE_TOKEN_EXPIRADO = "Token expirado";
	public static final String MENSAJE_TOKEN_MALFORMADO = "Token malformado";
	public static final String MENSAJE_ERROR_AUTENTICACION = "Error de autenticación";
	public static final String MENSAJE_NOMBRE_OBLIGATORIO = "El nombre es obligatorio";
	public static final String MENSAJE_APELLIDOS_OBLIGATORIOS = "Los apellidos son obligatorios";
	public static final String MENSAJE_FECHA_NACIMIENTO_OBLIGATORIA = "La fecha de nacimiento es obligatoria";
	public static final String MENSAJE_EMAIL_NO_VALIDO = "El email no es valido";
	public static final String MENSAJE_EMAIL_OBLIGATORIO = "El email es obligatorio";
	public static final String MENSAJE_CONTRASENA_OBLIGATORIA = "La contraseña es obligatoria";
	public static final String MENSAJE_JUGADOR_OBLIGATORIO = "El jugador es obligatorio";
	public static final String MENSAJE_MOTIVO_OBLIGATORIO = "El motivo es obligatorio";
	public static final String MENSAJE_IMPORTE_OBLIGATORIO = "El importe es obligatorio";
	public static final String MENSAJE_IMPORTE_MAYOR_CERO = "El importe debe ser mayor que cero";
	public static final String MENSAJE_METODO_PAGO_OBLIGATORIO = "El método de pago es obligatorio";
	public static final String MENSAJE_NUMERO_CUOTAS_OBLIGATORIO = "El numero de cuotas es obligatorio";
	public static final String MENSAJE_SOLO_ADMIN_EFECTIVO = "Solo ADMIN puede registrar pagos en efectivo";
	public static final String MENSAJE_IMPORTE_EFECTIVO_SUPERA_PENDIENTE =
			"El importe en efectivo no puede superar el pendiente del jugador";
	public static final String MENSAJE_ACCESO_DENEGADO_JUGADOR = "No autorizado para acceder a este jugador";
	public static final String MENSAJE_ACCESO_DENEGADO_PAGOS = "No autorizado para acceder a los pagos de este jugador";
	public static final String MENSAJE_ACCESO_DENEGADO_EQUIPACION = "No autorizado para acceder a la equipación de este jugador";

	public static final String CODIGO_ERROR_INSCRIPCION_INVALIDA = "INSCRIPCION_INVALIDA";
	public static final String CODIGO_ERROR_REGLA_NEGOCIO = "REGLA_NEGOCIO";
	public static final String CODIGO_ERROR_RECURSO_NO_ENCONTRADO = "RECURSO_NO_ENCONTRADO";
	public static final String CODIGO_ERROR_CREDENCIALES_INVALIDAS = "CREDENCIALES_INVALIDAS";
	public static final String CODIGO_ERROR_VALIDACION = "VALIDACION";
	public static final String CODIGO_ERROR_ACCESS_DENIED = "ACCESS_DENIED";
	public static final String JSON_ERROR_TEMPLATE = "{\"error\": \"%s\"}";

	public static final String CATEGORIA_JUVENIL = "Juvenil";
	public static final String CATEGORIA_CADETE = "Cadete";
	public static final String CATEGORIA_INFANTIL = "Infantil";
	public static final String CATEGORIA_ALEVIN = "Alevin";
	public static final String CATEGORIA_BENJAMIN = "Benjamin";
	public static final String CATEGORIA_PREBENJAMIN = "Prebenjamin";
	public static final String CATEGORIA_DEBUTANTE = "Debutante";

	public static final BigDecimal IMPORTE_EQUIPACION = BigDecimal.valueOf(160);
	public static final BigDecimal CUOTA_CATEGORIA_BAJA = BigDecimal.valueOf(280.00);
	public static final BigDecimal CUOTA_CATEGORIA_ALTA = BigDecimal.valueOf(320.00);

	public static final String PREFIJO_CONCEPTO_EQUIPACION = "EQUIP-";
	public static final String PREFIJO_CONCEPTO_CUOTA_1 = "CUOTA1-";
	public static final String PREFIJO_CONCEPTO_CUOTA = "CUOTA";
	public static final String PREFIJO_CONCEPTO_EQUIPACION_RESUMEN = "EQUIP";
	public static final String ROL_TUTOR = "TUTOR";
	public static final String CAMPO_JUGADOR_ID = "jugadorId";
	public static final String AUTHORITY_ADMIN = "ADMIN";
	public static final String PREAUTHORIZE_ADMIN = "hasAuthority('ADMIN')";

	public static final String TABLA_JUGADORES = "jugadores";
	public static final String TABLA_TUTORES = "tutores";
	public static final String TABLA_PAGOS = "pagos";
	public static final String TABLA_EQUIPACIONES = "equipaciones";
	public static final String COLUMNA_TUTOR_ID = "tutor_id";
	public static final String COLUMNA_JUGADOR_ID = "jugador_id";
	public static final String MAPPED_BY_TUTOR = "tutor";
	public static final String MAPPED_BY_JUGADOR = "jugador";

	public static final String PARAM_EMAIL = "email";
	public static final String PARAM_ESTADO = "estado";
	public static final String PARAM_CATEGORIA = "categoria";

	public static final String JPQL_JUGADOR_TUTOR_EMAIL_CON_PAGOS =
			"SELECT j FROM Jugador j LEFT JOIN FETCH j.pagos WHERE j.tutor.email = :email";
	public static final String JPQL_JUGADOR_ALL_CON_PAGOS =
			"SELECT j FROM Jugador j LEFT JOIN FETCH j.pagos";
	public static final String JPQL_JUGADOR_CATEGORIA_CON_PAGOS =
			"SELECT j FROM Jugador j LEFT JOIN FETCH j.pagos WHERE j.categoria = :categoria";
	public static final String JPQL_PAGO_ESTADO_CON_JUGADOR =
			"SELECT p FROM Pago p JOIN FETCH p.jugador WHERE p.estado = :estado";
	public static final String RUTA_ID = "/{id}";

	public static final String RUTA_API_AUTH = "/api/auth";
	public static final String RUTA_API_JUGADORES = "/api/jugadores";
	public static final String RUTA_API_PAGOS = "/api/pagos";
	public static final String RUTA_API_EQUIPACIONES = "/api/equipaciones";
	public static final String RUTA_API_AUTH_ALL = RUTA_API_AUTH + "/**";
	public static final String RUTA_API_JUGADORES_ID = RUTA_API_JUGADORES + RUTA_ID;
	public static final String RUTA_API_JUGADORES_CATEGORIA_ALL = RUTA_API_JUGADORES + "/categoria/**";
	public static final String RUTA_API_JUGADORES_ALL = RUTA_API_JUGADORES + "/**";
	public static final String RUTA_API_PAGOS_JUGADOR_ALL = RUTA_API_PAGOS + "/jugador/**";
	public static final String RUTA_API_PAGOS_PENDIENTE_ALL = RUTA_API_PAGOS + "/pendiente/**";
	public static final String RUTA_API_PAGOS_TOTAL_ALL = RUTA_API_PAGOS + "/total/**";
	public static final String RUTA_API_PAGOS_ALL = RUTA_API_PAGOS + "/**";
	public static final String RUTA_API_PAGOS_CONFIRMAR = RUTA_API_PAGOS + "/*/confirmar";
	public static final String RUTA_API_PAGOS_RECHAZAR = RUTA_API_PAGOS + "/*/rechazar";
	public static final String RUTA_API_EQUIPACIONES_JUGADOR_ALL = RUTA_API_EQUIPACIONES + "/jugador/**";
	public static final String RUTA_API_EQUIPACIONES_TIENE_ALL = RUTA_API_EQUIPACIONES + "/tiene/**";
	public static final String RUTA_API_EQUIPACIONES_ALL = RUTA_API_EQUIPACIONES + "/**";
	public static final String RUTA_PAGOS_JUGADOR_ID = "jugador/{id}";
	public static final String RUTA_PAGOS_PENDIENTE_ID = "pendiente/{id}";
	public static final String RUTA_PAGOS_TOTAL_ID = "total/{id}";
	public static final String RUTA_PAGOS_EFECTIVO = "/efectivo";
	public static final String RUTA_PAGOS_CONFIRMAR = "/{id}/confirmar";
	public static final String RUTA_PAGOS_RECHAZAR = "/{id}/rechazar";
	public static final String RUTA_PAGOS_PENDIENTES = "/pendientes";
	public static final String RUTA_PAGOS_ELEGIR_CUOTAS = "/elegir-cuotas";
	public static final String RUTA_PAGOS_SIGUIENTE_CUOTA = "/siguiente-cuota";
	public static final String RUTA_JUGADORES_TUTOR_ID = "/tutor/{id}";
	public static final String RUTA_JUGADORES_TUTOR_ME = "/tutor/me";
	public static final String RUTA_JUGADORES_CATEGORIA = "/categoria/{categoria}";
	public static final String RUTA_JUGADORES_ADMIN_ALL = "/admin/all";
	public static final String RUTA_JUGADORES_ADMIN_CATEGORIA = "/admin/categoria/{categoria}";
	public static final String RUTA_EQUIPACIONES_JUGADOR_ID = "/jugador/{id}";
	public static final String RUTA_EQUIPACIONES_TIENE = "/tiene/{jugadorId}/{temporada}";
	public static final String RUTA_AUTH_REGISTRO = "/registro";
	public static final String RUTA_AUTH_LOGIN = "/login";
	public static final String RUTA_AUTH_LOGOUT = "/logout";
	public static final String RUTA_LOGIN = "/login";
	public static final String RUTA_REGISTRO = "/registro";
	public static final String RUTA_INSCRIPCION = "/inscripcion";
	public static final String RUTA_PAGO = "/pago";
	public static final String RUTA_ROOT = "/";
	public static final String RUTA_TUTOR = "/tutor";
	public static final String RUTA_ADMIN = "/admin";
	public static final String RUTA_PANEL = "/panel";
	public static final String RUTA_TRABAJA = "/trabaja";
	public static final String RUTA_INSCRIPCIONES = "/inscripciones";
	public static final String RUTA_EQUIPO = "/equipo";
	public static final String RUTA_ENTRENADORES = "/entrenadores";
	public static final String RUTA_CONTACTO = "/contacto";
	public static final String RUTA_TUTOR_ALL = "/tutor/**";
	public static final String RUTA_ADMIN_ALL = "/admin/**";
	public static final String RUTA_CSS_ALL = "/css/**";
	public static final String RUTA_JS_ALL = "/js/**";
	public static final String RUTA_IMAGES_ALL = "/images/**";
	public static final String RUTA_FAVICON = "/favicon.ico";

	public static final String VISTA_INDEX = "index";
	public static final String VISTA_TRABAJA = "trabaja";
	public static final String VISTA_AUTH_LOGIN = "auth/login";
	public static final String VISTA_AUTH_REGISTRO = "auth/registro";
	public static final String VISTA_INSCRIPCION = "inscripcion";
	public static final String VISTA_PAGO = "pago";
	public static final String VISTA_ADMIN_PANEL = "admin/panel";
	public static final String VISTA_TUTOR_PANEL = "tutor/panel";


	public static final int NUMERO_CUOTAS_POR_DEFECTO = 1;
}
