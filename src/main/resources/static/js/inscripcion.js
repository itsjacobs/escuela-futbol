checkAuth();

const categorias = {
    2008: 'Juvenil', 2009: 'Juvenil', 2010: 'Juvenil',
    2011: 'Cadete', 2012: 'Cadete',
    2013: 'Infantil', 2014: 'Infantil',
    2015: 'Alevin', 2016: 'Alevin',
    2017: 'Benjamin', 2018: 'Benjamin',
    2019: 'Prebenjamin', 2020: 'Prebenjamin',
    2021: 'Debutante', 2022: 'Debutante'
};

const cuotas = {
    'Debutante': 280, 'Prebenjamin': 280, 'Benjamin': 280, 'Alevin': 280,
    'Infantil': 320, 'Cadete': 320, 'Juvenil': 320
};

let categoriaCalculada = '';
let cuotaCalculada = 0;
let necesitaEquipacionActual = false;
let numeroCuotasSeleccionado = 1;

function calcularCategoria() {
    const nombre = document.getElementById('nombre').value;
    const apellidos = document.getElementById('apellidos').value;
    const fechaNacimiento = document.getElementById('fechaNacimiento').value;
    necesitaEquipacionActual = document.getElementById('necesitaEquipacion').value === 'true';

    if (!nombre || !apellidos || !fechaNacimiento) {
        document.getElementById('error-msg').style.display = 'block';
        document.getElementById('error-msg').textContent = 'Rellena todos los campos';
        return;
    }

    document.getElementById('error-msg').style.display = 'none';

    const anio = new Date(fechaNacimiento).getFullYear();
    categoriaCalculada = categorias[anio] || 'Sin categoría';
    cuotaCalculada = cuotas[categoriaCalculada] || 0;

    // Rellenar resumen
    document.getElementById('resumen-nombre').textContent = nombre + ' ' + apellidos;
    document.getElementById('resumen-fecha').textContent = fechaNacimiento;
    document.getElementById('resumen-categoria').textContent = categoriaCalculada;
    document.getElementById('resumen-cuota').textContent = cuotaCalculada + '€';

    // Equipación
    document.getElementById('resumen-equipacion-row').style.display = necesitaEquipacionActual ? 'flex' : 'none';

    // Fraccionamiento: solo si NO necesita equipación
    const seccionCuotas = document.getElementById('seccion-cuotas');
    if (!necesitaEquipacionActual) {
        seccionCuotas.style.display = 'block';
        actualizarResumenCuotas();
    } else {
        seccionCuotas.style.display = 'none';
        document.getElementById('resumen-total').textContent = '160€ (equipación) + ' + cuotaCalculada + '€ (cuota)';
    }

    document.getElementById('paso1').style.display = 'none';
    document.getElementById('paso2').style.display = 'block';
}

function seleccionarCuotas(n, btn) {
    numeroCuotasSeleccionado = n;
    document.querySelectorAll('.cuota-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    actualizarResumenCuotas();
}

function actualizarResumenCuotas() {
    const importePrimeraCuota = (cuotaCalculada / numeroCuotasSeleccionado).toFixed(2);
    const textoTotal = numeroCuotasSeleccionado === 1
        ? cuotaCalculada + '€ (pago único)'
        : importePrimeraCuota + '€ ahora · ' + numeroCuotasSeleccionado + ' cuotas de ' + importePrimeraCuota + '€';
    document.getElementById('resumen-total').textContent = textoTotal;
    document.getElementById('resumen-primera-cuota').textContent = importePrimeraCuota + '€';
}

function volver() {
    document.getElementById('paso1').style.display = 'block';
    document.getElementById('paso2').style.display = 'none';
}

async function inscribir() {
    const nombre = document.getElementById('nombre').value;
    const apellidos = document.getElementById('apellidos').value;

    const body = {
        nombre: nombre,
        apellidos: apellidos,
        fechaNacimiento: document.getElementById('fechaNacimiento').value,
        necesitaEquipacion: necesitaEquipacionActual,
        numeroCuotas: necesitaEquipacionActual ? 1 : numeroCuotasSeleccionado,
        tutorId: null
    };

    const response = await fetchConAuth('/api/jugadores', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });

    if (!response) return;

    if (response.ok) {
        const jugador = await response.json();

        // Obtener el pago pendiente para el concepto e importe
        const pagosRes = await fetchConAuth(`/api/pagos/jugador/${jugador.id}`);
        let concepto = 'PAGO-' + apellidos.split(' ')[0].toUpperCase() + '-' + categoriaCalculada.toUpperCase();
        let importe = necesitaEquipacionActual ? 160 : (cuotaCalculada / numeroCuotasSeleccionado).toFixed(2);

        if (pagosRes && pagosRes.ok) {
            const pagos = await pagosRes.json();
            const pagoPendiente = pagos.find(p => p.estado === 'PENDIENTE');
            if (pagoPendiente?.concepto) concepto = pagoPendiente.concepto;
            if (pagoPendiente?.importe) importe = pagoPendiente.importe;
        }

        // Guardar en sessionStorage para la página de pago
        sessionStorage.setItem('pago_nombre', nombre + ' ' + apellidos);
        sessionStorage.setItem('pago_concepto', concepto);
        sessionStorage.setItem('pago_importe', importe);

        window.location.href = '/pago';

    } else {
        document.getElementById('error-msg').style.display = 'block';
        document.getElementById('error-msg').textContent = 'Error al inscribir al jugador. Inténtalo de nuevo.';
    }
}