/**
 * Flujo de inscripcion de jugador desde el panel del tutor.
 * @module inscripcion
 */
checkAuth();

/**
 * @typedef {Object} PagoResumen
 * @property {string} estado
 * @property {string} [concepto]
 * @property {number|string} [importe]
 */

const APP_CFG = window.AppConstants || {};
const API = APP_CFG.api || {};
const INSCRIPCION_ROUTES = APP_CFG.routes || {};
const MSG = APP_CFG.mensajes || {};
const categorias = APP_CFG.categorias || {};
const cuotas = APP_CFG.cuotas || {};
const INSCRIPCION_UI = APP_CFG.ui || {};
const INSCRIPCION_STORAGE = APP_CFG.storage || {};
const PAGO = APP_CFG.pago || {};

let categoriaCalculada = '';
let cuotaCalculada = 0;
let necesitaEquipacionActual = false;
let numeroCuotasSeleccionado = PAGO.cuotasDefault;

/**
 * Valida datos del paso 1, calcula categoria/cuota y muestra resumen.
 * @returns {void}
 */
function calcularCategoria(event) {
    if (event) event.preventDefault();
    const nombre = document.getElementById('nombre').value;
    const apellidos = document.getElementById('apellidos').value;
    const fechaNacimiento = document.getElementById('fechaNacimiento').value;
    necesitaEquipacionActual = document.getElementById('necesitaEquipacion').value === 'true';

    if (!nombre || !apellidos || !fechaNacimiento) {
        const errEl = document.getElementById('error-msg');
        errEl.classList.remove('is-hidden');
        errEl.textContent = MSG.errorCamposObligatorios;
        return;
    }

    document.getElementById('error-msg').classList.add('is-hidden');

    const anio = new Date(fechaNacimiento).getFullYear();
    categoriaCalculada = categorias[anio];
    if (!categoriaCalculada) {
        const errEl = document.getElementById('error-msg');
        errEl.classList.remove('is-hidden');
        errEl.textContent = MSG.errorAnioNoPermitido;
        return;
    }

    cuotaCalculada = cuotas[categoriaCalculada] || 0;

    // Rellenar resumen
    document.getElementById('resumen-nombre').textContent = nombre + ' ' + apellidos;
    document.getElementById('resumen-fecha').textContent = fechaNacimiento;
    document.getElementById('resumen-categoria').textContent = categoriaCalculada;
    document.getElementById('resumen-cuota').textContent = cuotaCalculada + INSCRIPCION_UI.euro;

    // Equipación
    document.getElementById('resumen-equipacion-row').style.display = necesitaEquipacionActual ? 'flex' : 'none';

    // Fraccionamiento: solo si NO necesita equipación
    const seccionCuotas = document.getElementById('seccion-cuotas');
    if (!necesitaEquipacionActual) {
        seccionCuotas.classList.remove('is-hidden');
        actualizarResumenCuotas();
    } else {
        seccionCuotas.classList.add('is-hidden');
        document.getElementById('resumen-total').textContent =
            PAGO.importeEquipacion + INSCRIPCION_UI.euro + ' (' + MSG.resumenEquipacionDetalle + ') + '
            + cuotaCalculada + INSCRIPCION_UI.euro + ' (' + MSG.resumenCuotaDetalle + ')';
    }

    document.getElementById('paso1').classList.add('is-hidden');
    document.getElementById('paso2').classList.remove('is-hidden');
}

/**
 * Selecciona numero de cuotas y actualiza estado visual del selector.
 * @param {number} n Numero de cuotas elegido.
 * @param {HTMLElement} btn Boton pulsado.
 * @returns {void}
 */
