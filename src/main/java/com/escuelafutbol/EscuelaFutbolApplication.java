package com.escuelafutbol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal de la aplicación Spring Boot.
 * <p>
 * EN: Main bootstrap class for the Escuela Futbol application.
 * ES: Clase de arranque principal para la aplicación Escuela Futbol.
 */
@SpringBootApplication
public class EscuelaFutbolApplication {
	/**
	 * Inicializa el contexto de Spring y arranca la aplicación.
	 *
	 * @param args argumentos de línea de comandos
	 */
	public static void main(String[] args) {
		SpringApplication.run(EscuelaFutbolApplication.class, args);
	}
}
