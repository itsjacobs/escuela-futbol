checkAdmin();
document.getElementById('nombre-admin').textContent = '👤 ' + getNombre();

let jugadorIdPago = null;
let jugadorIdBorrar = null;

// ── PESTAÑAS ───────────────────────────────────────────────
function cambiarPestana(pestana) {
    document.querySelectorAll('.pestana-btn').forEach(b => b.classList.remove('active'));
    document.getElementById('pestana-' + pestana).classList.add('active');
    document.getElementById('seccion-jugadores').style.display = pestana === 'jugadores' ? 'block' : 'none';
    document.getElementById('seccion-pagos').style.display = pestana === 'pagos' ? 'block' : 'none';

    if (pestana === 'pagos') cargarPagosPendientes();
    if (pestana === 'jugadores') cargarJugadores();
}

// ── JUGADORES ──────────────────────────────────────────────
async function cargarJugadores(categoria = null) {
    const url = categoria && categoria !== 'todos'
        ? `/api/jugadores/admin/categoria/${categoria}`
        : '/api/jugadores/admin/all';

    const response = await fetchConAuth(url);
    if (!response) return;

    const jugadores = await response.json();
    const tbody = document.getElementById('tabla-body');
    tbody.innerHTML = '';

    let totalPendiente = 0;
    let totalPagado = 0;

    jugadores.forEach(j => {
        const pendienteClass = j.pendiente > 0 ? 'pendiente' : 'pagado';
        totalPendiente += j.pendiente;
        totalPagado += j.totalPagado;

        // Indicativo si no hay ningún pago confirmado
        const sinConfirmar = j.totalPagado === 0
            ? `<span class="badge-sin-confirmar">⏳ Sin confirmar</span>`
            : '';

        tbody.innerHTML += `
            <tr>
                <td><strong>${j.nombre} ${j.apellidos}</strong>${sinConfirmar}</td>
                <td><span class="categoria-badge">${j.categoria}</span></td>
                <td>${j.fechaNacimiento}</td>
                <td>${j.tutorNombre} ${j.tutorApellidos}</td>
                <td>
                    <a href="mailto:${j.tutorEmail}" class="contacto-link">📧 ${j.tutorEmail}</a><br>
                    <a href="tel:${j.tutorTelefono}" class="contacto-link">📞 ${j.tutorTelefono}</a>
                </td>
                <td>${j.cuotaTemporada}€</td>
                <td class="pagado">${j.totalPagado}€</td>
                <td class="${pendienteClass}">${j.pendiente}€</td>
                <td>
                    <button class="btn-accion btn-pago" onclick="abrirModalPagoEfectivo(${j.id}, '${j.nombre} ${j.apellidos}', ${j.pendiente})">
                        💶 Efectivo
                    </button>
                    <button class="btn-accion btn-borrar" onclick="abrirModalBorrar(${j.id}, '${j.nombre} ${j.apellidos}')">
                        🗑️ Borrar
                    </button>
                </td>
            </tr>
        `;
    });

    document.getElementById('stats-bar').innerHTML = `
        <span>Total jugadores: <strong>${jugadores.length}</strong></span>
        <span>Total pagado: <strong class="pagado">${totalPagado.toFixed(2)}€</strong></span>
        <span>Total pendiente: <strong class="pendiente">${totalPendiente.toFixed(2)}€</strong></span>
    `;
}

