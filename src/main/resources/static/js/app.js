/**
 * Utilidades front comunes para autenticacion, navegacion y acciones globales.
 * @module app
 */
const APP = window.AppConstants || {};
const STORAGE = APP.storage || {};
const ROUTES = APP.routes || {};
const AUTH = APP.auth || {};
const HTTP = APP.http || {};
const UI = APP.ui || {};

/**
 * Obtiene el token JWT almacenado en sessionStorage.
 * @returns {string|null} Token actual o null si no existe.
 */
function getToken() {
    return sessionStorage.getItem(STORAGE.token);
}

/**
 * Obtiene el rol almacenado del usuario autenticado.
 * @returns {string|null} Rol del usuario o null.
 */
function getRol() {
    return sessionStorage.getItem(STORAGE.rol);
}

/**
 * Obtiene el nombre almacenado del usuario autenticado.
 * @returns {string|null} Nombre del usuario o null.
 */
function getNombre() {
    return sessionStorage.getItem(STORAGE.nombre);
}

/**
 * Comprueba si el token actual esta caducado o es invalido.
 * @returns {boolean} true si el token no es usable; false si sigue vigente.
 */
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

/**
 * Verifica sesion activa en paginas protegidas.
 * Si no hay token valido, cierra sesion.
 * @returns {void}
 */
function checkAuth() {
    if (!getToken() || isTokenExpirado()) {
        logout();
    }
}

/**
 * Verifica sesion y privilegios de administrador.
 * Si la validacion falla, cierra sesion.
 * @returns {void}
 */
function checkAdmin() {
    const rol = getRol();
    if (!getToken() || isTokenExpirado() || (rol !== AUTH.adminRolePrefixed && rol !== AUTH.adminRole)) {
        logout();
    }
}

/**
 * Ejecuta fetch autenticado con cabecera Authorization y control de 401.
 * @param {string} url URL de destino.
 * @param {RequestInit} [options={}] Opciones adicionales para fetch.
 * @returns {Promise<Response|null>} Respuesta HTTP o null si se invalida sesion.
 */
async function fetchConAuth(url, options = {}) {
    if (isTokenExpirado()) {
        logout();
        return null;
    }

    const response = await fetch(url, {
        ...options,
        headers: {
            ...options.headers,
            Authorization: AUTH.bearerPrefix + getToken()
        }
    });

    if (response.status === HTTP.unauthorized) {
        logout();
        return null;
    }

    return response;
}

/**
 * Limpia almacenamiento local y redirige a login.
 * @returns {void}
 */
function logout() {
    sessionStorage.clear();
    localStorage.clear();
    window.location.href = ROUTES.login;
}

/**
 * Redirige a panel segun rol cuando la sesion es valida.
 * @returns {void}
 */
function irACuenta() {
    const rol = getRol();
    if (getToken() && !isTokenExpirado()) {
        if (rol === AUTH.adminRolePrefixed || rol === AUTH.adminRole) {
            window.location.href = ROUTES.adminPanel;
        } else {
            window.location.href = ROUTES.tutorPanel;
        }
    } else {
        logout();
    }
}

/**
 * Redirige a inscripcion si hay sesion valida; en caso contrario a registro.
 * @returns {void}
 */
function irAInscripcion() {
    if (getToken() && !isTokenExpirado()) {
        window.location.href = ROUTES.inscripcion;
    } else {
        window.location.href = ROUTES.registro;
    }
}

/**
 * Actualiza el texto del enlace de cuenta en la navbar.
 * @returns {void}
 */
function actualizarNavbar() {
    const nombre = getNombre();
    const token = getToken();
    const navLinks = document.querySelector('.navbar-links');

    if (token && !isTokenExpirado() && nombre && navLinks) {
        const enlaceCuenta = navLinks.querySelector('[data-action="ir-a-cuenta"]');
        if (enlaceCuenta) {
            enlaceCuenta.textContent = UI.userPrefix + nombre;
        }
    }
}

/**
 * Vincula acciones globales declaradas con data-action.
 * @returns {void}
 */
function bindCommonActions() {
    document.addEventListener('click', (event) => {
        const actionElement = event.target.closest('[data-action]');
        if (!actionElement) {
            return;
        }

        const action = actionElement.dataset.action;
        if (action === 'ir-a-cuenta') {
            event.preventDefault();
            irACuenta();
        } else if (action === 'ir-a-inscripcion') {
            event.preventDefault();
            irAInscripcion();
        } else if (action === 'logout') {
            event.preventDefault();
            logout();
        }
    });
}

/**
 * Inicializa botones de visibilidad de contraseña marcados con data-password-target.
 * @returns {void}
 */
function initPasswordToggles() {
    document.querySelectorAll('[data-password-target]').forEach((button) => {
        const targetId = button.dataset.passwordTarget;
        const input = document.getElementById(targetId);
        if (!input) {
            return;
        }

        // Ensure correct initial state: eye-open visible ("click to show"), eye-closed hidden
        const eyeOpenInit = button.querySelector('.eye-open');
        const eyeClosedInit = button.querySelector('.eye-closed');
        if (eyeOpenInit && eyeClosedInit) {
            eyeOpenInit.classList.remove('is-hidden');
            eyeClosedInit.classList.add('is-hidden');
        }

        button.addEventListener('click', () => {
            const esOculto = input.type === 'password';
            input.type = esOculto ? 'text' : 'password';
            button.setAttribute('aria-pressed', String(esOculto));

            const eyeOpen = button.querySelector('.eye-open');
            const eyeClosed = button.querySelector('.eye-closed');
            if (eyeOpen && eyeClosed) {
                // When showing password: show crossed eye ("click to hide")
                // When hiding password: show open eye ("click to show")
                eyeOpen.classList.toggle('is-hidden', esOculto);
                eyeClosed.classList.toggle('is-hidden', !esOculto);
            }

            const labelShow = button.dataset.labelShow || 'Mostrar contrasena';
            const labelHide = button.dataset.labelHide || 'Ocultar contrasena';
            button.setAttribute('aria-label', esOculto ? labelHide : labelShow);
        });
    });
}

/**
 * Crea el boton de toggle de tema oscuro y aplica preferencia guardada.
 * @returns {void}
 */
function initDarkMode() {
    // Light theme is the default. Save preference in sessionStorage
    // so it forgets the dark mode once the user closes the tab/window.
    const saved = sessionStorage.getItem('darkMode');

    if (saved === 'true') {
        document.body.classList.add('dark-mode');
    } else {
        document.body.classList.remove('dark-mode');
    }

    const btn = document.createElement('button');
    btn.className = 'theme-toggle';
    btn.setAttribute('aria-label', 'Cambiar tema');
    btn.innerHTML = document.body.classList.contains('dark-mode') ? '☀️' : '🌙';
    btn.addEventListener('click', () => {
        document.body.classList.toggle('dark-mode');
        const isDark = document.body.classList.contains('dark-mode');
        sessionStorage.setItem('darkMode', String(isDark));
        btn.innerHTML = isDark ? '☀️' : '🌙';
    });

    document.body.appendChild(btn);
}

document.addEventListener('DOMContentLoaded', () => {
    actualizarNavbar();
    bindCommonActions();
    initPasswordToggles();
    initDarkMode();
});
