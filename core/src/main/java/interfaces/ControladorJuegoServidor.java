package interfaces;

import elementos.Direcciones;

public interface ControladorJuegoServidor {
    
    void iniciarJuego();
    
    void moverJugador(int numeroJugador, Direcciones direccion);

    void jugadorDesconectado(int numeroJugador);

    boolean estaJuegoIniciado();
}