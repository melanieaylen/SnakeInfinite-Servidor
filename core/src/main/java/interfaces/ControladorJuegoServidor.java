package interfaces;

import elementos.Direcciones;

/**
 * Interfaz específica para el controlador del servidor
 * Tiene solo los métodos que el servidor realmente necesita
 */
public interface ControladorJuegoServidor {
    
    /**
     * Inicia el juego cuando hay suficientes jugadores
     */
    void iniciarJuego();
    
    /**
     * Mueve un jugador específico
     * @param numeroJugador Número del jugador (1 o 2)
     * @param direccion Dirección del movimiento
     */
    void moverJugador(int numeroJugador, Direcciones direccion);
    
    /**
     * Notifica que un jugador se desconectó
     * @param numeroJugador Número del jugador desconectado
     */
    void jugadorDesconectado(int numeroJugador);
}