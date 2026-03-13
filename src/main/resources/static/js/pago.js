/**
 * Vista de datos de transferencia para completar el pago.
 * @module pago
 */
const APP_CFG = window.AppConstants || {};
const PAGO_STORAGE = APP_CFG.storage || {};
const MSG = APP_CFG.mensajes || {};
const PAGO = APP_CFG.pago || {};
const PAGO_UI = APP_CFG.ui || {};

const nombre = sessionStorage.getItem(PAGO_STORAGE.pagoNombre) || PAGO_UI.dash;
const concepto = (sessionStorage.getItem(PAGO_STORAGE.pagoConcepto) || PAGO_UI.dash).toUpperCase();
const importe = sessionStorage.getItem(PAGO_STORAGE.pagoImporte) || PAGO_UI.dash;

/**
 * Pinta en pantalla nombre, concepto e importe a transferir.
 * @returns {void}
 */
function renderPago() {
    const nombreEl = document.getElementById('jugador-nombre');
    const conceptoEl = document.getElementById('pago-concepto');
    const importeEl = document.getElementById('pago-importe');

    if (nombreEl) nombreEl.textContent = nombre;
    if (conceptoEl) conceptoEl.textContent = concepto;
    if (importeEl) importeEl.textContent = importe + PAGO_UI.euro;
}

/**
 * Copia el IBAN del club al portapapeles.
 * @returns {void}
 */
function copiarIban() {
    navigator.clipboard.writeText(PAGO.iban).then(() => {
        alert(PAGO_UI.successPrefix + MSG.copiaIbanOk);
    });
}

/**
 * Copia el concepto de pago al portapapeles.
 * @returns {void}
 */
function copiarConcepto() {
    navigator.clipboard.writeText(concepto).then(() => {
        alert(PAGO_UI.successPrefix + MSG.copiaConceptoOk);
    });
}

document.addEventListener('DOMContentLoaded', () => {
    renderPago();

    const ibanBtn = document.querySelector('[data-action="copiar-iban"]');
    if (ibanBtn) {
        ibanBtn.addEventListener('click', copiarIban);
    }

    const conceptoBtn = document.querySelector('[data-action="copiar-concepto"]');
    if (conceptoBtn) {
        conceptoBtn.addEventListener('click', copiarConcepto);
    }
});
