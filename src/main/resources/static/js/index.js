/**
 * Script de portada para control del carrusel principal.
 * @module index
 */
const APP_CFG = window.AppConstants || {};
const UI = APP_CFG.ui || {};

let slideActual = 0;
const totalSlides = UI.carouselTotalSlides;

/**
 * Actualiza posicion visual y estado de dots del carrusel.
 * @returns {void}
 */
function actualizarCarrusel() {
    const slides = document.getElementById('carrusel-slides');
    if (!slides) return;

    slides.style.transform = `translateX(-${slideActual * 100}%)`;
    document.querySelectorAll('.carrusel-dot').forEach((d, i) => {
        d.classList.toggle('active', i === slideActual);
    });
}

/**
 * Mueve el carrusel en la direccion indicada.
 * @param {number} dir Desplazamiento relativo (-1 o 1).
 * @returns {void}
 */
function moverCarrusel(dir) {
    slideActual = (slideActual + dir + totalSlides) % totalSlides;
    actualizarCarrusel();
}

/**
 * Navega directamente al slide indicado.
 * @param {number} n Indice del slide destino.
 * @returns {void}
 */
function irASlide(n) {
    slideActual = n;
    actualizarCarrusel();
}

/**
 * Inicializa listeners del carrusel y arranca autoavance.
 * @returns {void}
 */
function initCarrusel() {
    const carrusel = document.getElementById('carrusel-slides');
    if (!carrusel) return;

    document.querySelectorAll('[data-action="mover-carrusel"]').forEach((btn) => {
        btn.addEventListener('click', () => {
            moverCarrusel(Number(btn.dataset.dir ?? 0));
        });
    });

    document.querySelectorAll('[data-action="ir-a-slide"]').forEach((dot) => {
        dot.addEventListener('click', () => {
            irASlide(Number(dot.dataset.slide ?? 0));
        });
    });

    actualizarCarrusel();
    setInterval(() => moverCarrusel(1), UI.carouselIntervalMs);
}

document.addEventListener('DOMContentLoaded', initCarrusel);
