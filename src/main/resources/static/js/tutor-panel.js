/**
 * Panel del tutor para seguimiento de jugadores y pagos por transferencia.
 * @module tutor-panel
 */
checkAuth();
const APP_CFG = window.AppConstants || {};
const API = APP_CFG.api || {};
const TUTOR_ROUTES = APP_CFG.routes || {};
const MSG = APP_CFG.mensajes || {};
const PAGO = APP_CFG.pago || {};
const CUOTAS = APP_CFG.cuotas || {};
const TUTOR_UI = APP_CFG.ui || {};

document.getElementById('nombre-tutor').textContent = TUTOR_UI.userPrefix + getNombre();

let jugadorIdCuotas = null;
let cuotaTotalJugador = 0;
let numeroCuotasModal = PAGO.cuotasDefault;

/**
 * Escapa caracteres especiales para evitar inyeccion HTML en plantillas.
 * @param {unknown} value Valor original.
 * @returns {string} Texto seguro para interpolar en HTML.
 */
function escapeHtml(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

/**
 * @typedef {Object} JugadorTutorRow
 * @property {number} id
 * @property {string} nombre
 * @property {string} apellidos
 * @property {string} categoria
 * @property {number} cuotaTemporada
 * @property {number} totalPagado
 * @property {number} pendiente
 * @property {boolean} necesitaEquipacion
 * @property {boolean} equipacionConfirmada
 * @property {boolean} tieneCuotaPendiente
 * @property {number | null | undefined} numeroCuotas
 */

/**
 * @typedef {Object} PagoTutorRow
 * @property {string} estado
 * @property {string} concepto
 * @property {number} importe
 */

/**
 * Carga y renderiza los jugadores asociados al tutor autenticado.
 * @returns {Promise<void>} Promesa resuelta al finalizar el render.
 */
async function cargarJugadores() {
    const response = await fetchConAuth(API.jugadoresTutorMe);
    if (!response) return;

    const container = document.getElementById('jugadores-container');
    if (!container) {
        return;
    }

    /** @type {JugadorTutorRow[] | unknown} */
    let payload = [];
    if (response.ok) {
        try {
            payload = await response.json();
        } catch {
            payload = [];
        }
    }

    const jugadores = Array.isArray(payload) ? payload : [];
    container.innerHTML = '';

    if (jugadores.length > 0) {
        jugadores.forEach(j => {
            const pendienteClass = j.pendiente > 0 ? 'pendiente' : 'pagado';
            const botonAccion = generarBotonAccion(j);
            const nombreCompleto = `${j.nombre} ${j.apellidos}`;

            container.innerHTML += `
                <div class="jugador-card">
                    <h2>${escapeHtml(nombreCompleto)}</h2>
                    <span class="categoria-badge">${escapeHtml(j.categoria)}</span>
                    <div class="pago-info">
                        <div class="pago-row">
                            <span>${MSG.labelCuotaTemporada}</span>
                            <span>${j.cuotaTemporada}${TUTOR_UI.euro}</span>
                        </div>
                        <div class="pago-row">
                            <span>${MSG.labelTotalPagado}</span>
                            <span class="pagado">${j.totalPagado}${TUTOR_UI.euro}</span>
                        </div>
                        <div class="pago-row">
                            <span>${MSG.labelPendiente}</span>
                            <span class="${pendienteClass}">${j.pendiente}${TUTOR_UI.euro}</span>
                        </div>
                    </div>
                    ${botonAccion}
                </div>
            `;
        });
    } else {
        container.innerHTML += `
            <div class="no-data">
                <p>${MSG.noJugadoresActivosAun || MSG.noJugadoresInscritos}</p>
                <p>${MSG.inscripcionPendienteRevision || ''}</p>
            </div>
        `;
    }

    container.innerHTML += `
        <div class="jugador-card nueva-inscripcion">
            <h2>➕ ${MSG.inscribirJugador}</h2>
            <p>${MSG.otroHijoPregunta}</p>
            <a href="${TUTOR_ROUTES.inscripcion}" class="btn-primary inline-block mt-15">${MSG.inscribir}</a>
        </div>
    `;
}


/**
 * Genera el bloque HTML de accion segun estado de pago del jugador.
 * @param {JugadorTutorRow} j Datos del jugador.
 * @returns {string} HTML del boton o estado correspondiente.
 */
function generarBotonAccion(j) {
    const nombreCompleto = `${j.nombre} ${j.apellidos}`;

    // Cuota al día
    if (j.pendiente <= 0) {
        return '<p class="text-success text-center text-strong mt-15">✅ ' + MSG.cuotaAlDia + '</p>';
    }

    // Tiene transferencia enviada esperando confirmación del admin
    if (j.tieneCuotaPendiente) {
        return `
            <div class="estado-pago-pendiente">
                ⏳ ${MSG.transferenciaEnviadaPendiente}
            </div>
            <button class="btn-secondary btn-block mt-10"
                data-action="ver-datos-pago"
                data-jugador-id="${j.id}"
                data-jugador-nombre="${escapeHtml(nombreCompleto)}"
                data-numero-cuotas="${j.numeroCuotas || PAGO.cuotasDefault}">
                ${MSG.verDatosTransferencia}
            </button>`;
    }

    // Necesita elegir cuotas:
    // - Tiene equipación y ya la confirmaron, pero no ha elegido cómo pagar la cuota
    // - No tiene equipación y es la primera vez que va a pagar (numeroCuotas aún es 1 por defecto)
    const necesitaElegirCuotas =
        (j.necesitaEquipacion && j.equipacionConfirmada && j.pendiente > 0 && (j.numeroCuotas == null || j.numeroCuotas <= 1))
        || (!j.necesitaEquipacion && j.pendiente > 0 && (j.numeroCuotas == null || j.numeroCuotas <= 1));

    if (necesitaElegirCuotas) {
        return `
            <button class="btn-primary btn-block mt-15"
                data-action="abrir-modal-cuotas"
                data-jugador-id="${j.id}"
                data-jugador-nombre="${escapeHtml(nombreCompleto)}"
                data-cuota-pendiente="${j.pendiente}">
                📅 ${MSG.elegirFormaPago}
            </button>`;
    }

    // Ya eligió cuotas, puede pagar la siguiente
    return `
        <button class="btn-primary btn-block mt-15"
            data-action="ver-datos-pago"
            data-jugador-id="${j.id}"
            data-jugador-nombre="${escapeHtml(nombreCompleto)}"
            data-numero-cuotas="${j.numeroCuotas || PAGO.cuotasDefault}">
            🏦 ${MSG.pagarSiguienteCuota}
        </button>`;
}

// ── MODAL ELEGIR CUOTAS ────────────────────────────────────
/**
 * Abre modal para seleccionar numero de cuotas.
 * @param {number} jugadorId Identificador del jugador.
 * @param {string} nombre Nombre del jugador.
 * @param {number} cuotaPendiente Importe total pendiente.
 * @returns {void}
 */
function abrirModalCuotas(jugadorId, nombre, cuotaPendiente) {
    jugadorIdCuotas = jugadorId;
    cuotaTotalJugador = Number(cuotaPendiente);
    numeroCuotasModal = PAGO.cuotasDefault;

    document.getElementById('modal-cuotas-nombre').textContent = nombre;
    document.getElementById('modal-cuotas-total').textContent = cuotaTotalJugador + TUTOR_UI.euro;

    actualizarOpcionesCuotas(cuotaTotalJugador);

    document.querySelectorAll('.cuota-btn').forEach((b, i) => {
        b.classList.toggle('active', i === 0);
    });

    actualizarImporteCuota();
    document.getElementById('modal-cuotas-error').style.display = 'none';
    document.getElementById('modal-cuotas').style.display = 'flex';
}

/**
 * Marca la opcion de cuotas elegida dentro del modal.
 * @param {number} n Numero de cuotas.
 * @param {HTMLElement} btn Boton seleccionado.
 * @returns {void}
 */
function seleccionarCuotasModal(n, btn) {
    numeroCuotasModal = n;
    document.querySelectorAll('.cuota-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    actualizarImporteCuota();
}

/**
 * Recalcula el importe por cuota en el modal.
 * @returns {void}
 */
function actualizarImporteCuota() {
    const importe = (cuotaTotalJugador / numeroCuotasModal).toFixed(2);
    document.getElementById('modal-importe-cuota').textContent = importe + TUTOR_UI.euro;
}

/**
 * Habilita o deshabilita opcion de 3 cuotas segun importe pendiente.
 * @param {number} cuotaPendiente Importe pendiente del jugador.
 * @returns {void}
 */
function actualizarOpcionesCuotas(cuotaPendiente) {
    const botonTresCuotas = document.querySelector('[data-cuotas="3"]');
    if (!botonTresCuotas) {
        return;
    }

    const cuotasConTresPagos = [CUOTAS.Debutante, CUOTAS.Prebenjamin, CUOTAS.Benjamin, CUOTAS.Alevin, CUOTAS.Infantil, CUOTAS.Cadete, CUOTAS.Juvenil]
        .filter((v, i, a) => a.indexOf(v) === i);
    const permiteTresCuotas = cuotasConTresPagos.some(v => Math.abs(cuotaPendiente - v) < 0.01);
    botonTresCuotas.disabled = !permiteTresCuotas;

    if (!permiteTresCuotas) {
        botonTresCuotas.classList.remove('active');
        numeroCuotasModal = Math.min(numeroCuotasModal, 2);
        const botonPorDefecto = document.querySelector(`[data-cuotas="${numeroCuotasModal}"]`);
        if (botonPorDefecto) {
            botonPorDefecto.classList.add('active');
        }
    }
}

/**
 * Confirma seleccion de cuotas y abre modal con datos de transferencia.
 * @returns {Promise<void>} Promesa resuelta al finalizar.
 */
async function confirmarElegirCuotas() {
    const response = await fetchConAuth(API.pagosElegirCuotas, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            jugadorId: jugadorIdCuotas,
            numeroCuotas: numeroCuotasModal
        })
    });

    if (!response || !response.ok) {
        document.getElementById('modal-cuotas-error').style.display = 'block';
        document.getElementById('modal-cuotas-error').textContent = MSG.errorGenerarPago;
        return;
    }

    const pago = await response.json();
    const nombre = document.getElementById('modal-cuotas-nombre').textContent;
    cerrarModalCuotas();
    abrirModalTransferenciaConDatos(nombre, pago.importe, pago.concepto);
    await cargarJugadores();
}