function seleccionarCuotas(n, btn) {
    numeroCuotasSeleccionado = n;
    document.querySelectorAll('.cuota-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    actualizarResumenCuotas();
}

/**
 * Recalcula el texto de resumen para la modalidad de cuotas activa.
 * @returns {void}
 */
function actualizarResumenCuotas() {
    const importePrimeraCuota = (cuotaCalculada / numeroCuotasSeleccionado).toFixed(2);
    document.getElementById('resumen-total').textContent = numeroCuotasSeleccionado === PAGO.cuotasDefault
        ? cuotaCalculada + INSCRIPCION_UI.euro + ' (' + MSG.resumenPagoUnico + ')'
        : importePrimeraCuota + INSCRIPCION_UI.euro + ' ' + MSG.resumenTransferenciaAhora + ' · '
            + numeroCuotasSeleccionado + ' ' + MSG.resumenCuotasDe + ' ' + importePrimeraCuota + INSCRIPCION_UI.euro;
    document.getElementById('resumen-primera-cuota').textContent = importePrimeraCuota + INSCRIPCION_UI.euro;
}

/**
 * Vuelve del paso de resumen al formulario inicial.
 * @returns {void}
 */
function volver(event) {
    if (event) event.preventDefault();
    document.getElementById('paso1').classList.remove('is-hidden');
    document.getElementById('paso2').classList.add('is-hidden');
}

/**
 * Crea la inscripcion del jugador y prepara datos para la pantalla de pago.
 * @returns {Promise<void>} Promesa resuelta cuando termina el flujo.
 */
async function inscribir(event) {
    if (event) event.preventDefault();
    const nombre = document.getElementById('nombre').value;
    const apellidos = document.getElementById('apellidos').value;

    const body = {
        nombre: nombre,
        apellidos: apellidos,
        fechaNacimiento: document.getElementById('fechaNacimiento').value,
        necesitaEquipacion: necesitaEquipacionActual,
        numeroCuotas: necesitaEquipacionActual ? PAGO.cuotasDefault : numeroCuotasSeleccionado,
        tutorId: null
    };
    if (!document.getElementById('aceptaPrivacidad').checked) {
        const errEl = document.getElementById('error-msg');
        errEl.classList.remove('is-hidden');
        errEl.textContent = 'Debes aceptar la política de privacidad para continuar';
        return;
    }

    const response = await fetchConAuth(API.jugadores, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });

    if (!response) return;

    if (response.ok) {
        const jugador = await response.json();

        // Obtener el pago pendiente para el concepto e importe
        const pagosRes = await fetchConAuth(API.pagosJugador + jugador.id);
        let concepto = (`CUOTA1-${nombre} ${apellidos}-${categoriaCalculada}`).trim().toUpperCase();
        let importe = necesitaEquipacionActual ? PAGO.importeEquipacion : (cuotaCalculada / numeroCuotasSeleccionado).toFixed(2);

        if (pagosRes && pagosRes.ok) {
            /** @type {PagoResumen[]} */
            const pagos = await pagosRes.json();
            const pagoPendiente = pagos.find(p => p.estado === PAGO.estadoPendiente);
            if (pagoPendiente?.concepto) concepto = pagoPendiente.concepto;
            if (pagoPendiente?.importe) importe = pagoPendiente.importe;
        }

        // Guardar en sessionStorage para la página de pago
        sessionStorage.setItem(INSCRIPCION_STORAGE.pagoNombre, nombre + ' ' + apellidos);
        sessionStorage.setItem(INSCRIPCION_STORAGE.pagoConcepto, concepto);
        sessionStorage.setItem(INSCRIPCION_STORAGE.pagoImporte, importe);

        window.location.href = INSCRIPCION_ROUTES.pago;

    } else {
        let errorMsg = MSG.errorInscripcionGenerico;

        try {
            const errorBody = await response.json();
            if (errorBody && errorBody.message) {
                errorMsg = errorBody.message;
            } else if (errorBody && errorBody.error) {
                errorMsg = errorBody.error;
            }
        } catch {
            // Si no hay JSON, se mantiene el mensaje por defecto.
        }

        const errEl = document.getElementById('error-msg');
        errEl.classList.remove('is-hidden');
        errEl.textContent = errorMsg;
    }
}

/**
 * Registra eventos del formulario y acciones de cuotas.
 * @returns {void}
 */
function initInscripcionEvents() {
    const continuarBtn = document.querySelector('[data-action="inscripcion-continuar"]');
    if (continuarBtn) {
        continuarBtn.addEventListener('click', calcularCategoria);
    }

    const confirmarBtn = document.querySelector('[data-action="inscripcion-confirmar"]');
    if (confirmarBtn) {
        confirmarBtn.addEventListener('click', inscribir);
    }

    const volverBtn = document.querySelector('[data-action="inscripcion-volver"]');
    if (volverBtn) {
        volverBtn.addEventListener('click', volver);
    }

    document.querySelectorAll('[data-action="seleccionar-cuotas"]').forEach((btn) => {
        btn.addEventListener('click', (event) => {
            if (event) event.preventDefault();
            seleccionarCuotas(Number(btn.dataset.cuotas ?? PAGO.cuotasDefault), btn);
        });
    });
}

document.addEventListener('DOMContentLoaded', initInscripcionEvents);
