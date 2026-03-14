/**
 * Script del panel de administracion para gestion de jugadores y pagos.
 * @module admin-panel
 */
checkAdmin();

/**
 * @typedef {Object} JugadorAdminRow
 * @property {number} id
 * @property {string} nombre
 * @property {string} apellidos
 * @property {string} categoria
 * @property {string} fechaNacimiento
 * @property {string} tutorNombre
 * @property {string} tutorApellidos
 * @property {string} tutorEmail
 * @property {string} tutorTelefono
 * @property {number} cuotaTemporada
 * @property {number} totalPagado
 * @property {number} pendiente
 */

/**
 * @typedef {Object} PagoPendienteRow
 * @property {number} id
 * @property {string} jugadorNombre
 * @property {string} concepto
 * @property {number} importe
 * @property {string|null} fechaPago
 * @property {string|null} registradoPor
 */

const APP_CFG = window.AppConstants || {};
const API = APP_CFG.api || {};
const MSG = APP_CFG.mensajes || {};
const ADMIN_UI = APP_CFG.ui || {};
const PAGO = APP_CFG.pago || {};

document.getElementById('nombre-admin').textContent = ADMIN_UI.userPrefix + getNombre();

let jugadorIdPago = null;
let jugadorIdBorrar = null;

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
 * Cambia la pestana activa y carga los datos asociados.
 * @param {string} pestana Identificador de pestana (jugadores o pagos).
 * @returns {Promise<void>} Promesa resuelta al terminar la carga.
 */
async function cambiarPestana(pestana) {
    document.querySelectorAll('.pestana-btn').forEach((b) => b.classList.remove('active'));
    document.getElementById('pestana-' + pestana).classList.add('active');
    document.getElementById('seccion-jugadores').style.display = pestana === 'jugadores' ? 'block' : 'none';
    document.getElementById('seccion-pagos').style.display = pestana === 'pagos' ? 'block' : 'none';

    if (pestana === 'pagos') await cargarPagosPendientes();
    if (pestana === 'jugadores') await cargarJugadores();
}

/**
 * Carga el listado administrativo de jugadores, opcionalmente filtrado.
 * @param {string|null} [categoria=null] Categoria a filtrar.
 * @returns {Promise<void>} Promesa resuelta tras renderizar la tabla.
 */
async function cargarJugadores(categoria = null) {
    const url = categoria && categoria !== 'todos'
        ? API.jugadoresAdminCategoria + categoria
        : API.jugadoresAdminAll;

    const response = await fetchConAuth(url);
    if (!response) return;

    /** @type {JugadorAdminRow[]} */
    const jugadores = await response.json();
    const tbody = document.getElementById('tabla-body');
    tbody.innerHTML = '';

    let totalPendiente = 0;
    let totalPagado = 0;

    jugadores.forEach((j) => {
        const pendienteClass = j.pendiente > 0 ? 'pendiente' : 'pagado';
        totalPendiente += j.pendiente;
        totalPagado += j.totalPagado;
        const nombreCompleto = `${j.nombre} ${j.apellidos}`;
        const tutorCompleto = `${j.tutorNombre} ${j.tutorApellidos}`;

        const sinConfirmar = j.totalPagado === 0
            ? `<span class="badge-sin-confirmar">${MSG.badgeSinConfirmar}</span>`
            : '';

        tbody.innerHTML += `
            <tr>
                <td><strong>${escapeHtml(nombreCompleto)}</strong>${sinConfirmar}</td>
                <td><span class="categoria-badge">${escapeHtml(j.categoria)}</span></td>
                <td>${j.fechaNacimiento}</td>
                <td>${escapeHtml(tutorCompleto)}</td>
                <td>
                    <a href="mailto:${escapeHtml(j.tutorEmail)}" class="contacto-link">${escapeHtml(j.tutorEmail)}</a><br>
                    <a href="tel:${escapeHtml(j.tutorTelefono)}" class="contacto-link">${escapeHtml(j.tutorTelefono)}</a>
                </td>
                <td>${j.cuotaTemporada}${ADMIN_UI.euro}</td>
                <td class="pagado">${j.totalPagado}${ADMIN_UI.euro}</td>
                <td class="${pendienteClass}">${j.pendiente}${ADMIN_UI.euro}</td>
                <td>
                    <button class="btn-accion btn-pago"
                            data-action="abrir-modal-pago-efectivo"
                            data-jugador-id="${j.id}"
                            data-jugador-nombre="${escapeHtml(nombreCompleto)}"
                            data-pendiente="${j.pendiente}">${MSG.accionEfectivo}</button>
                    <button class="btn-accion btn-borrar"
                            data-action="abrir-modal-borrar"
                            data-jugador-id="${j.id}"
                            data-jugador-nombre="${escapeHtml(nombreCompleto)}">${MSG.accionBorrar}</button>
                </td>
            </tr>
        `;
    });

    document.getElementById('stats-bar').innerHTML = `
        <span>Total jugadores: <strong>${jugadores.length}</strong></span>
        <span>Total pagado: <strong class="pagado">${totalPagado.toFixed(2)}${ADMIN_UI.euro}</strong></span>
        <span>Total pendiente: <strong class="pendiente">${totalPendiente.toFixed(2)}${ADMIN_UI.euro}</strong></span>
    `;
}

