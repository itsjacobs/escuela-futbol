package com.escuelafutbol.ui.controller;

import com.escuelafutbol.commons.Constantes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador MVC para vistas de autenticación y flujo de inscripción.
 * <p>
 * EN: Serves login, register, enrollment and payment pages.
 * ES: Sirve las vistas de login, registro, inscripción y pago.
 */
@Controller
public class AuthController {

    /**
     * Muestra la vista de inicio de sesión.
     *
     * @return nombre de plantilla de login
     */
    @GetMapping(Constantes.RUTA_LOGIN)
    public String login() {
        return Constantes.VISTA_AUTH_LOGIN;
    }

    /**
     * Muestra la vista de registro de tutor.
     *
     * @return nombre de plantilla de registro
     */
    @GetMapping(Constantes.RUTA_REGISTRO)
    public String registro() {
        return Constantes.VISTA_AUTH_REGISTRO;
    }

    /**
     * Muestra la vista de formulario de inscripción de jugador.
     *
     * @return nombre de plantilla de inscripción
     */
    @GetMapping(Constantes.RUTA_INSCRIPCION)
    public String inscripcion() {
        return Constantes.VISTA_INSCRIPCION;
    }

    /**
     * Muestra la vista de instrucciones de pago.
     *
     * @return nombre de plantilla de pago
     */
    @GetMapping(Constantes.RUTA_PAGO)
    public String pago() {
        return Constantes.VISTA_PAGO;
    }
}
