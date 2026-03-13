package com.escuelafutbol.ui.controller;

import com.escuelafutbol.commons.Constantes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador MVC para vistas del área de tutor.
 * <p>
 * EN: Serves the tutor panel view.
 * ES: Sirve la vista del panel de tutor.
 */
@Controller
@RequestMapping(Constantes.RUTA_TUTOR)
public class TutorController {

    /**
     * Muestra el panel principal de tutor.
     *
     * @return nombre de vista del panel de tutor
     */
    @GetMapping(Constantes.RUTA_PANEL)
    public String panel() {
        return Constantes.VISTA_TUTOR_PANEL;
    }
}
