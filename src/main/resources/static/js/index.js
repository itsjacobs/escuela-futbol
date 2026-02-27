let slideActual = 0;
const totalSlides = 3;

function actualizarCarrusel() {
    document.getElementById('carrusel-slides').style.transform = `translateX(-${slideActual * 100}%)`;
    document.querySelectorAll('.carrusel-dot').forEach((d, i) => {
        d.classList.toggle('active', i === slideActual);
    });
}

function moverCarrusel(dir) {
    slideActual = (slideActual + dir + totalSlides) % totalSlides;
    actualizarCarrusel();
}

function irASlide(n) {
    slideActual = n;
    actualizarCarrusel();
}

setInterval(() => moverCarrusel(1), 5000);