/**
 * Vista de datos de transferencia para completar el pago.
 * @module pago
 */
const APP_CFG = window.AppConstants || {};
const PAGO_STORAGE = APP_CFG.storage || {};
const MSG = APP_CFG.mensajes || {};
const PAGO = APP_CFG.pago || {};
const UI = APP_CFG.ui || {};

const nombre = sessionStorage.getItem(PAGO_STORAGE.pagoNombre) || UI.dash;
const concepto = (sessionStorage.getItem(PAGO_STORAGE.pagoConcepto) || UI.dash).toUpperCase();
const importe = sessionStorage.getItem(PAGO_STORAGE.pagoImporte) || UI.dash;

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
    if (importeEl) importeEl.textContent = importe + UI.euro;
}

/**
 * Copia el IBAN del club al portapapeles.
 * @returns {void}
 */
function copiarIban() {
    navigator.clipboard.writeText(PAGO.iban).then(() => {
        alert(UI.successPrefix + MSG.copiaIbanOk);
    });
}

/**
 * Copia el concepto de pago al portapapeles.
 * @returns {void}
 */
function copiarConcepto() {
    navigator.clipboard.writeText(concepto).then(() => {
        alert(UI.successPrefix + MSG.copiaConceptoOk);
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
