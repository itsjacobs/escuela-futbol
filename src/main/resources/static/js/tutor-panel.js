checkAuth();

document.getElementById('nombre-tutor').textContent = '👤 ' + getNombre();

async function cargarJugadores() {
    const response = await fetch('/api/jugadores/tutor/me', {
        headers: { 'Authorization': 'Bearer ' + getToken() }
    });

    if (response.status === 401) {
        window.location.href = '/login';
        return;
    }

    const jugadores = await response.json();
    const container = document.getElementById('jugadores-container');

    if (jugadores.length === 0) {
        container.innerHTML = '<p class="no-data">No tienes jugadores inscritos aún. <a href="/inscripcion">Inscribe a tu hijo/a</a></p>';
        return;
    }

    jugadores.forEach(j => {
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
                        <span class="pendiente">${j.pendiente}€</span>
                    </div>
                </div>
            </div>
        `;
    });
}

cargarJugadores();