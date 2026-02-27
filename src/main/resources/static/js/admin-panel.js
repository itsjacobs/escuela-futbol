checkAdmin();

document.getElementById('nombre-admin').textContent = '👤 ' + getNombre();

let jugadorIdPago = null;
let jugadorIdBorrar = null;

async function cargarJugadores(categoria = null) {
    const url = categoria && categoria !== 'todos'
        ? `/api/jugadores/admin/categoria/${categoria}`
        : '/api/jugadores/admin/all';

    const response = await fetch(url, {
        headers: { 'Authorization': 'Bearer ' + getToken() }
    });

    if (response.status === 401) {
        window.location.href = '/login';
        return;
    }

    const jugadores = await response.json();
    const tbody = document.getElementById('tabla-body');
    tbody.innerHTML = '';

    let totalPendiente = 0;
    let totalPagado = 0;

    jugadores.forEach(j => {
        const pendienteClass = j.pendiente > 0 ? 'pendiente' : 'pagado';
        totalPendiente += j.pendiente;
        totalPagado += j.totalPagado;

        tbody.innerHTML += `
            <tr>
                <td><strong>${j.nombre} ${j.apellidos}</strong></td>
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
                    <button class="btn-accion btn-pago" onclick="abrirModalPago(${j.id}, '${j.nombre} ${j.apellidos}', ${j.pendiente})">
                        💶 Pago
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

// ── MODAL PAGO ─────────────────────────────────────────────
function abrirModalPago(id, nombre, pendiente) {
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

    const response = await fetch('/api/pagos/efectivo', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + getToken()
        },
        body: JSON.stringify({
            jugadorId: jugadorIdPago,
            importe: parseFloat(importe),
            concepto: concepto || 'Pago en efectivo',
            metodoPago: 'EFECTIVO'
        })
    });

    if (response.ok) {
        document.getElementById('modal-success').style.display = 'block';
        document.getElementById('modal-success').textContent = 'Pago registrado correctamente';
        setTimeout(() => {
            cerrarModal();
            cargarJugadores();
        }, 1500);
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
    const response = await fetch(`/api/jugadores/${jugadorIdBorrar}`, {
        method: 'DELETE',
        headers: { 'Authorization': 'Bearer ' + getToken() }
    });

    if (response.ok) {
        cerrarModalBorrar();
        cargarJugadores();
    } else {
        cerrarModalBorrar();
        alert('Error al eliminar el jugador');
    }
}

cargarJugadores();