function filtrar(categoria, btn) {
    document.querySelectorAll('.filtro-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    cargarJugadores(categoria);
}

// ── PAGOS PENDIENTES ───────────────────────────────────────
async function cargarPagosPendientes() {
    const response = await fetchConAuth('/api/pagos/pendientes');
    if (!response) return;

    const pagos = await response.json();
    const tbody = document.getElementById('pagos-pendientes-container');
    tbody.innerHTML = '';

    document.getElementById('badge-pendientes').textContent = pagos.length;

    if (pagos.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="no-data">✅ No hay pagos pendientes de confirmar.</td></tr>`;
        return;
    }

    pagos.forEach(p => {
        tbody.innerHTML += `
            <tr>
                <td><strong>${p.jugadorNombre}</strong></td>
                <td style="font-family:monospace;font-weight:700;color:var(--azul)">${p.concepto}</td>
                <td class="pendiente" style="font-weight:700">${p.importe}€</td>
                <td>${p.fechaPago || '—'}</td>
                <td>${p.registradoPor || '—'}</td>
                <td>
                    <button class="btn-accion btn-pago" onclick="confirmarTransferencia(${p.id})">
                        ✅ Confirmar
                    </button>
                    <button class="btn-accion btn-borrar" onclick="rechazarTransferencia(${p.id}, '${p.jugadorNombre}')">
                        ❌ Rechazar
                    </button>
                </td>
            </tr>
        `;
    });
}

async function confirmarTransferencia(pagoId) {
    const response = await fetchConAuth(`/api/pagos/${pagoId}/confirmar`, { method: 'PUT' });
    if (response && response.ok) {
        cargarPagosPendientes();
    } else {
        alert('Error al confirmar el pago');
    }
}

async function rechazarTransferencia(pagoId, nombre) {
    if (!confirm(`¿Rechazar el pago de ${nombre}? Se eliminará al jugador.`)) return;
    const response = await fetchConAuth(`/api/pagos/${pagoId}/rechazar`, { method: 'PUT' });
    if (response && response.ok) {
        cargarPagosPendientes();
    } else {
        alert('Error al rechazar el pago');
    }
}

// ── MODAL PAGO EFECTIVO ────────────────────────────────────
function abrirModalPagoEfectivo(id, nombre, pendiente) {
    jugadorIdPago = id;
    document.getElementById('modal-jugador-nombre').textContent = nombre + ' · Pendiente: ' + pendiente + '€';
    document.getElementById('modal-importe').value = pendiente;
    document.getElementById('modal-concepto').value = '';
    document.getElementById('modal-error').style.display = 'none';
    document.getElementById('modal-success').style.display = 'none';
    document.getElementById('modal-pago').style.display = 'flex';
}

function cerrarModal() {
    document.getElementById('modal-pago').style.display = 'none';
    jugadorIdPago = null;
}

async function confirmarPago() {
    const importe = document.getElementById('modal-importe').value;
    const concepto = document.getElementById('modal-concepto').value;

    if (!importe || importe <= 0) {
        document.getElementById('modal-error').style.display = 'block';
        document.getElementById('modal-error').textContent = 'El importe debe ser mayor que 0';
        return;
    }

    const response = await fetchConAuth('/api/pagos/efectivo', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            jugadorId: jugadorIdPago,
            importe: parseFloat(importe),
            concepto: concepto || 'Pago en efectivo',
            metodoPago: 'EFECTIVO'
        })
    });

    if (response && response.ok) {
        cerrarModal();           // cerrar primero
        cargarJugadores();       // luego recargar sin timeout
    } else {
        document.getElementById('modal-error').style.display = 'block';
        document.getElementById('modal-error').textContent = 'Error al registrar el pago';
    }
}

// ── MODAL BORRAR ───────────────────────────────────────────
function abrirModalBorrar(id, nombre) {
    jugadorIdBorrar = id;
    document.getElementById('modal-borrar-nombre').textContent = '¿Seguro que quieres eliminar a ' + nombre + '?';
    document.getElementById('modal-borrar').style.display = 'flex';
}

function cerrarModalBorrar() {
    document.getElementById('modal-borrar').style.display = 'none';
    jugadorIdBorrar = null;
}

async function confirmarBorrar() {
    const response = await fetchConAuth(`/api/jugadores/${jugadorIdBorrar}`, { method: 'DELETE' });
    if (response && response.ok) {
        cerrarModalBorrar();
        cargarJugadores();
    } else {
        cerrarModalBorrar();
        alert('Error al eliminar el jugador');
    }
}

// ── INICIO ─────────────────────────────────────────────────
cargarJugadores();
cargarPagosPendientes();