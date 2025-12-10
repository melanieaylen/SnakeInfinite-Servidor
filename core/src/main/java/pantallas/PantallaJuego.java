package pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import elementos.Direcciones;
import elementos.EstadoJuego;
import elementos.Fruta;
import elementos.GestorFrutas;
import elementos.Grilla;
import elementos.Imagen;
import elementos.Serpiente;
import elementos.Texto;
import entradas.salidas.teclado.Entradas;
import utiles.Config;
import utiles.Recursos;
import utiles.Render;

public class PantallaJuego implements Screen {

	// CONSTANTES
	private final int TAMANIO_ELEMENTOS = 30;
	private final float VELOCIDAD_SERPIENTE = 0.12f; // Tiempo entre movimientos
	private final float DURACION_INVULNERABILIDAD = 1.5f; // Segundos de invulnerabilidad

	// Para el mundo del juego
	private OrthographicCamera camaraUI;
	private Viewport viewportUI;
	private EstadoJuego estadoGuardado;
	
	// ELEMENTOS - FRUTAS
	private Serpiente serpiente;
	private GestorFrutas gestorFrutas;
	private Grilla grilla;

	// DISEÑO
	private Texto textoPuntuacion;
	private OrthographicCamera camara;
	private Viewport viewport;
	private Sound sonidoComer; 
	private Music musica;
	private Imagen dosVidas; 
	private Imagen unaVida; 
	private Imagen sinVidas; 
	
	// ENTRADAS
	private Entradas entrada = new Entradas();
	private Direcciones direccionActual = Direcciones.NINGUNA;

	// LOGICA Y ETC
	private float posElementosX;
	private float posElementosY;
	private int puntuacion = 0;
	private float tiempo;
	private int vida = 3;
	
	// Sistema de invulnerabilidad
	private boolean invulnerable = false;
	private float tiempoInvulnerabilidad = 0;
	
	public PantallaJuego() {
		this.estadoGuardado = null;
	}

	// Constructor con estado (restaurar desde pausa)
	public PantallaJuego(EstadoJuego estado) {
		this.estadoGuardado = estado;
	}
	
	@Override
	public void show() {
		textoPuntuacion = new Texto(Recursos.TEXTO, 33, Color.WHITE, Color.BLACK, -4, 4, true);
		grilla = new Grilla(TAMANIO_ELEMENTOS);
		sonidoComer = Gdx.audio.newSound(Gdx.files.internal("sonidos/comer.wav"));
		musica = Gdx.audio.newMusic(Gdx.files.internal("sonidos/musicaJuego.mp3"));
		musica.setLooping(true);
		musica.setVolume(0.15f);
		musica.play();
		unaVida = new Imagen(Recursos.VIDA1);
		dosVidas = new Imagen(Recursos.VIDA2);
		sinVidas = new Imagen(Recursos.VIDA3);
		
		// Restaurar estado si existe
		if (estadoGuardado != null) {
			serpiente = estadoGuardado.getSerpiente();
			gestorFrutas = estadoGuardado.getGestorFrutas();
			puntuacion = estadoGuardado.getPuntuacion();
			posElementosX = estadoGuardado.getPosElementosX();
			posElementosY = estadoGuardado.getPosElementosY();
			direccionActual = estadoGuardado.getDireccionActual();
			tiempo = estadoGuardado.getTiempo();
			vida = estadoGuardado.getVida();
		} else {
			// Inicialización normal (juego nuevo)
			posElementosX = 0;
			posElementosY = 0;
			serpiente = new Serpiente(posElementosX, posElementosY, TAMANIO_ELEMENTOS, TAMANIO_ELEMENTOS);
			gestorFrutas = new GestorFrutas(TAMANIO_ELEMENTOS);
			gestorFrutas.inicializarFrutas(serpiente);
			puntuacion = 0;
			tiempo = 0;
			direccionActual = Direcciones.NINGUNA;
			vida = 3;
		}

		// Configurar cámaras
		camara = new OrthographicCamera();
		camara.setToOrtho(false, Config.ANCHO, Config.ALTO);
		viewport = new FitViewport(Config.ANCHO, Config.ALTO, camara);
		camara.position.set(posElementosX, posElementosY, 0);
		camara.update();

		camaraUI = new OrthographicCamera();
		viewportUI = new FitViewport(Config.ANCHO, Config.ALTO, camaraUI);
		camaraUI.position.set(Config.ANCHO / 2, Config.ALTO / 2, 0);
		camaraUI.update();

		Gdx.input.setInputProcessor(entrada);
	}

