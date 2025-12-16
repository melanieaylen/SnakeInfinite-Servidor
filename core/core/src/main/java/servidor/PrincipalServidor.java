package servidor;

import logica.LogicaJuegoServidor;

import java.util.Scanner;

public class PrincipalServidor {

    public static void main(String[] args) {
        System.out.println("===================================");
        System.out.println("  SNAKE INFINITE - SERVIDOR");
        System.out.println("  MODO: 2 JUGADORES"); // ✅ CAMBIO
        System.out.println("===================================");
        System.out.println();

        // Crear instancia del servidor
        LogicaJuegoServidor servidor = new LogicaJuegoServidor();

        System.out.println();
        System.out.println("Servidor ejecutandose...");
        System.out.println("Escribe 'salir' para cerrar el servidor");
        System.out.println();

        // Mantener el servidor activo hasta que el usuario escriba "salir"
        Scanner escaner = new Scanner(System.in);
        String entrada;

        do {
            entrada = escaner.nextLine().trim().toLowerCase();

            if (entrada.equals("estado")) {
                // Mostrar estado del servidor
                System.out.println("Estado del servidor");
            } else if (entrada.equals("ayuda")) {
                System.out.println("Comandos disponibles:");
                System.out.println("  estado - Mostrar estado del servidor");
                System.out.println("  ayuda  - Mostrar esta ayuda");
                System.out.println("  salir  - Cerrar el servidor");
            } else if (!entrada.equals("salir") && !entrada.isEmpty()) {
                System.out.println("Comando desconocido. Escribe 'ayuda' para ver comandos disponibles.");
            }
        } while (!entrada.equals("salir"));

        // Cerrar servidor
        System.out.println();
        System.out.println("Cerrando servidor...");
        servidor.cerrar();

        escaner.close();
        System.out.println("Servidor cerrado exitosamente");
        System.exit(0);
    }
}