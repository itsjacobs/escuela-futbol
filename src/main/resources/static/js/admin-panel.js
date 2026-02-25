checkAdmin();

async function cargarJugadores(categoria = null) {
    const url = categoria && categoria !== 'todos'
        ? `/api/jugadores/categoria/${categoria}`
        : '/api/jugadores';

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

    jugadores.forEach(j => {
        const pendienteClass = j.pendiente > 0 ? 'pendiente' : 'pagado';
        tbody.innerHTML += `
            <tr>
                <td>${j.nombre} ${j.apellidos}</td>
                <td><span class="categoria-badge">${j.categoria}</span></td>
                <td>${j.fechaNacimiento}</td>
                <td>${j.cuotaTemporada}€</td>
                <td class="pagado">${j.totalPagado}€</td>
                <td class="${pendienteClass}">${j.pendiente}€</td>
            </tr>
        `;
    });

    document.getElementById('stats-bar').textContent = `Total jugadores: ${jugadores.length}`;
}

function filtrar(categoria) {
    cargarJugadores(categoria);
}

cargarJugadores();