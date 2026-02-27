checkAuth();

// Leer jugadorId de la URL (?jugadorId=1)
const params = new URLSearchParams(window.location.search);
const jugadorId = params.get('jugadorId');

if (!jugadorId) window.location.href = '/tutor/panel';

// Inicializar Stripe con la clave pública
const stripe = Stripe('pk_test_51T54TCRUIQxviFnovomFERlKSFmBHlvertGXgAANrViSgXjWFIyXXHaXgihJo79fOxND5WGfIXFt7Y2XdSSa5b8N001gzU5wMD'); // pk_test_...
const elements = stripe.elements();

// Crear el elemento de tarjeta
const cardElement = elements.create('card', {
    style: {
        base: {
            fontSize: '16px',
            color: '#1a1a2e',
            '::placeholder': { color: '#aab7c4' }
        }
    }
});
cardElement.mount('#card-element');

let clientSecret = null;

// Al cargar la página creamos el PaymentIntent
async function iniciarPago() {
    const response = await fetch('/api/stripe/payment-intent', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + getToken()
        },
        body: JSON.stringify({
            importe: 160,
            descripcion: 'Equipación temporada - Jugador ID: ' + jugadorId
        })
    });

    const data = await response.json();
    clientSecret = data.clientSecret;
}

async function pagar() {
    const btn = document.getElementById('btn-pagar');
    btn.disabled = true;
    btn.textContent = 'Procesando...';

    const { error, paymentIntent } = await stripe.confirmCardPayment(clientSecret, {
        payment_method: {
            card: cardElement
        }
    });

    if (error) {
        document.getElementById('error-msg').style.display = 'block';
        document.getElementById('error-msg').textContent = error.message;
        btn.disabled = false;
        btn.textContent = 'Pagar 160€';
        return;
    }

    if (paymentIntent.status === 'succeeded') {
        // Registrar el pago en nuestra BD
        await fetch('/api/equipaciones', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + getToken()
            },
            body: JSON.stringify({
                jugadorId: parseInt(jugadorId),
                importe: 160,
                motivo: 'NUEVA_INSCRIPCION'
            })
        });

        document.getElementById('success-msg').style.display = 'block';
        document.getElementById('success-msg').textContent = '¡Pago realizado correctamente! Redirigiendo...';
        setTimeout(() => window.location.href = '/tutor/panel', 2000);
    }
}

iniciarPago();