checkAuth();

document.getElementById('nombre-tutor').textContent = '👤 ' + getNombre();

const stripe = Stripe('pk_test_51T54TCRUIQxviFnovomFERlKSFmBHlvertGXgAANrViSgXjWFIyXXHaXgihJo79fOxND5WGfIXFt7Y2XdSSa5b8N001gzU5wMD'); // pk_test_...
const elements = stripe.elements();
let cardElement = null;
let jugadorIdPago = null;
let maxPendiente = 0;
let clientSecret = null;

function crearCardElement() {
    if (cardElement) {
        cardElement.unmount();
        cardElement.destroy();
    }
    cardElement = elements.create('card', {
        style: {
            base: {
                fontSize: '16px',
                color: '#1a1a2e',
                '::placeholder': { color: '#aab7c4' }
            }
        }
    });
    cardElement.mount('#card-element');
}

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
    container.innerHTML = '';

    if (jugadores.length === 0) {
        container.innerHTML = '<p class="no-data">No tienes jugadores inscritos aún.</p>';
    } else {
        jugadores.forEach(j => {
            const pendienteClass = j.pendiente > 0 ? 'pendiente' : 'pagado';
            const botonPago = j.pendiente > 0
                ? `<button class="btn-primary" style="width:100%;margin-top:15px" onclick="abrirModalPago(${j.id}, '${j.nombre} ${j.apellidos}', ${j.pendiente})">💳 Pagar cuota</button>`
                : `<p style="color:#27ae60;text-align:center;margin-top:15px;font-weight:600">✅ Cuota al día</p>`;

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
                    ${botonPago}
                </div>
            `;
        });
    }

    container.innerHTML += `
        <div class="jugador-card nueva-inscripcion">
            <h2>➕ Inscribir jugador</h2>
            <p>¿Tienes otro hijo/a en la escuela?</p>
            <a href="/inscripcion" class="btn-primary" style="display:inline-block; margin-top:15px">Inscribir</a>
        </div>
    `;
}

async function abrirModalPago(id, nombre, pendiente) {
    jugadorIdPago = id;
    maxPendiente = pendiente;
    clientSecret = null;

    document.getElementById('modal-jugador-nombre').textContent = nombre;
    document.getElementById('modal-pendiente').textContent = pendiente + '€';
    document.getElementById('modal-importe').value = pendiente;
    document.getElementById('modal-importe').max = pendiente;
    document.getElementById('modal-importe-hint').textContent = 'Máximo: ' + pendiente + '€';
    document.getElementById('modal-error').style.display = 'none';
    document.getElementById('modal-success').style.display = 'none';
    document.getElementById('btn-pagar').disabled = false;
    document.getElementById('btn-pagar').textContent = 'Pagar';
    document.getElementById('modal-pago').style.display = 'flex';

    crearCardElement();
}

async function confirmarPago() {
    const importe = parseFloat(document.getElementById('modal-importe').value);

    if (!importe || importe <= 0) {
        document.getElementById('modal-error').style.display = 'block';
        document.getElementById('modal-error').textContent = 'El importe debe ser mayor que 0';
        return;
    }

    if (importe > maxPendiente) {
        document.getElementById('modal-error').style.display = 'block';
        document.getElementById('modal-error').textContent = 'No puedes pagar más de lo que debes (' + maxPendiente + '€)';
        return;
    }

    const btn = document.getElementById('btn-pagar');
    btn.disabled = true;
    btn.textContent = 'Procesando...';

    // Crear PaymentIntent con el importe final elegido
    const piResponse = await fetch('/api/stripe/payment-intent', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + getToken()
        },
        body: JSON.stringify({
            importe: Math.round(importe),
            descripcion: 'Pago cuota jugador ID: ' + jugadorIdPago
        })
    });

    const piData = await piResponse.json();
    clientSecret = piData.clientSecret;

    if (!clientSecret) {
        document.getElementById('modal-error').style.display = 'block';
        document.getElementById('modal-error').textContent = 'Error al conectar con el sistema de pago';
        btn.disabled = false;
        btn.textContent = 'Pagar';
        return;
    }

    const { error, paymentIntent } = await stripe.confirmCardPayment(clientSecret, {
        payment_method: { card: cardElement }
    });

    if (error) {
        document.getElementById('modal-error').style.display = 'block';
        document.getElementById('modal-error').textContent = error.message;
        btn.disabled = false;
        btn.textContent = 'Pagar';
        return;
    }

    if (paymentIntent.status === 'succeeded') {
        await fetch('/api/pagos', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + getToken()
            },
            body: JSON.stringify({
                jugadorId: jugadorIdPago,
                importe: importe,
                concepto: 'Pago cuota online',
                metodoPago: 'ONLINE'
            })
        });

        document.getElementById('modal-success').style.display = 'block';
        document.getElementById('modal-success').textContent = '✅ Pago realizado correctamente';
        setTimeout(() => {
            cerrarModal();
            cargarJugadores();
        }, 2000);
    }
}

function cerrarModal() {
    document.getElementById('modal-pago').style.display = 'none';
    jugadorIdPago = null;
    clientSecret = null;
    if (cardElement) {
        cardElement.unmount();
        cardElement.destroy();
        cardElement = null;
    }
}

cargarJugadores();