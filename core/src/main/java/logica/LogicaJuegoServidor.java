package logica;

import elementos.*;
import interfaces.ControladorJuegoServidor;
import jugadores.JugadorServidor;
import red.HiloServidor;

import java.util.Timer;
import java.util.TimerTask;

/**
 * LogicaJuegoServidor - Lógica autoritativa del servidor
 * Controla TODO el estado del juego SIN dependencias de LibGDX
 */
public class LogicaJuegoServidor implements ControladorJuegoServidor {

    // CONSTANTES
    private final int TAMANIO_ELEMENTOS = 30;
    private final float VELOCIDAD_SERPIENTE = 120; // ms entre movimientos
    private final int NUM_JUGADORES = 2;

    // RED
    private HiloServidor hiloServidor;

    // JUGADORES
    private JugadorServidor[] jugadores;
    private float[] posicionesX;
    private float[] posicionesY;

    // ELEMENTOS
    private GestorFrutasServidor gestorFrutas;

    // ESTADO
    private boolean juegoIniciado = false;
    private boolean juegoTerminado = false;
    private Timer temporizadorJuego;

    public LogicaJuegoServidor() {
        // Inicializar servidor
        hiloServidor = new HiloServidor(this);
        hiloServidor.start();

        // Inicializar arrays
        jugadores = new JugadorServidor[NUM_JUGADORES];
        posicionesX = new float[NUM_JUGADORES];
        posicionesY = new float[NUM_JUGADORES];

        System.out.println("✅ Lógica del servidor inicializada");
        System.out.println("⏳ Esperando jugadores...");
    }

    @Override
    public void iniciarJuego() {
        System.out.println("🎮 Iniciando juego...");

        // Inicializar jugadores
        for (int i = 0; i < NUM_JUGADORES; i++) {
            float posX = (i == 0) ? -300 : 300; // Separar jugadores
            float posY = 0;
            posicionesX[i] = posX;
            posicionesY[i] = posY;

            SerpienteServidor serpiente = new SerpienteServidor(posX, posY, TAMANIO_ELEMENTOS, TAMANIO_ELEMENTOS);
            jugadores[i] = new JugadorServidor(i + 1, "Jugador " + (i + 1), serpiente);
        }

        // Inicializar frutas
        gestorFrutas = new GestorFrutasServidor(TAMANIO_ELEMENTOS);
        gestorFrutas.inicializarFrutas(jugadores[0].getSerpiente());

        // Enviar estado inicial
        enviarEstadoCompleto();

        // Iniciar loop del juego
        juegoIniciado = true;
        iniciarBucleJuego();

        System.out.println("✅ Juego iniciado exitosamente");
        System.out.println("🎯 Velocidad: " + VELOCIDAD_SERPIENTE + "ms por movimiento");
    }