/**
 * Cierra el modal de seleccion de cuotas.
 * @returns {void}
 */
function cerrarModalCuotas() {
    document.getElementById('modal-cuotas').style.display = 'none';
    jugadorIdCuotas = null;
}

// ── VER DATOS DE PAGO ──────────────────────────────────────
/**
 * Obtiene o genera el siguiente pago pendiente y muestra datos de transferencia.
 * @param {number} jugadorId Identificador del jugador.
 * @param {string} nombre Nombre del jugador.
 * @param {number} numeroCuotas Numero total de cuotas pactadas.
 * @returns {Promise<void>} Promesa resuelta tras completar el flujo.
 */
async function verDatosPago(jugadorId, nombre, numeroCuotas) {
    const response = await fetchConAuth(API.pagosJugador + jugadorId);
    if (!response) return;

    /** @type {PagoTutorRow[]} */
    const pagos = await response.json();

    // Si ya hay una cuota pendiente de confirmar, mostrar sus datos directamente
    const pagoPendiente = pagos.find(p =>
        p.estado === PAGO.estadoPendiente && p.concepto?.startsWith(PAGO.conceptoCuotaPrefix)
    );

    if (pagoPendiente) {
        abrirModalTransferenciaConDatos(nombre, pagoPendiente.importe, pagoPendiente.concepto);
        return;
    }

    // No hay pendiente — calcular cuántas cuotas confirmadas hay
    const cuotasConfirmadas = pagos.filter(p =>
        p.estado === PAGO.estadoConfirmado && p.concepto?.startsWith(PAGO.conceptoCuotaPrefix)
    ).length;

    // Si quedan cuotas por pagar, generar la siguiente
    if (cuotasConfirmadas < numeroCuotas) {
        const res = await fetchConAuth(API.pagosSiguienteCuota, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ jugadorId })
        });
        if (!res || !res.ok) {
            alert(MSG.errorSiguientePago);
            return;
        }
        const nuevoPago = await res.json();
        abrirModalTransferenciaConDatos(nombre, nuevoPago.importe, nuevoPago.concepto);
        await cargarJugadores();
    }
}

