/**
 * Script de portada para control del carrusel principal.
 * @module index
 */
const APP_CFG = window.AppConstants || {};
const INDEX_UI = APP_CFG.ui || {};

let slideActual = 0;
const totalSlides = INDEX_UI.carouselTotalSlides;
let ropaSlideActual = 0;
let ropaAutoplayId = null;

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
    setInterval(() => moverCarrusel(1), INDEX_UI.carouselIntervalMs);
}

/**
 * Actualiza posicion y dots del carrusel de ropa.
 * @returns {void}
 */
function actualizarCarruselRopa() {
    const track = document.getElementById('ropa-carrusel-track');
    if (!track) return;

    const total = track.children.length;
    if (total === 0) return;

    if (ropaSlideActual < 0) {
        ropaSlideActual = total - 1;
    } else if (ropaSlideActual >= total) {
        ropaSlideActual = 0;
    }

    track.style.transform = `translateX(-${ropaSlideActual * 100}%)`;
    document.querySelectorAll('.ropa-dot').forEach((dot, index) => {
        dot.classList.toggle('active', index === ropaSlideActual);
    });
}

/**
 * Mueve carrusel de ropa en una direccion.
 * @param {number} dir Desplazamiento relativo.
 * @returns {void}
 */
function moverCarruselRopa(dir) {
    ropaSlideActual += dir;
    actualizarCarruselRopa();
}

/**
 * Navega a un slide concreto en carrusel de ropa.
 * @param {number} n Indice de slide.
 * @returns {void}
 */
function irASlideRopa(n) {
    ropaSlideActual = n;
    actualizarCarruselRopa();
}

/**
 * Inicializa interacciones del carrusel de ropa.
 * @returns {void}
 */
function initCarruselRopa() {
    const carruselRopa = document.getElementById('ropa-carrusel');
    if (!carruselRopa) return;

    const prevBtn = carruselRopa.querySelector('[data-action="ropa-prev"]');
    const nextBtn = carruselRopa.querySelector('[data-action="ropa-next"]');

    if (prevBtn) {
        prevBtn.addEventListener('click', () => moverCarruselRopa(-1));
    }
    if (nextBtn) {
        nextBtn.addEventListener('click', () => moverCarruselRopa(1));
    }

    carruselRopa.querySelectorAll('[data-action="ropa-go"]').forEach((dot) => {
        dot.addEventListener('click', () => irASlideRopa(Number(dot.dataset.slide ?? 0)));
    });

    const intervaloRopa = INDEX_UI.ropaCarouselIntervalMs || INDEX_UI.carouselIntervalMs;
    const iniciarAutoplayRopa = () => {
        if (ropaAutoplayId !== null) {
            return;
        }
        ropaAutoplayId = setInterval(() => moverCarruselRopa(1), intervaloRopa);
    };
    const pararAutoplayRopa = () => {
        if (ropaAutoplayId === null) {
            return;
        }
        clearInterval(ropaAutoplayId);
        ropaAutoplayId = null;
    };

    carruselRopa.addEventListener('mouseenter', pararAutoplayRopa);
    carruselRopa.addEventListener('mouseleave', iniciarAutoplayRopa);
    iniciarAutoplayRopa();

    actualizarCarruselRopa();
}

document.addEventListener('DOMContentLoaded', () => {
    initCarrusel();
    initCarruselRopa();
});
