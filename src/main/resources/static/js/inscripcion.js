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

function calcularCategoria() {
    const nombre = document.getElementById('nombre').value;
    const apellidos = document.getElementById('apellidos').value;
    const fechaNacimiento = document.getElementById('fechaNacimiento').value;
    const necesitaEquipacion = document.getElementById('necesitaEquipacion').value === 'true';

    if (!nombre || !apellidos || !fechaNacimiento) {
        document.getElementById('error-msg').style.display = 'block';
        document.getElementById('error-msg').textContent = 'Rellena todos los campos';
        return;
    }

    const anio = new Date(fechaNacimiento).getFullYear();
    const categoria = categorias[anio] || 'Sin categoría';
    const cuota = cuotas[categoria] || 0;
    const total = necesitaEquipacion ? cuota + 160 : cuota;

    // Rellenar resumen
    document.getElementById('resumen-nombre').textContent = nombre + ' ' + apellidos;
    document.getElementById('resumen-fecha').textContent = fechaNacimiento;
    document.getElementById('resumen-categoria').textContent = categoria;
    document.getElementById('resumen-cuota').textContent = cuota + '€';
    document.getElementById('resumen-total').textContent = total + '€';

    // Mostrar u ocultar fila de equipación
    document.getElementById('resumen-equipacion-row').style.display = necesitaEquipacion ? 'flex' : 'none';

    // Cambiar de paso
    document.getElementById('paso1').style.display = 'none';
    document.getElementById('paso2').style.display = 'block';
}

function volver() {
    document.getElementById('paso1').style.display = 'block';
    document.getElementById('paso2').style.display = 'none';
}

async function inscribir() {
    const body = {
        nombre: document.getElementById('nombre').value,
        apellidos: document.getElementById('apellidos').value,
        fechaNacimiento: document.getElementById('fechaNacimiento').value,
        necesitaEquipacion: document.getElementById('necesitaEquipacion').value === 'true',
        tutorId: null // se obtiene del token en el backend
    };

    const response = await fetch('/api/jugadores', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + getToken()
        },
        body: JSON.stringify(body)
    });

    if (response.ok) {
        const jugador = await response.json();
        document.getElementById('paso2').style.display = 'none';
        document.getElementById('paso3').style.display = 'block';
        document.getElementById('confirmacion-msg').textContent =
            jugador.nombre + ' ha sido inscrito en la categoría ' + jugador.categoria;
    } else {
        document.getElementById('error-msg').style.display = 'block';
        document.getElementById('error-msg').textContent = 'Error al inscribir el jugador';
        document.getElementById('paso1').style.display = 'block';
        document.getElementById('paso2').style.display = 'none';
    }
}