// ── MODAL TRANSFERENCIA ────────────────────────────────────
/**
 * Abre modal de transferencia con los datos del pago actual.
 * @param {string} nombre Nombre del jugador.
 * @param {number|string} importe Importe a transferir.
 * @param {string} concepto Concepto bancario del pago.
 * @returns {void}
 */
function abrirModalTransferenciaConDatos(nombre, importe, concepto) {
    document.getElementById('modal-jugador-nombre').textContent = nombre;
    document.getElementById('modal-importe-display').textContent = importe + TUTOR_UI.euro;
    document.getElementById('modal-concepto').textContent = String(concepto || '').toUpperCase();
    document.getElementById('modal-transferencia').style.display = 'flex';
}

/**
 * Cierra modal de transferencia.
 * @returns {void}
 */
function cerrarModalTransferencia() {
    document.getElementById('modal-transferencia').style.display = 'none';
}

/**
 * Copia el IBAN al portapapeles.
 * @returns {void}
 */
function copiarIban() {
    navigator.clipboard.writeText(PAGO.iban).then(() => {
        alert(TUTOR_UI.successPrefix + MSG.copiaIbanOk);
    });
}

/**
 * Copia el concepto del pago al portapapeles.
 * @returns {void}
 */
function copiarConcepto() {
    const concepto = document.getElementById('modal-concepto').textContent;
    navigator.clipboard.writeText(concepto).then(() => {
        alert(TUTOR_UI.successPrefix + MSG.copiaConceptoOk);
    });
}

