package logica;

import elementos.*;
import interfaces.ControladorJuegoServidor;
import jugadores.JugadorServidor;
import red.HiloServidor;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class LogicaJuegoServidor implements ControladorJuegoServidor {

	private final int TAMANIO_ELEMENTOS = 30;
	private final float VELOCIDAD_SERPIENTE = 120;
	private final int MAX_JUGADORES = 2;
	private final int TIEMPO_ESPERA_INICIO = 5000;

	private HiloServidor hiloServidor;
	private JugadorServidor[] jugadores;
	private float[] posicionesX;
	private float[] posicionesY;
	private GestorFrutasServidor gestorFrutas;

	private boolean juegoIniciado = false;
	private boolean juegoTerminado = false;
	private Timer temporizadorJuego;
	private Timer temporizadorEspera;
	private int numeroJugadoresActivos = 0;

	private int contadorActualizaciones = 0;

	public LogicaJuegoServidor() {
		hiloServidor = new HiloServidor(this);
		hiloServidor.start();

		jugadores = new JugadorServidor[MAX_JUGADORES];
		posicionesX = new float[MAX_JUGADORES];
		posicionesY = new float[MAX_JUGADORES];

		System.out.println("Logica del servidor inicializada");
		System.out.println("Esperando jugadores (se requieren 2 jugadores)...");
	}

	public void programarInicioJuego() {
		if (temporizadorEspera != null) {
			temporizadorEspera.cancel();
		}

		int jugadoresConectados = hiloServidor.obtenerClientesConectados();

		System.out.println("===================================");
		System.out.println("HAY " + jugadoresConectados + " JUGADORES CONECTADOS");
		System.out.println("ESPERANDO " + (TIEMPO_ESPERA_INICIO / 1000) + " SEGUNDOS...");
		System.out.println("===================================");

		temporizadorEspera = new Timer();
		temporizadorEspera.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!juegoIniciado && !juegoTerminado && hiloServidor.obtenerClientesConectados() >= 2) {
					iniciarJuego();
				}
			}
		}, TIEMPO_ESPERA_INICIO);
	}

	@Override
	public void iniciarJuego() {
		if (juegoIniciado) {
			System.out.println("El juego ya esta iniciado");
			return;
		}

		if (temporizadorEspera != null) {
			temporizadorEspera.cancel();
			temporizadorEspera = null;
		}

		System.out.println("===================================");
		System.out.println("INICIANDO JUEGO...");

		numeroJugadoresActivos = hiloServidor.obtenerClientesConectados();
		System.out.println("Jugadores activos: " + numeroJugadoresActivos);

		if (numeroJugadoresActivos < 2) {
			System.err.println("ERROR: Se requieren 2 jugadores");
			return;
		}

		float[] posicionesIniciales = calcularPosicionesIniciales();

		System.out.println("Inicializando jugadores:");
		for (int i = 0; i < numeroJugadoresActivos; i++) {
			float posX = posicionesIniciales[i * 2];
			float posY = posicionesIniciales[i * 2 + 1];
			posicionesX[i] = posX;
			posicionesY[i] = posY;

			String nombreJugador = hiloServidor.obtenerNombreJugador(i + 1);
			System.out.println("   " + (i + 1) + ". " + nombreJugador + " en (" + posX + ", " + posY + ")");

			SerpienteServidor serpiente = new SerpienteServidor(posX, posY, TAMANIO_ELEMENTOS, TAMANIO_ELEMENTOS);
			jugadores[i] = new JugadorServidor(i + 1, nombreJugador, serpiente);
		}

		System.out.println("Inicializando frutas...");
		gestorFrutas = new GestorFrutasServidor(TAMANIO_ELEMENTOS);
		gestorFrutas.inicializarFrutasConJugadores(jugadores, numeroJugadoresActivos);

		juegoIniciado = true;
		juegoTerminado = false;
		contadorActualizaciones = 0;

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Enviando mensaje 'Iniciar' a todos los clientes...");
		hiloServidor.enviarMensajeATodos("Iniciar");

		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Enviando estado inicial...");
		enviarEstadoCompleto();

		iniciarBucleJuego();

		System.out.println("JUEGO INICIADO");
		System.out.println("===================================");
	}

	private float[] calcularPosicionesIniciales() {
		float[] posiciones = new float[4];
		
		posiciones[0] = -300;
		posiciones[1] = 0;
		
		posiciones[2] = 300;
		posiciones[3] = 0;

		return posiciones;
	}

	private void iniciarBucleJuego() {
		if (temporizadorJuego != null) {
			temporizadorJuego.cancel();
		}

		temporizadorJuego = new Timer();
		temporizadorJuego.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (juegoIniciado && !juegoTerminado) {
					actualizarJuego();
				}
			}
		}, 0, (long) VELOCIDAD_SERPIENTE);
	}

	private void actualizarJuego() {
		contadorActualizaciones++;

		if (contadorActualizaciones % 50 == 0) {
			System.out.println("\nActualizacion #" + contadorActualizaciones);
			System.out.println("Jugadores activos: " + numeroJugadoresActivos);
			for (int i = 0; i < numeroJugadoresActivos; i++) {
				if (jugadores[i] != null) {
					System.out.println("   J" + (i + 1) + " (" + jugadores[i].getNombre() + "): " + "Pos("
							+ jugadores[i].getSerpiente().getPosX() + ", " + jugadores[i].getSerpiente().getPosY()
							+ ") " + "Dir=" + jugadores[i].getDireccionActual() + " " + "Size="
							+ jugadores[i].getSerpiente().getTamanioActual() + " " + "Vidas=" + jugadores[i].getVidas()
							+ " " + "Pts=" + jugadores[i].getPuntuacion());
				}
			}
		}

		// Mover todas las serpientes
		for (int i = 0; i < numeroJugadoresActivos; i++) {
			if (jugadores[i] != null && jugadores[i].getVidas() > 0) {
				moverSerpiente(i);
			}
		}

		// Verificar colisiones
		verificarColisionesFrutas();
		verificarColisionesSerpientes(); // ✅ CORREGIDO

		// Actualizar invulnerabilidad
		for (int i = 0; i < numeroJugadoresActivos; i++) {
			if (jugadores[i] != null) {
				jugadores[i].actualizarInvulnerabilidad(VELOCIDAD_SERPIENTE / 1000f);
			}
		}

		// Enviar actualizaciones a clientes
		enviarActualizacionesSerpientes();
		verificarFinJuego();
	}

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
			return;
		}

		jugador.getSerpiente().mover(posicionesX[indice], posicionesY[indice]);
	}

	private void verificarColisionesFrutas() {
		for (int i = 0; i < numeroJugadoresActivos; i++) {
			if (jugadores[i] == null) {
				System.err.println("ERROR: jugadores[" + i + "] es NULL");
				continue;
			}

			if (jugadores[i].getVidas() <= 0) {
				continue;
			}

			SerpienteServidor serpiente = jugadores[i].getSerpiente();
			if (serpiente == null) {
				System.err.println("ERROR: Serpiente de jugador " + (i + 1) + " es NULL");
				continue;
			}

			FrutaServidor frutaColisionada = gestorFrutas.verificarColisiones(serpiente);
			if (frutaColisionada != null) {
				int puntosGanados = frutaColisionada.getPuntos();
				jugadores[i].agregarPuntos(puntosGanados);
				jugadores[i].crecerSerpiente();

				System.out.println(
						"J" + (i + 1) + " (" + jugadores[i].getNombre() + ") comio " + frutaColisionada.getTipo()
								+ " (+" + puntosGanados + " pts) " + "Total: " + jugadores[i].getPuntuacion());

				hiloServidor.enviarMensajeATodos("JugadorComio:" + (i + 1) + ":" + puntosGanados);

				gestorFrutas.reubicarFrutaConJugadores(frutaColisionada, jugadores, numeroJugadoresActivos);
				enviarActualizacionFrutas();
				enviarActualizacionPuntuacion();
			}
		}
	}

	// ✅ CORREGIDO: Sistema de colisiones completamente refactorizado
	private void verificarColisionesSerpientes() {
		// Set para rastrear qué jugadores deben perder vida este frame
		Set<Integer> jugadoresQuePerderanVida = new HashSet<>();
		
		// 1. Primero detectar TODAS las colisiones
		for (int i = 0; i < numeroJugadoresActivos; i++) {
			if (jugadores[i] == null || jugadores[i].isInvulnerable() || jugadores[i].getVidas() <= 0) {
				continue;
			}

			SerpienteServidor serpienteActual = jugadores[i].getSerpiente();
			if (serpienteActual == null) {
				continue;
			}

			// A. Colisión consigo mismo
			if (serpienteActual.colisionSerpiente()) {
				System.out.println("J" + (i + 1) + " (" + jugadores[i].getNombre() + ") choco consigo mismo");
				jugadoresQuePerderanVida.add(i);
				continue; // No verificar más colisiones para este jugador
			}

			// B. Colisión con otros jugadores
			for (int j = 0; j < numeroJugadoresActivos; j++) {
				if (i == j) continue; // No colisionar consigo mismo
				
				if (jugadores[j] == null || jugadores[j].getVidas() <= 0) {
					continue;
				}

				SerpienteServidor otraSerpiente = jugadores[j].getSerpiente();
				if (otraSerpiente == null) {
					continue;
				}

				// Verificar si la cabeza del jugador i choca con cualquier parte del jugador j
				if (otraSerpiente.colisionConPosicion(serpienteActual.getPosX(), serpienteActual.getPosY())) {
					System.out.println("J" + (i + 1) + " (" + jugadores[i].getNombre() + ") " + 
					                 "choco con J" + (j + 1) + " (" + jugadores[j].getNombre() + ")");
					jugadoresQuePerderanVida.add(i);
					break; // Ya sabemos que este jugador perderá vida
				}
			}
		}
		
		// 2. Ahora aplicar TODAS las pérdidas de vida detectadas
		for (Integer indice : jugadoresQuePerderanVida) {
			jugadorPierdeVida(indice);
		}
	}

	private void jugadorPierdeVida(int indice) {
		JugadorServidor jugador = jugadores[indice];
		boolean tieneVidas = jugador.perderVida();

		hiloServidor.enviarMensajeATodos("JugadorMurio:" + (indice + 1) + ":" + jugador.getVidas());

		if (!tieneVidas) {
			System.out.println("J" + (indice + 1) + " (" + jugador.getNombre() + ") ELIMINADO");
		} else {
			System.out.println("J" + (indice + 1) + " (" + jugador.getNombre() + ") " + "perdio vida (quedan "
					+ jugador.getVidas() + ")");

			// Resetear posición
			float[] posiciones = calcularPosicionesIniciales();
			float posX = posiciones[indice * 2];
			float posY = posiciones[indice * 2 + 1];
			posicionesX[indice] = posX;
			posicionesY[indice] = posY;
			jugador.resetearPosicion(posX, posY, TAMANIO_ELEMENTOS, TAMANIO_ELEMENTOS);

			System.out.println("J" + (indice + 1) + " reseteado a (" + posX + ", " + posY + ")");
		}
	}

	private void verificarFinJuego() {
		int jugadoresVivos = 0;
		int ultimoVivo = -1;
		int maxPuntos = -1;
		int ganadorPorPuntos = -1;

		// Contar jugadores vivos y encontrar al de mayor puntuación
		for (int i = 0; i < numeroJugadoresActivos; i++) {
			if (jugadores[i] != null && jugadores[i].getVidas() > 0) {
				jugadoresVivos++;
				ultimoVivo = i;
			}
			
			// Buscar al de mayor puntuación (incluyendo muertos)
			if (jugadores[i] != null) {
				if (jugadores[i].getPuntuacion() > maxPuntos) {
					maxPuntos = jugadores[i].getPuntuacion();
					ganadorPorPuntos = i;
				}
			}
		}

		// ✅ CORREGIDO: Sistema de victoria mejorado
		if (jugadoresVivos <= 1 && numeroJugadoresActivos > 1) {
			int ganador;
			
			if (jugadoresVivos == 1) {
				// Hay un único sobreviviente
				ganador = ultimoVivo + 1;
				System.out.println("Ganador por supervivencia: J" + ganador);
			} else if (jugadoresVivos == 0) {
				// Empate por muerte simultánea - gana quien tenga más puntos
				System.out.println("¡EMPATE! Determinando ganador por puntuación...");
				
				// Verificar si hay empate en puntos también
				int contadorMaxPuntos = 0;
				for (int i = 0; i < numeroJugadoresActivos; i++) {
					if (jugadores[i] != null && jugadores[i].getPuntuacion() == maxPuntos) {
						contadorMaxPuntos++;
						ganadorPorPuntos = i; // Último con max puntos
					}
				}
				
				if (contadorMaxPuntos > 1) {
					// Empate total - gana el primer jugador por defecto
					ganador = 1;
					System.out.println("¡EMPATE TOTAL! Gana J1 por defecto");
				} else {
					ganador = ganadorPorPuntos + 1;
					System.out.println("Ganador por puntos: J" + ganador + " (" + maxPuntos + " pts)");
				}
			} else {
				ganador = 1; // Fallback (no debería llegar aquí)
			}
			
			terminarJuego(ganador);
		}
	}

	private void terminarJuego(int ganador) {
		if (juegoTerminado)
			return;

		juegoTerminado = true;
		if (temporizadorJuego != null) {
			temporizadorJuego.cancel();
			temporizadorJuego = null;
		}

		// ✅ CORREGIDO: Verificar que el ganador es válido antes de acceder al array
		hiloServidor.enviarMensajeATodos("JuegoTerminado:" + ganador);
		
		System.out.println("===================================");
		
		// Verificar que el índice del ganador es válido
		if (ganador >= 1 && ganador <= numeroJugadoresActivos && jugadores[ganador - 1] != null) {
			System.out.println("Juego terminado. Ganador: J" + ganador + " (" + jugadores[ganador - 1].getNombre() + ")");
		} else {
			System.out.println("Juego terminado. Ganador: J" + ganador);
		}
		
		System.out.println("Puntuacion final:");
		for (int i = 0; i < numeroJugadoresActivos; i++) {
			if (jugadores[i] != null) {
				System.out.println("   J" + (i + 1) + " (" + jugadores[i].getNombre() + "): "
						+ jugadores[i].getPuntuacion() + " puntos");
			}
		}
		System.out.println("===================================");
		
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				resetearEstadoJuego();
			}
		}, 5000);
	}

	private void resetearEstadoJuego() {
		hiloServidor.desconectarClientesYResetear();

		jugadores = new JugadorServidor[MAX_JUGADORES];
		posicionesX = new float[MAX_JUGADORES];
		posicionesY = new float[MAX_JUGADORES];
		gestorFrutas = null;

		juegoIniciado = false;
		juegoTerminado = false;
		numeroJugadoresActivos = 0;
		contadorActualizaciones = 0;

		System.out.println("Estado reseteado");
		System.out.println("Esperando nuevos jugadores...");
	}

	private void enviarEstadoCompleto() {
		System.out.println("Enviando estado completo...");

		for (int i = 0; i < 5; i++) {
			String mensajeFrutas = "ActualizarFrutas:" + gestorFrutas.serializar();
			System.out.println("   Envio #" + (i + 1) + " de frutas");
			hiloServidor.enviarMensajeATodos(mensajeFrutas);

			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("   Enviando serpientes iniciales");
		enviarActualizacionesSerpientes();

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("   Enviando puntuacion inicial");
		enviarActualizacionPuntuacion();

		System.out.println("Estado completo enviado");
	}

	private void enviarActualizacionesSerpientes() {
		for (int i = 0; i < numeroJugadoresActivos; i++) {
			if (jugadores[i] != null) {
				String mensaje = "ActualizarSerpiente:" + (i + 1) + ":" + jugadores[i].getSerpiente().serializar();
				hiloServidor.enviarMensajeATodos(mensaje);
			}
		}
	}

	private void enviarActualizacionFrutas() {
		String mensaje = "ActualizarFrutas:" + gestorFrutas.serializar();
		hiloServidor.enviarMensajeATodos(mensaje);
	}

	private void enviarActualizacionPuntuacion() {
		StringBuilder sb = new StringBuilder("ActualizarPuntuacion:");
		for (int i = 0; i < numeroJugadoresActivos; i++) {
			if (i > 0)
				sb.append(":");
			sb.append(jugadores[i] != null ? jugadores[i].getPuntuacion() : 0);
		}
		hiloServidor.enviarMensajeATodos(sb.toString());
	}

	@Override
	public void moverJugador(int numeroJugador, Direcciones direccion) {
		int indice = numeroJugador - 1;
		if (indice >= 0 && indice < numeroJugadoresActivos && jugadores[indice] != null) {
			jugadores[indice].cambiarDireccion(direccion);
		}
	}

	@Override
	public void jugadorDesconectado(int numeroJugador) {
		System.out.println("J" + numeroJugador + " desconectado");

		if (juegoIniciado && !juegoTerminado) {
			int ganador = -1;
			for (int i = 0; i < numeroJugadoresActivos; i++) {
				if (i != (numeroJugador - 1) && jugadores[i] != null && jugadores[i].getVidas() > 0) {
					ganador = i + 1;
					break;
				}
			}

			if (ganador > 0) {
				System.out.println("J" + ganador + " gana por abandono");
				terminarJuego(ganador);
			}
		}
	}

	@Override
	public boolean estaJuegoIniciado() {
		return juegoIniciado;
	}

	public void cerrar() {
		if (temporizadorJuego != null) {
			temporizadorJuego.cancel();
		}
		if (temporizadorEspera != null) {
			temporizadorEspera.cancel();
		}
		if (hiloServidor != null) {
			hiloServidor.terminar();
		}
		System.out.println("Servidor cerrado");
	}
}