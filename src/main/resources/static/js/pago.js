const nombre = sessionStorage.getItem('pago_nombre') || '—';
const concepto = sessionStorage.getItem('pago_concepto') || '—';
const importe = sessionStorage.getItem('pago_importe') || '—';

document.getElementById('jugador-nombre').textContent = nombre;
document.getElementById('pago-concepto').textContent = concepto;
document.getElementById('pago-importe').textContent = importe + '€';

function copiarIban() {
    navigator.clipboard.writeText('ES4221000579611300175282').then(() => {
        alert('✅ IBAN copiado al portapapeles');
    });
}

function copiarConcepto() {
    navigator.clipboard.writeText(concepto).then(() => {
        alert('✅ Concepto copiado al portapapeles');
    });
}