/**
 * Registra listeners de acciones del panel del tutor.
 * @returns {void}
 */
function bindTutorPanelEvents() {
    document.addEventListener('click', async (event) => {
        const target = event.target.closest('[data-action]');
        if (!target) return;

        const action = target.dataset.action;

        if (action === 'abrir-modal-cuotas') {
            abrirModalCuotas(
                Number(target.dataset.jugadorId),
                target.dataset.jugadorNombre,
                Number(target.dataset.cuotaPendiente)
            );
            return;
        }

        if (action === 'ver-datos-pago') {
            await verDatosPago(
                Number(target.dataset.jugadorId),
                target.dataset.jugadorNombre,
                Number(target.dataset.numeroCuotas)
            );
            return;
        }

        if (action === 'seleccionar-cuotas-modal') {
            seleccionarCuotasModal(Number(target.dataset.cuotas), target);
            return;
        }

        if (action === 'confirmar-elegir-cuotas') {
            await confirmarElegirCuotas();
            return;
        }

        if (action === 'cerrar-modal-cuotas') {
            cerrarModalCuotas();
            return;
        }

        if (action === 'copiar-iban') {
            copiarIban();
            return;
        }

        if (action === 'copiar-concepto') {
            copiarConcepto();
            return;
        }

        if (action === 'cerrar-modal-transferencia') {
            cerrarModalTransferencia();
        }
    });
}

/**
 * Inicializa el panel del tutor cargando datos y eventos.
 * @returns {Promise<void>} Promesa resuelta tras la carga inicial.
 */
async function initTutorPanel() {
    bindTutorPanelEvents();
    await cargarJugadores();
}

void initTutorPanel();
