function irACuenta() {
    const token = localStorage.getItem('token');
    const rol = localStorage.getItem('rol');
    if (token) {
        if (rol === 'ROLE_ADMIN') {
            window.location.href = '/admin/panel';
        } else {
            window.location.href = '/tutor/panel';
        }
    } else {
        window.location.href = '/login';
    }
}

function logout() {
    const token = localStorage.getItem('token');
    fetch('/api/auth/logout', {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token }
    });
    localStorage.clear();
    window.location.href = '/';
}

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
    const token = localStorage.getItem('token');
    if (!token) window.location.href = '/login';
}

function checkAdmin() {
    const token = localStorage.getItem('token');
    const rol = localStorage.getItem('rol');
    if (!token || rol !== 'ROLE_ADMIN') window.location.href = '/login';
}