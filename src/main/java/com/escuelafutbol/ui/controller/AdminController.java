package com.escuelafutbol.ui.controller;

import com.escuelafutbol.commons.Constantes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador MVC para vistas del área administrativa.
 * <p>
 * EN: Serves the administration panel view.
 * ES: Sirve la vista del panel de administración.
 */
@Controller
@RequestMapping(Constantes.RUTA_ADMIN)
public class AdminController {

    /**
     * Muestra el panel principal de administración.
     *
     * @return nombre de vista del panel admin
     */
    @GetMapping(Constantes.RUTA_PANEL)
    public String panel() {
        return Constantes.VISTA_ADMIN_PANEL;
    }
}
