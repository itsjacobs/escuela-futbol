/**
 * Configuracion global compartida por scripts frontend.
 * @module core/constants
 */

/**
 * Objeto de constantes global expuesto en window.
 * @type {Record<string, unknown>}
 */
window.AppConstants = {
    storage: {
        token: 'token',
        rol: 'rol',
        nombre: 'nombre',
        pagoNombre: 'pago_nombre',
        pagoConcepto: 'pago_concepto',
        pagoImporte: 'pago_importe'
    },
    api: {
        authLogin: '/api/auth/login',
        authRegistro: '/api/auth/registro',
        jugadores: '/api/jugadores',
        jugadoresTutorMe: '/api/jugadores/tutor/me',
        jugadoresAdminAll: '/api/jugadores/admin/all',
        jugadoresAdminCategoria: '/api/jugadores/admin/categoria/',
        pagosJugador: '/api/pagos/jugador/',
        pagosPendientes: '/api/pagos/pendientes',
        pagosElegirCuotas: '/api/pagos/elegir-cuotas',
        pagosSiguienteCuota: '/api/pagos/siguiente-cuota',
        pagosEfectivo: '/api/pagos/efectivo',
        pagosConfirmar: '/api/pagos/',
        pagosRechazarSuffix: '/rechazar',
        pagosConfirmarSuffix: '/confirmar'
    },
    routes: {
        home: '/',
        login: '/login',
        registro: '/registro',
        inscripcion: '/inscripcion',
        pago: '/pago',
        adminPanel: '/admin/panel',
        tutorPanel: '/tutor/panel'
    },
    auth: {
        bearerPrefix: 'Bearer ',
        adminRole: 'ADMIN',
        adminRolePrefixed: 'ROLE_ADMIN'
    },
    http: {
        unauthorized: 401
    },
    ui: {
        userPrefix: '👤 ',
        successPrefix: '✅ ',
        dash: '—',
        euro: '€',
        cuotaUnicaLabel: 'pago unico',
        transferenciaAhoraLabel: 'ahora',
        cuotasDeLabel: 'cuotas de',
        carouselTotalSlides: 3,
        carouselIntervalMs: 5000
    },
    pago: {
        iban: 'ES4221000579611300175282',
        conceptoCuotaPrefix: 'CUOTA',
        estadoPendiente: 'PENDIENTE',
        estadoConfirmado: 'CONFIRMADO',
        cuotasDefault: 1,
        metodoEfectivo: 'EFECTIVO',
        importeEquipacion: 160
    },
    mensajes: {
        errorCamposObligatorios: 'Rellena todos los campos',
        errorLogin: 'Email o contrasena incorrectos',
        registroExito: 'Cuenta creada correctamente. Redirigiendo...',
        registroDuplicado: 'Ya existe una cuenta con ese email',
        registroError: 'Error al crear la cuenta',
        copiaIbanOk: 'IBAN copiado al portapapeles',
        copiaConceptoOk: 'Concepto copiado al portapapeles',
        errorSiguientePago: 'Error al generar el siguiente pago',
        errorGenerarPago: 'Error al generar el pago. Intentalo de nuevo.',
        errorConfirmarPago: 'Error al confirmar el pago',
        errorRechazarPago: 'Error al rechazar el pago',
        errorRegistrarPago: 'Error al registrar el pago',
        errorEliminarJugador: 'Error al eliminar el jugador',
        errorInscripcionGenerico: 'Error al inscribir al jugador. Intentalo de nuevo.',
        errorAnioNoPermitido: 'Ese ano no se puede seleccionar para inscripcion',
        noJugadoresInscritos: 'No tienes jugadores inscritos aun.',
        inscribirJugador: 'Inscribir jugador',
        inscribir: 'Inscribir',
        otroHijoPregunta: 'Tienes otro hijo/a que quiera unirse?',
        labelCuotaTemporada: 'Cuota temporada',
        labelTotalPagado: 'Total pagado',
        labelPendiente: 'Pendiente',
        cuotaAlDia: 'Cuota al dia',
        transferenciaEnviadaPendiente: 'Transferencia enviada - esperando confirmacion del club',
        verDatosTransferencia: 'Ver datos de la transferencia',
        elegirFormaPago: 'Elegir forma de pago',
        pagarSiguienteCuota: 'Pagar siguiente cuota',
        confirmarRechazarPagoJugador: 'Rechazar el pago de {nombre}? Se eliminara al jugador.',
        importeMayorCero: 'El importe debe ser mayor que 0',
        noPagosPendientes: 'No hay pagos pendientes de confirmar.',
        badgeSinConfirmar: 'Sin confirmar',
        accionEfectivo: 'Efectivo',
        accionBorrar: 'Borrar',
        accionConfirmar: 'Confirmar',
        accionRechazar: 'Rechazar',
        textoPendienteJugador: 'Pendiente',
        conceptoEfectivoDefault: 'Pago en efectivo',
        eliminarJugadorConfirmacion: 'Seguro que quieres eliminar a {nombre}?',
        resumenEquipacionDetalle: 'equipacion',
        resumenCuotaDetalle: 'cuota',
        resumenCuotasPregunta: 'En cuantas cuotas quieres pagar?',
        resumenPrimeraTransferencia: 'Primera transferencia',
        resumenPagoUnico: 'pago unico',
        resumenTransferenciaAhora: 'ahora',
        resumenCuotasDe: 'cuotas de'
    },
    categorias: {
        2008: 'Juvenil', 2009: 'Juvenil', 2010: 'Juvenil',
        2011: 'Cadete', 2012: 'Cadete',
        2013: 'Infantil', 2014: 'Infantil',
        2015: 'Alevin', 2016: 'Alevin',
        2017: 'Benjamin', 2018: 'Benjamin',
        2019: 'Prebenjamin', 2020: 'Prebenjamin',
        2021: 'Debutante', 2022: 'Debutante'
    },
    cuotas: {
        Debutante: 280,
        Prebenjamin: 280,
        Benjamin: 280,
        Alevin: 280,
        Infantil: 320,
        Cadete: 320,
        Juvenil: 320
    }
};

