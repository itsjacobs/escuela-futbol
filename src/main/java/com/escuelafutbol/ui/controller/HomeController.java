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
}
