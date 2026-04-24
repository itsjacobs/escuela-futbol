/**
 * Flujo de registro de nuevos tutores.
 * @module registro
 */
const APP_CFG = window.AppConstants || {};
const API = APP_CFG.api || {};
const REGISTRO_ROUTES = APP_CFG.routes || {};
const MSG = APP_CFG.mensajes || {};

/**
 * Envia datos de alta a la API y muestra feedback de exito/error.
 * @returns {Promise<void>} Promesa resuelta al finalizar el proceso.
 */
async function registro() {

    if (!document.getElementById('aceptaPrivacidad').checked) {
        mostrarError('Debes aceptar la política de privacidad para continuar');
        return;
    }
    const body = {
        nombre: document.getElementById('nombre').value,
        apellidos: document.getElementById('apellidos').value,
        email: document.getElementById('email').value,
        telefono: document.getElementById('telefono').value,
        password: document.getElementById('password').value
    };
    const response = await fetch(API.authRegistro, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });

    if (response.ok) {
        const sucEl = document.getElementById('success-msg');
        sucEl.classList.remove('is-hidden');
        sucEl.textContent = MSG.registroExito;
        setTimeout(() => window.location.href = REGISTRO_ROUTES.login, 2000);
    } else if (response.status === 409) {
        const errEl = document.getElementById('error-msg');
        errEl.classList.remove('is-hidden');
        errEl.textContent = MSG.registroDuplicado;
    } else {
        const errEl = document.getElementById('error-msg');
        errEl.classList.remove('is-hidden');
        errEl.textContent = MSG.registroError;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const btnRegistro = document.querySelector('[data-action="registro"]');
    if (btnRegistro) {
        btnRegistro.addEventListener('click', registro);
    }
});
