package interfaces;

import elementos.Direcciones;

/**
 * Interfaz para el controlador del servidor
 */
public interface ControladorJuegoServidor {
    
    /**
     * Inicia el juego cuando hay suficientes jugadores
     */
    void iniciarJuego();
    
    /**
     * Mueve un jugador específico
     * @param numeroJugador Número del jugador (1-4)
     * @param direccion Dirección del movimiento
     */
    void moverJugador(int numeroJugador, Direcciones direccion);
    
    /**
     * Notifica que un jugador se desconectó
     * @param numeroJugador Número del jugador desconectado
     */
    void jugadorDesconectado(int numeroJugador);
    
    /**
     * ✅ NUEVO: Verifica si el juego ya está iniciado
     * @return true si el juego está en curso
     */
    boolean estaJuegoIniciado();
}