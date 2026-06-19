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

function initFAQ() {
    document.querySelectorAll('[data-action="faq-toggle"]').forEach((btn) => {
        btn.addEventListener('click', () => {
            const item = btn.closest('.faq-item');
            const estaAbierto = item.classList.contains('faq-abierto');
            document.querySelectorAll('.faq-item').forEach((i) => i.classList.remove('faq-abierto'));
            if (!estaAbierto) {
                item.classList.add('faq-abierto');
            }
        });
    });
}

function initCountUp() {
    const stats = document.querySelectorAll('.stat-numero');
    if (!stats.length || !('IntersectionObserver' in window)) return;

    const animate = (el) => {
        const text = el.textContent.trim();
        if (text.includes('-')) return;
        const prefix = text.startsWith('+') ? '+' : '';
        const suffix = text.endsWith('%') ? '%' : '';
        const value = parseInt(text.replace(/\D/g, ''), 10);
        if (isNaN(value) || value === 0) return;

        const duration = 1400;
        const step = 16;
        let elapsed = 0;
        const timer = setInterval(() => {
            elapsed += step;
            const progress = Math.min(elapsed / duration, 1);
            const eased = 1 - Math.pow(1 - progress, 3);
            el.textContent = prefix + Math.floor(eased * value) + suffix;
            if (progress >= 1) {
                el.textContent = prefix + value + suffix;
                clearInterval(timer);
            }
        }, step);
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                animate(entry.target);
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.6 });

    stats.forEach(el => observer.observe(el));
}

function initLightbox() {
    const lightbox = document.getElementById('lightbox-calendario');
    const lightboxImg = document.getElementById('lightbox-img');
    if (!lightbox || !lightboxImg) return;

    document.addEventListener('click', (e) => {
        const el = e.target.closest('[data-action]');
        if (!el) return;

        if (el.dataset.action === 'abrir-calendario') {
            lightboxImg.src = el.src;
            lightbox.classList.add('lightbox-abierto');
            document.body.style.overflow = 'hidden';
        } else if (el.dataset.action === 'cerrar-calendario') {
            lightbox.classList.remove('lightbox-abierto');
            document.body.style.overflow = '';
        }
    });

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            lightbox.classList.remove('lightbox-abierto');
            document.body.style.overflow = '';
        }
    });

    lightboxImg.addEventListener('click', (e) => e.stopPropagation());
}

document.addEventListener('DOMContentLoaded', () => {
    initCarrusel();
    initCarruselRopa();
    initFAQ();
    initCountUp();
    initLightbox();
});