	@Override
	public void render(float delta) {
		Render.limpiarPantalla(0, 0, 0);

		// LOGICA
		procesarEntradas(delta);

		// Actualizar invulnerabilidad
		if (invulnerable) {
			tiempoInvulnerabilidad += delta;
			if (tiempoInvulnerabilidad >= DURACION_INVULNERABILIDAD) {
				invulnerable = false;
				tiempoInvulnerabilidad = 0;
			}
		}

		// Verificar colisiones con frutas
		Fruta frutaColisionada = gestorFrutas.verificarColisiones(serpiente);
		if (frutaColisionada != null) {
			sonidoComer.play();
			serpiente.crecer();
			puntuacion += frutaColisionada.getTipo().getPuntos();
			gestorFrutas.reubicarFruta(frutaColisionada, serpiente);
		}

		// ACTUALIZAR CAMARA PARA SEGUIR A LA SERPIENTE
		actualizarCamara();

		// ===== RENDER DEL MUNDO DEL JUEGO =====
		viewport.apply();
		Render.batch.setProjectionMatrix(camara.combined);
		Render.shaper.setProjectionMatrix(camara.combined);

		// RENDER SERPIENTE
		Render.shaper.begin(ShapeType.Filled);
		grilla.dibujarGrilla(camara);
		
		// Verificar colisión solo si NO es invulnerable
		if (!invulnerable && serpiente.colisionSerpiente()) {
			perderVida();
			Render.batch.begin();
			if(vida == 2) {
				dosVidas.dibujar();
			}else if(vida == 1) {
				unaVida.dibujar();
			}else if(vida == 0) {
				sinVidas.dibujar();
			}
			Render.batch.end();
		}
		
		// Dibujar serpiente con efecto de parpadeo si es invulnerable
		if (!invulnerable || (int)(tiempoInvulnerabilidad * 10) % 2 == 0) {
			serpiente.dibujar();
		}
		
		Render.shaper.end();

		// RENDER FRUTAS
		Render.batch.begin();
		gestorFrutas.dibujarTodas();
		Render.batch.end();

		// ===== RENDER DE LA UI FIJA =====
		dibujarUI();
	}

	private void perderVida() {
		vida--;
		
		if (vida <= 0) {
			// Game Over
			Render.app.setScreen(new PantallaGameOver());
		} else {
			// Resetear serpiente a la posición inicial
			posElementosX = 0;
			posElementosY = 0;
			direccionActual = Direcciones.NINGUNA;
			
			// Recrear serpiente en posición inicial
			serpiente = new Serpiente(posElementosX, posElementosY, 
									  TAMANIO_ELEMENTOS, TAMANIO_ELEMENTOS);
			
			// Activar invulnerabilidad
			invulnerable = true;
			tiempoInvulnerabilidad = 0;
		}
	}

	private void actualizarCamara() {
		// Centrar la cámara directamente en la serpiente (sin lag)
		float targetX = serpiente.getPosX() + TAMANIO_ELEMENTOS / 2;
		float targetY = serpiente.getPosY() + TAMANIO_ELEMENTOS / 2;

		// Actualización instantánea
		camara.position.set(targetX, targetY, 0);
		camara.update();
	}

	private void dibujarUI() {
		// Usa la cámara UI dedicada
		viewportUI.apply();
		Render.batch.setProjectionMatrix(camaraUI.combined);

		Render.batch.begin();
		
		// Puntuación
		textoPuntuacion.dibujarTexto("Puntos: " + puntuacion, 20, Config.ALTO - 35);
		
		// Vidas con corazones
		textoPuntuacion.dibujarTexto("Vidas: " + vida, 20, Config.ALTO - 70);
		
		// Mensaje de invulnerabilidad (opcional)
		if (invulnerable) {
			textoPuntuacion.dibujarTexto("INVULNERABLE!", 
										 Config.ANCHO / 2 - 100, 
										 Config.ALTO / 2);
		}
		
		Render.batch.end();
	}

	private void procesarEntradas(float delta) {
		if (entrada.isArriba() && direccionActual != Direcciones.ABAJO) {
			direccionActual = Direcciones.ARRIBA;
		} else if (entrada.isAbajo() && direccionActual != Direcciones.ARRIBA) {
			direccionActual = Direcciones.ABAJO;
		} else if (entrada.isDerecha() && direccionActual != Direcciones.IZQUIERDA) {
			direccionActual = Direcciones.DERECHA;
		} else if (entrada.isIzquierda() && direccionActual != Direcciones.DERECHA) {
			direccionActual = Direcciones.IZQUIERDA;
		} else if (entrada.isPausa()) {
			// Guardar estado antes de pausar
			EstadoJuego estado = new EstadoJuego(
				serpiente, gestorFrutas, puntuacion,
				posElementosX, posElementosY, direccionActual, tiempo, vida
			);
			Render.app.setScreen(new PantallaPausa(estado));
			return;
		}

		moverSerpiente(delta);
	}

	private void moverSerpiente(float delta) {
		tiempo += delta;
		if (tiempo > VELOCIDAD_SERPIENTE) {
			tiempo = 0;

			switch (direccionActual) {
			case ARRIBA:
				posElementosY += serpiente.getAlto();
				break;

			case ABAJO:
				posElementosY -= serpiente.getAlto();
				break;

			case DERECHA:
				posElementosX += serpiente.getAncho();
				break;

			case IZQUIERDA:
				posElementosX -= serpiente.getAncho();
				break;

			case NINGUNA:
				break;

			default:
				break;
			}

			// La serpiente puede moverse libremente sin límites
			serpiente.mover(posElementosX, posElementosY);
		}
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
		viewportUI.update(width, height, true);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
		// Se llama cuando cambias de pantalla
		if (musica != null && musica.isPlaying()) {
			musica.stop();
		}
	}

	@Override
	public void dispose() {
		if (musica != null) {
			musica.dispose();
		}
		if (estadoGuardado == null) {
			gestorFrutas.dispose();
		}
		if (sonidoComer != null) {
			sonidoComer.dispose();
		}
	}
}