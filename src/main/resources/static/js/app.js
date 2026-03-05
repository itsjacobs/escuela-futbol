function getToken() {
    return localStorage.getItem('token');
}

function getRol() {
    return localStorage.getItem('rol');
}

function getNombre() {
    return localStorage.getItem('nombre');
}

function isTokenExpirado() {
    const token = getToken();
    if (!token) return true;
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return payload.exp * 1000 < Date.now();
    } catch (e) {
        return true;
    }
}

function checkAuth() {
    if (!getToken() || isTokenExpirado()) {
        logout();
        return;
    }
}

function checkAdmin() {
    const rol = getRol();
    if (!getToken() || isTokenExpirado() || (rol !== 'ROLE_ADMIN' && rol !== 'ADMIN')) {
        logout();
        return;
    }
}

// Wrapper fetch con manejo automático de 401
async function fetchConAuth(url, options = {}) {
    if (isTokenExpirado()) {
        logout();
        return null;
    }

    const response = await fetch(url, {
        ...options,
        headers: {
            ...options.headers,
            'Authorization': 'Bearer ' + getToken()
        }
    });

    if (response.status === 401) {
        logout();
        return null;
    }

    return response;
}

function logout() {
    localStorage.clear();
    window.location.href = '/login';
}

function irACuenta() {
    const rol = getRol();
    if (getToken() && !isTokenExpirado()) {
        if (rol === 'ROLE_ADMIN' || rol === 'ADMIN') {
            window.location.href = '/admin/panel';
        } else {
            window.location.href = '/tutor/panel';
        }
    } else {
        logout();
    }
}

function irAInscripcion() {
    if (getToken() && !isTokenExpirado()) {
        window.location.href = '/inscripcion';
    } else {
        window.location.href = '/registro';
    }
}

function actualizarNavbar() {
    const nombre = getNombre();
    const token = getToken();
    const navLinks = document.querySelector('.navbar-links');

    if (token && !isTokenExpirado() && nombre && navLinks) {
        const enlaceCuenta = navLinks.querySelector('a[onclick="irACuenta()"]');
        if (enlaceCuenta) {
            enlaceCuenta.textContent = '👤 ' + nombre;
        }
    }
}

document.addEventListener('DOMContentLoaded', actualizarNavbar);