function getToken() {
    return localStorage.getItem('token');
}

function getRol() {
    return localStorage.getItem('rol');
}

function getNombre() {
    return localStorage.getItem('nombre');
}

function checkAuth() {
    const token = getToken();
    if (!token) window.location.href = '/login';
}

function checkAdmin() {
    const token = getToken();
    const rol = getRol();
    if (!token || (rol !== 'ROLE_ADMIN' && rol !== 'ADMIN')) {
        window.location.href = '/login';
    }
}

function irACuenta() {
    const token = getToken();
    const rol = getRol();
    if (token) {
        if (rol === 'ROLE_ADMIN' || rol === 'ADMIN') {
            window.location.href = '/admin/panel';
        } else {
            window.location.href = '/tutor/panel';
        }
    } else {
        window.location.href = '/login';
    }
}

function irAInscripcion() {
    const token = getToken();
    if (token) {
        window.location.href = '/inscripcion';
    } else {
        window.location.href = '/registro';
    }
}

function logout() {
    const token = getToken();
    fetch('/api/auth/logout', {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token }
    });
    localStorage.clear();
    window.location.href = '/';
}

function actualizarNavbar() {
    const nombre = getNombre();
    const token = getToken();
    const navLinks = document.querySelector('.navbar-links');

    if (token && nombre && navLinks) {
        const enlaceCuenta = navLinks.querySelector('a[onclick="irACuenta()"]');
        if (enlaceCuenta) {
            enlaceCuenta.textContent = '👤 ' + nombre;
        }
    }
}

document.addEventListener('DOMContentLoaded', actualizarNavbar);