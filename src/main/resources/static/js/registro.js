async function registro() {
    const body = {
        nombre: document.getElementById('nombre').value,
        apellidos: document.getElementById('apellidos').value,
        email: document.getElementById('email').value,
        telefono: document.getElementById('telefono').value,
        password: document.getElementById('password').value
    };

    const response = await fetch('/api/auth/registro', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });

    if (response.ok) {
        document.getElementById('success-msg').style.display = 'block';
        document.getElementById('success-msg').textContent = 'Cuenta creada correctamente. Redirigiendo...';
        setTimeout(() => window.location.href = '/login', 2000);
    } else if (response.status === 409) {
        document.getElementById('error-msg').style.display = 'block';
        document.getElementById('error-msg').textContent = 'Ya existe una cuenta con ese email';
    } else {
        document.getElementById('error-msg').style.display = 'block';
        document.getElementById('error-msg').textContent = 'Error al crear la cuenta';
    }
}