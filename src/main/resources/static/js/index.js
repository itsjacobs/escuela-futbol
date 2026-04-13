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
    initScrollReveal();
    initNavbarScroll();
    initStatCounters();
});

/**
 * Intersection Observer para animaciones de scroll reveal.
 * Elementos con clase .reveal, .reveal-left o .reveal-right
 * se hacen visibles al entrar en viewport.
 * @returns {void}
 */
function initScrollReveal() {
    const revealElements = document.querySelectorAll('.reveal, .reveal-left, .reveal-right');
    if (revealElements.length === 0) return;

    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.15, rootMargin: '0px 0px -40px 0px' });

    revealElements.forEach((el) => observer.observe(el));
}

/**
 * Aplica efecto glassmorphism al navbar al hacer scroll.
 * @returns {void}
 */
function initNavbarScroll() {
    const navbar = document.querySelector('.navbar');
    if (!navbar) return;

    const onScroll = () => {
        if (window.scrollY > 60) {
            navbar.classList.add('scrolled');
        } else {
            navbar.classList.remove('scrolled');
        }
    };

    window.addEventListener('scroll', onScroll, { passive: true });
    onScroll();
}

/**
 * Anima los numeros de las estadisticas al entrar en viewport.
 * @returns {void}
 */
function initStatCounters() {
    const statsStrip = document.querySelector('.stats-strip');
    if (!statsStrip) return;

    const statNums = statsStrip.querySelectorAll('.stat-num');
    let animated = false;

    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry) => {
            if (entry.isIntersecting && !animated) {
                animated = true;
                statNums.forEach((el) => animateValue(el));
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.3 });

    observer.observe(statsStrip);
}

/**
 * Anima el texto de un elemento desde 0 hasta su valor final.
 * Soporta numeros puros, rangos con guion y texto fijo.
 * @param {HTMLElement} el Elemento DOM con el valor a animar.
 * @returns {void}
 */
function animateValue(el) {
    const text = el.textContent.trim();
    const pureNum = parseInt(text, 10);

    if (text.includes('–') || text.includes('-')) {
        el.style.opacity = '0';
        el.style.transform = 'translateY(10px)';
        setTimeout(() => {
            el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            el.style.opacity = '1';
            el.style.transform = 'translateY(0)';
        }, 200);
        return;
    }

    if (isNaN(pureNum)) {
        el.style.opacity = '0';
        el.style.transform = 'translateY(10px)';
        setTimeout(() => {
            el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
            el.style.opacity = '1';
            el.style.transform = 'translateY(0)';
        }, 300);
        return;
    }

    const duration = 1200;
    const start = performance.now();
    const end = pureNum;

    el.textContent = '0';

    function step(now) {
        const elapsed = now - start;
        const progress = Math.min(elapsed / duration, 1);
        const eased = 1 - Math.pow(1 - progress, 3);
        const current = Math.round(eased * end);
        el.textContent = current;

        if (progress < 1) {
            requestAnimationFrame(step);
        } else {
            el.textContent = text;
        }
    }

    requestAnimationFrame(step);
}
