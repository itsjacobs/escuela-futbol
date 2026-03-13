package com.escuelafutbol.ui.controller;

import com.escuelafutbol.commons.Constantes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador MVC para páginas públicas de la web.
 * <p>
 * EN: Serves public Thymeleaf views such as home and jobs page.
 * ES: Sirve vistas públicas Thymeleaf como inicio y trabaja con nosotros.
 */
@Controller
public class HomeController {

    private static final String PREFIJO_REDIRECT = "redirect:";

    /**
     * Muestra la página principal.
     *
     * @return nombre de vista de inicio
     */
    @GetMapping(Constantes.RUTA_ROOT)
    public String home() {
        return Constantes.VISTA_INDEX;
    }

    /**
     * Muestra la página de empleo/colaboración.
     *
     * @return nombre de vista de trabaja con nosotros
     */
    @GetMapping(Constantes.RUTA_TRABAJA)
    public String trabaja() {
        return Constantes.VISTA_TRABAJA;
    }

    /**
     * URL SEO-friendly de inscripción que redirige al flujo oficial.
     *
     * @return redirección a la ruta de inscripción principal
     */
    @GetMapping(Constantes.RUTA_INSCRIPCIONES)
    public String inscripciones() {
        return redirigirA(Constantes.RUTA_INSCRIPCION);
    }

    /**
     * URL SEO-friendly de equipo/categorías.
     *
     * @return redirección a la sección de categorías en home
     */
    @GetMapping(Constantes.RUTA_EQUIPO)
    public String equipo() {
        return redirigirA(Constantes.RUTA_ROOT + "#categorias");
    }

    /**
     * URL SEO-friendly para sección de entrenadores.
     *
     * @return redirección a la página pública relacionada
     */
    @GetMapping(Constantes.RUTA_ENTRENADORES)
    public String entrenadores() {
        return redirigirA(Constantes.RUTA_TRABAJA);
    }

    /**
     * URL SEO-friendly de contacto.
     *
     * @return redirección a la sección de contacto en home
     */
    @GetMapping(Constantes.RUTA_CONTACTO)
    public String contacto() {
        return redirigirA(Constantes.RUTA_ROOT + "#info");
    }

    /**
     * Construye una redirección MVC hacia una ruta interna.
     *
     * @param ruta destino relativo de la aplicación
     * @return instrucción de redirección para Spring MVC
     */
    private String redirigirA(String ruta) {
        return PREFIJO_REDIRECT + ruta;
    }
}