/**
 * Activa un filtro visual y recarga jugadores para esa categoria.
 * @param {string} categoria Categoria seleccionada.
 * @param {HTMLElement} btn Boton pulsado para marcar estado activo.
 * @returns {Promise<void>} Promesa resuelta cuando termina la recarga.
 */
async function filtrar(categoria, btn) {
    document.querySelectorAll('.filtro-btn').forEach((b) => b.classList.remove('active'));
    btn.classList.add('active');
    await cargarJugadores(categoria);
}

/**
 * Carga pagos pendientes de confirmar y renderiza su tabla.
 * @returns {Promise<void>} Promesa resuelta al finalizar.
 */
async function cargarPagosPendientes() {
    const response = await fetchConAuth(API.pagosPendientes);
    if (!response) return;

    /** @type {PagoPendienteRow[]} */
    const pagos = await response.json();
    const tbody = document.getElementById('pagos-pendientes-container');
    tbody.innerHTML = '';

    document.getElementById('badge-pendientes').textContent = String(pagos.length);

    if (pagos.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="no-data">${MSG.noPagosPendientes}</td></tr>`;
        return;
    }

    pagos.forEach((p) => {
        const nombreJugador = p.nombreJugador || p.jugadorNombre || ADMIN_UI.dash;
        const concepto = String(p.concepto || nombreJugador).toUpperCase();
        tbody.innerHTML += `
            <tr>
                <td><strong>${escapeHtml(nombreJugador)}</strong></td>
                <td class="text-mono-accent">${escapeHtml(concepto)}</td>
                <td class="pendiente text-strong">${p.importe}${ADMIN_UI.euro}</td>
                <td>${p.fechaPago || ADMIN_UI.dash}</td>
                <td>${p.registradoPor || ADMIN_UI.dash}</td>
                <td>
                    <button class="btn-accion btn-pago" data-action="confirmar-transferencia" data-pago-id="${p.id}">${MSG.accionConfirmar}</button>
                    <button class="btn-accion btn-borrar" data-action="rechazar-transferencia" data-pago-id="${p.id}" data-jugador-nombre="${escapeHtml(nombreJugador)}">${MSG.accionRechazar}</button>
                </td>
            </tr>
        `;
    });
}

/**
 * Confirma un pago por transferencia pendiente.
 * @param {number} pagoId Identificador del pago.
 * @returns {Promise<void>} Promesa resuelta tras recargar datos.
 */
async function confirmarTransferencia(pagoId) {
    const response = await fetchConAuth(
        API.pagosConfirmar + pagoId + API.pagosConfirmarSuffix,
        { method: 'PUT' }
    );
    if (response && response.ok) {
        await cargarPagosPendientes();
    } else {
        alert(MSG.errorConfirmarPago);
    }
}

/**
 * Rechaza un pago pendiente y elimina el jugador asociado segun backend.
 * @param {number} pagoId Identificador del pago.
 * @param {string} nombre Nombre visible del jugador para confirmacion.
 * @returns {Promise<void>} Promesa resuelta tras procesar la accion.
 */
async function rechazarTransferencia(pagoId, nombre) {
    if (!confirm(MSG.confirmarRechazarPagoJugador.replace('{nombre}', nombre))) return;
    const response = await fetchConAuth(
        API.pagosConfirmar + pagoId + API.pagosRechazarSuffix,
        { method: 'PUT' }
    );
    if (response && response.ok) {
        await cargarPagosPendientes();
    } else {
        alert(MSG.errorRechazarPago);
    }
}

/**
 * Abre el modal para registrar pago en efectivo.
 * @param {number} id Identificador del jugador.
 * @param {string} nombre Nombre completo del jugador.
 * @param {number} pendiente Importe pendiente actual.
 * @returns {void}
 */
function abrirModalPagoEfectivo(id, nombre, pendiente) {
    jugadorIdPago = id;
    document.getElementById('modal-jugador-nombre').textContent = `${nombre} · ${MSG.textoPendienteJugador}: ${pendiente}${ADMIN_UI.euro}`;
    document.getElementById('modal-importe').value = String(pendiente);
    document.getElementById('modal-concepto').value = '';
    document.getElementById('modal-error').style.display = 'none';
    document.getElementById('modal-success').style.display = 'none';
    document.getElementById('modal-pago').style.display = 'flex';
}

/**
 * Cierra el modal de pago en efectivo y limpia estado temporal.
 * @returns {void}
 */
function cerrarModal() {
    document.getElementById('modal-pago').style.display = 'none';
    jugadorIdPago = null;
}

/**
 * Confirma el pago en efectivo introducido en modal.
 * @returns {Promise<void>} Promesa resuelta al finalizar alta y recarga.
 */
async function confirmarPago() {
    const importe = document.getElementById('modal-importe').value;
    const concepto = document.getElementById('modal-concepto').value;

    if (!importe || importe <= 0) {
        document.getElementById('modal-error').style.display = 'block';
        document.getElementById('modal-error').textContent = MSG.importeMayorCero;
        return;
    }

    const response = await fetchConAuth(API.pagosEfectivo, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            jugadorId: jugadorIdPago,
            importe: parseFloat(importe),
            concepto: concepto || MSG.conceptoEfectivoDefault,
            metodoPago: PAGO.metodoEfectivo
        })
    });

    if (response && response.ok) {
        cerrarModal();
        await cargarJugadores();
    } else {
        document.getElementById('modal-error').style.display = 'block';
        document.getElementById('modal-error').textContent = MSG.errorRegistrarPago;
    }
}

/**
 * Abre el modal de confirmacion para borrar jugador.
 * @param {number} id Identificador del jugador.
 * @param {string} nombre Nombre del jugador.
 * @returns {void}
 */
function abrirModalBorrar(id, nombre) {
    jugadorIdBorrar = id;
    document.getElementById('modal-borrar-nombre').textContent = MSG.eliminarJugadorConfirmacion.replace('{nombre}', nombre);
    document.getElementById('modal-borrar').style.display = 'flex';
}

/**
 * Cierra el modal de borrado y limpia estado temporal.
 * @returns {void}
 */
function cerrarModalBorrar() {
    document.getElementById('modal-borrar').style.display = 'none';
    jugadorIdBorrar = null;
}

/**
 * Ejecuta el borrado del jugador seleccionado en modal.
 * @returns {Promise<void>} Promesa resuelta al terminar la operacion.
 */
async function confirmarBorrar() {
    const response = await fetchConAuth(API.jugadores + '/' + jugadorIdBorrar, { method: 'DELETE' });
    if (response && response.ok) {
        cerrarModalBorrar();
        await cargarJugadores();
    } else {
        cerrarModalBorrar();
        alert(MSG.errorEliminarJugador);
    }
}

/**
 * Registra todos los eventos click del panel de administracion.
 * @returns {void}
 */
function bindAdminPanelEvents() {
    document.addEventListener('click', async (event) => {
        const target = event.target.closest('[data-action]');
        if (!target) return;

        const action = target.dataset.action;
        if (action === 'cambiar-pestana') {
            await cambiarPestana(target.dataset.pestana);
        } else if (action === 'filtrar') {
            await filtrar(target.dataset.categoria, target);
        } else if (action === 'abrir-modal-pago-efectivo') {
            abrirModalPagoEfectivo(Number(target.dataset.jugadorId), target.dataset.jugadorNombre, Number(target.dataset.pendiente));
        } else if (action === 'abrir-modal-borrar') {
            abrirModalBorrar(Number(target.dataset.jugadorId), target.dataset.jugadorNombre);
        } else if (action === 'confirmar-transferencia') {
            await confirmarTransferencia(Number(target.dataset.pagoId));
        } else if (action === 'rechazar-transferencia') {
            await rechazarTransferencia(Number(target.dataset.pagoId), target.dataset.jugadorNombre);
        } else if (action === 'confirmar-pago-efectivo') {
            await confirmarPago();
        } else if (action === 'cerrar-modal-pago') {
            cerrarModal();
        } else if (action === 'confirmar-borrar') {
            await confirmarBorrar();
        } else if (action === 'cerrar-modal-borrar') {
            cerrarModalBorrar();
        }
    });
}

/**
 * Inicializa el panel de administracion.
 * @returns {Promise<void>} Promesa resuelta tras cargar datos iniciales.
 */
async function initAdminPanel() {
    bindAdminPanelEvents();
    await cargarJugadores();
    await cargarPagosPendientes();
}

void initAdminPanel();