    /**
     * Inicia el bucle principal del juego
     */
    private void iniciarBucleJuego() {
        temporizadorJuego = new Timer();
        temporizadorJuego.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (juegoIniciado && !juegoTerminado) {
                    actualizarJuego();
                }
            }
        }, 0, (long) VELOCIDAD_SERPIENTE);
        
        System.out.println("🔄 Bucle del juego iniciado");
    }

    /**
     * Actualiza la lógica del juego (llamado cada VELOCIDAD_SERPIENTE ms)
     */
    private void actualizarJuego() {
        // Mover serpientes
        for (int i = 0; i < NUM_JUGADORES; i++) {
            if (jugadores[i] != null) {
                moverSerpiente(i);
            }
        }

        // Verificar colisiones con frutas
        verificarColisionesFrutas();

        // Verificar colisiones entre serpientes
        verificarColisionesSerpientes();

        // Verificar invulnerabilidad
        for (JugadorServidor jugador : jugadores) {
            if (jugador != null) {
                jugador.actualizarInvulnerabilidad(VELOCIDAD_SERPIENTE / 1000f);
            }
        }

        // Enviar actualizaciones a clientes
        enviarActualizacionesSerpientes();

        // Verificar fin del juego
        verificarFinJuego();
    }

    /**
     * Mueve una serpiente según su dirección actual
     */
    private void moverSerpiente(int indice) {
        JugadorServidor jugador = jugadores[indice];
        Direcciones direccion = jugador.getDireccionActual();

        switch (direccion) {
            case ARRIBA:
                posicionesY[indice] += TAMANIO_ELEMENTOS;
                break;
            case ABAJO:
                posicionesY[indice] -= TAMANIO_ELEMENTOS;
                break;
            case DERECHA:
                posicionesX[indice] += TAMANIO_ELEMENTOS;
                break;
            case IZQUIERDA:
                posicionesX[indice] -= TAMANIO_ELEMENTOS;
                break;
            case NINGUNA:
                return; // No mover si no hay dirección
        }

        jugador.getSerpiente().mover(posicionesX[indice], posicionesY[indice]);
    }

    /**
     * Verifica colisiones con frutas
     */
    private void verificarColisionesFrutas() {
        for (int i = 0; i < NUM_JUGADORES; i++) {
            if (jugadores[i] != null) {
                FrutaServidor frutaColisionada = gestorFrutas.verificarColisiones(jugadores[i].getSerpiente());
                if (frutaColisionada != null) {
                    // Jugador comió fruta
                    jugadores[i].agregarPuntos(frutaColisionada.getPuntos());
                    jugadores[i].crecerSerpiente();

                    // Notificar a clientes
                    hiloServidor.enviarMensajeATodos("JugadorComio:" + (i + 1) + ":" + frutaColisionada.getPuntos());
                    System.out.println("🍎 Jugador " + (i + 1) + " comió fruta (+" + frutaColisionada.getPuntos() + " pts)");

                    // Reubicar fruta
                    gestorFrutas.reubicarFruta(frutaColisionada, jugadores[i].getSerpiente());
                    enviarActualizacionFrutas();
                    enviarActualizacionPuntuacion();
                }
            }
        }
    }

    /**
     * Verifica colisiones entre serpientes
     */
    private void verificarColisionesSerpientes() {
        for (int i = 0; i < NUM_JUGADORES; i++) {
            if (jugadores[i] == null || jugadores[i].isInvulnerable()) {
                continue;
            }

            SerpienteServidor serpienteActual = jugadores[i].getSerpiente();

            // Colisión consigo misma
            if (serpienteActual.colisionSerpiente()) {
                jugadorPierdeVida(i);
                continue;
            }

            // Colisión con otra serpiente
            for (int j = 0; j < NUM_JUGADORES; j++) {
                if (i != j && jugadores[j] != null) {
                    if (jugadores[j].getSerpiente().colisionConPosicion(
                            serpienteActual.getPosX(), serpienteActual.getPosY())) {
                        jugadorPierdeVida(i);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Maneja cuando un jugador pierde una vida
     */
    private void jugadorPierdeVida(int indice) {
        JugadorServidor jugador = jugadores[indice];
        boolean tieneVidas = jugador.perderVida();

        hiloServidor.enviarMensajeATodos("JugadorMurio:" + (indice + 1) + ":" + jugador.getVidas());

        if (!tieneVidas) {
            System.out.println("💀 Jugador " + (indice + 1) + " eliminado (sin vidas)");
        } else {
            System.out.println("⚠️ Jugador " + (indice + 1) + " perdió una vida (quedan " + jugador.getVidas() + ")");
            
            // Resetear posición
            float posX = (indice == 0) ? -300 : 300;
            float posY = 0;
            posicionesX[indice] = posX;
            posicionesY[indice] = posY;
            jugador.resetearPosicion(posX, posY, TAMANIO_ELEMENTOS, TAMANIO_ELEMENTOS);
        }
    }

    /**
     * Verifica si el juego ha terminado
     */
    private void verificarFinJuego() {
        int jugadoresVivos = 0;
        int ultimoVivo = -1;

        for (int i = 0; i < NUM_JUGADORES; i++) {
            if (jugadores[i] != null && jugadores[i].getVidas() > 0) {
                jugadoresVivos++;
                ultimoVivo = i;
            }
        }

        if (jugadoresVivos <= 1) {
            terminarJuego(ultimoVivo + 1);
        }
    }

    /**
     * Termina el juego
     */
    private void terminarJuego(int ganador) {
        juegoTerminado = true;
        if (temporizadorJuego != null) {
            temporizadorJuego.cancel();
        }

        hiloServidor.enviarMensajeATodos("JuegoTerminado:" + ganador);
        System.out.println("🏆 Juego terminado. Ganador: Jugador " + ganador);
        System.out.println("📊 Puntuación final:");
        for (int i = 0; i < NUM_JUGADORES; i++) {
            System.out.println("   Jugador " + (i + 1) + ": " + jugadores[i].getPuntuacion() + " puntos");
        }

        // Esperar 5 segundos y desconectar clientes
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                hiloServidor.desconectarClientes();
                juegoIniciado = false;
                System.out.println("⏳ Esperando nuevos jugadores...");
            }
        }, 5000);
    }

    // ===== MÉTODOS DE ENVÍO DE DATOS =====

    /**
     * Envía el estado completo del juego (al inicio)
     */
    private void enviarEstadoCompleto() {
        enviarActualizacionesSerpientes();
        enviarActualizacionFrutas();
        enviarActualizacionPuntuacion();
        System.out.println("📡 Estado inicial enviado a clientes");
    }

    /**
     * Envía las posiciones de todas las serpientes
     */
    private void enviarActualizacionesSerpientes() {
        for (int i = 0; i < NUM_JUGADORES; i++) {
            if (jugadores[i] != null) {
                String mensaje = construirMensajeSerpiente(i);
                hiloServidor.enviarMensajeATodos(mensaje);
            }
        }
    }

    /**
     * Construye el mensaje de actualización de una serpiente
     * Formato: ActualizarSerpiente:NUMERO:X:Y:SEGMENTOS
     * donde SEGMENTOS = x1:y1,x2:y2,x3:y3,...
     */
    private String construirMensajeSerpiente(int indice) {
        SerpienteServidor serpiente = jugadores[indice].getSerpiente();
        StringBuilder sb = new StringBuilder();

        sb.append("ActualizarSerpiente:");
        sb.append(indice + 1).append(":");
        sb.append((int) serpiente.getPosX()).append(":");
        sb.append((int) serpiente.getPosY()).append(":");
        sb.append(serpiente.serializar()); // Todos los segmentos

        return sb.toString();
    }

    /**
     * Envía las posiciones de todas las frutas
     */
    private void enviarActualizacionFrutas() {
        String mensaje = "ActualizarFrutas:" + gestorFrutas.serializar();
        hiloServidor.enviarMensajeATodos(mensaje);
    }

    /**
     * Envía la puntuación actual
     */
    private void enviarActualizacionPuntuacion() {
        StringBuilder sb = new StringBuilder("ActualizarPuntuacion:");
        for (int i = 0; i < NUM_JUGADORES; i++) {
            if (i > 0) sb.append(":");
            sb.append(jugadores[i] != null ? jugadores[i].getPuntuacion() : 0);
        }
        hiloServidor.enviarMensajeATodos(sb.toString());
    }

    // ===== IMPLEMENTACIÓN DE ControladorJuegoServidor =====

    @Override
    public void moverJugador(int numeroJugador, Direcciones direccion) {
        int indice = numeroJugador - 1;
        if (indice >= 0 && indice < NUM_JUGADORES && jugadores[indice] != null) {
            jugadores[indice].cambiarDireccion(direccion);
        }
    }

    @Override
    public void jugadorDesconectado(int numeroJugador) {
        System.out.println("⚠️ Jugador " + numeroJugador + " desconectado");
        // Terminar el juego si un jugador se desconecta
        terminarJuego((numeroJugador == 1) ? 2 : 1);
    }

    /**
     * Cierra el servidor
     */
    public void cerrar() {
        if (temporizadorJuego != null) {
            temporizadorJuego.cancel();
        }
        if (hiloServidor != null) {
            hiloServidor.terminar();
        }
        System.out.println("✅ Servidor cerrado correctamente");
    }
}