/**
 * Flujo de login para autenticar usuario y persistir sesion.
 * @module login
 */
const APP_CFG = window.AppConstants || {};
const LOGIN_STORAGE = APP_CFG.storage || {};
const API = APP_CFG.api || {};
const MSG = APP_CFG.mensajes || {};

const tokenExistente = sessionStorage.getItem(LOGIN_STORAGE.token);
if (tokenExistente) irACuenta();

/**
 * Ejecuta autenticacion contra API y guarda token/datos de sesion.
 * @returns {Promise<void>} Promesa resuelta al finalizar login o mostrar error.
 */
async function login() {
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    if (!email || !password) {
        const errEl = document.getElementById('error-msg');
        errEl.classList.remove('is-hidden');
        errEl.textContent = MSG.errorCamposObligatorios;
        return;
    }

    const response = await fetch(API.authLogin, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    });

    if (response.ok) {
        const data = await response.json();
        sessionStorage.setItem(LOGIN_STORAGE.token, data.token);
        sessionStorage.setItem(LOGIN_STORAGE.rol, data.rol);
        sessionStorage.setItem(LOGIN_STORAGE.nombre, data.nombre);
        irACuenta();
    } else {
        const errEl = document.getElementById('error-msg');
        errEl.classList.remove('is-hidden');
        errEl.textContent = MSG.errorLogin;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const btnLogin = document.querySelector('[data-action="login"]');
    if (btnLogin) {
        btnLogin.addEventListener('click', login);
    }
});
