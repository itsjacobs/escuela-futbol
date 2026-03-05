checkAuth();
document.getElementById('nombre-tutor').textContent = '👤 ' + getNombre();

let jugadorIdCuotas = null;
let cuotaTotalJugador = 0;
let numeroCuotasModal = 1;

async function cargarJugadores() {
    const response = await fetchConAuth('/api/jugadores/tutor/me');
    if (!response) return;

    const jugadores = await response.json();
    const container = document.getElementById('jugadores-container');
    container.innerHTML = '';

    if (jugadores.length === 0) {
        container.innerHTML = `
            <div class="no-data">
                <p>No tienes jugadores inscritos aún.</p>
                <a href="/inscripcion" class="btn-primary" style="display:inline-block;margin-top:15px">Inscribir jugador</a>
            </div>`;
    } else {
        jugadores.forEach(j => {
            const pendienteClass = j.pendiente > 0 ? 'pendiente' : 'pagado';
            const botonAccion = generarBotonAccion(j);

            container.innerHTML += `
                <div class="jugador-card">
                    <h2>${j.nombre} ${j.apellidos}</h2>
                    <span class="categoria-badge">${j.categoria}</span>
                    <div class="pago-info">
                        <div class="pago-row">
                            <span>Cuota temporada</span>
                            <span>${j.cuotaTemporada}€</span>
                        </div>
                        <div class="pago-row">
                            <span>Total pagado</span>
                            <span class="pagado">${j.totalPagado}€</span>
                        </div>
                        <div class="pago-row">
                            <span>Pendiente</span>
                            <span class="${pendienteClass}">${j.pendiente}€</span>
                        </div>
                    </div>
                    ${botonAccion}
                </div>
            `;
        });
    }

    container.innerHTML += `
        <div class="jugador-card nueva-inscripcion">
            <h2>➕ Inscribir jugador</h2>
            <p>¿Tienes otro hijo/a que quiera unirse?</p>
            <a href="/inscripcion" class="btn-primary" style="display:inline-block;margin-top:15px">Inscribir</a>
        </div>
    `;
}

function generarBotonAccion(j) {
    // Cuota al día
    if (j.pendiente <= 0) {
        return `<p style="color:#27ae60;text-align:center;margin-top:15px;font-weight:600">✅ Cuota al día</p>`;
    }

    // Tiene transferencia enviada esperando confirmación del admin
    if (j.tieneCuotaPendiente) {
        return `
            <div class="estado-pago-pendiente">
                ⏳ Transferencia enviada — esperando confirmación del club
            </div>
            <button class="btn-secondary" style="width:100%;margin-top:10px"
                onclick="verDatosPago(${j.id}, '${j.nombre} ${j.apellidos}', ${j.cuotaTemporada}, ${j.numeroCuotas || 1})">
                Ver datos de la transferencia
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
            <button class="btn-primary" style="width:100%;margin-top:15px"
                onclick="abrirModalCuotas(${j.id}, '${j.nombre} ${j.apellidos}', ${j.cuotaTemporada})">
                📅 Elegir forma de pago
            </button>`;
    }

    // Ya eligió cuotas, puede pagar la siguiente
    return `
        <button class="btn-primary" style="width:100%;margin-top:15px"
            onclick="verDatosPago(${j.id}, '${j.nombre} ${j.apellidos}', ${j.cuotaTemporada}, ${j.numeroCuotas || 1})">
            🏦 Pagar siguiente cuota
        </button>`;
}

// ── MODAL ELEGIR CUOTAS ────────────────────────────────────
function abrirModalCuotas(jugadorId, nombre, cuotaTotal) {
    jugadorIdCuotas = jugadorId;
    cuotaTotalJugador = cuotaTotal;
    numeroCuotasModal = 1;

    document.getElementById('modal-cuotas-nombre').textContent = nombre;
    document.getElementById('modal-cuotas-total').textContent = cuotaTotal + '€';

    document.querySelectorAll('.cuota-btn').forEach((b, i) => {
        b.classList.toggle('active', i === 0);
    });

    actualizarImporteCuota();
    document.getElementById('modal-cuotas-error').style.display = 'none';
    document.getElementById('modal-cuotas').style.display = 'flex';
}

