const tokenExistente = localStorage.getItem('token');
if (tokenExistente) irACuenta();

async function login() {
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    if (!email || !password) {
        document.getElementById('error-msg').style.display = 'block';
        document.getElementById('error-msg').textContent = 'Rellena todos los campos';
        return;
    }

    const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    });

    if (response.ok) {
        const data = await response.json();
        localStorage.setItem('token', data.token);
        localStorage.setItem('rol', data.rol);
        localStorage.setItem('nombre', data.nombre);
        irACuenta();
    } else {
        document.getElementById('error-msg').style.display = 'block';
        document.getElementById('error-msg').textContent = 'Email o contraseña incorrectos';
    }
}