function seleccionarCuotasModal(n, btn) {
    numeroCuotasModal = n;
    document.querySelectorAll('.cuota-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    actualizarImporteCuota();
}

function actualizarImporteCuota() {
    const importe = (cuotaTotalJugador / numeroCuotasModal).toFixed(2);
    document.getElementById('modal-importe-cuota').textContent = importe + '€';
}

async function confirmarElegirCuotas() {
    const response = await fetchConAuth('/api/pagos/elegir-cuotas', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            jugadorId: jugadorIdCuotas,
            numeroCuotas: numeroCuotasModal
        })
    });

    if (!response || !response.ok) {
        document.getElementById('modal-cuotas-error').style.display = 'block';
        document.getElementById('modal-cuotas-error').textContent = 'Error al generar el pago. Inténtalo de nuevo.';
        return;
    }

    const pago = await response.json();
    const nombre = document.getElementById('modal-cuotas-nombre').textContent;
    cerrarModalCuotas();
    abrirModalTransferenciaConDatos(nombre, pago.importe, pago.concepto);
    cargarJugadores();
}

function cerrarModalCuotas() {
    document.getElementById('modal-cuotas').style.display = 'none';
    jugadorIdCuotas = null;
}

// ── VER DATOS DE PAGO ──────────────────────────────────────
async function verDatosPago(jugadorId, nombre, cuotaTotal, numeroCuotas) {
    const response = await fetchConAuth(`/api/pagos/jugador/${jugadorId}`);
    if (!response) return;

    const pagos = await response.json();

    // Si ya hay una cuota pendiente de confirmar, mostrar sus datos directamente
    const pagoPendiente = pagos.find(p =>
        p.estado === 'PENDIENTE' && p.concepto?.startsWith('CUOTA')
    );

    if (pagoPendiente) {
        abrirModalTransferenciaConDatos(nombre, pagoPendiente.importe, pagoPendiente.concepto);
        return;
    }

    // No hay pendiente — calcular cuántas cuotas confirmadas hay
    const cuotasConfirmadas = pagos.filter(p =>
        p.estado === 'CONFIRMADO' && p.concepto?.startsWith('CUOTA')
    ).length;

    // Si quedan cuotas por pagar, generar la siguiente
    if (cuotasConfirmadas < numeroCuotas) {
        const res = await fetchConAuth('/api/pagos/siguiente-cuota', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ jugadorId })
        });
        if (!res || !res.ok) {
            alert('Error al generar el siguiente pago');
            return;
        }
        const nuevoPago = await res.json();
        abrirModalTransferenciaConDatos(nombre, nuevoPago.importe, nuevoPago.concepto);
        cargarJugadores();
    }
}

// ── MODAL TRANSFERENCIA ────────────────────────────────────
function abrirModalTransferenciaConDatos(nombre, importe, concepto) {
    document.getElementById('modal-jugador-nombre').textContent = nombre;
    document.getElementById('modal-importe-display').textContent = importe + '€';
    document.getElementById('modal-concepto').textContent = concepto;
    document.getElementById('modal-transferencia').style.display = 'flex';
}

function cerrarModalTransferencia() {
    document.getElementById('modal-transferencia').style.display = 'none';
}

function copiarIban() {
    navigator.clipboard.writeText('ES4221000579611300175282').then(() => {
        alert('✅ IBAN copiado al portapapeles');
    });
}

function copiarConcepto() {
    const concepto = document.getElementById('modal-concepto').textContent;
    navigator.clipboard.writeText(concepto).then(() => {
        alert('✅ Concepto copiado al portapapeles');
    });
}

cargarJugadores();