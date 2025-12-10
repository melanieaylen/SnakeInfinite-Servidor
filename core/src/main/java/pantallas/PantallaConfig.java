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

import elementos.Imagen;
import elementos.Texto;
import entradas.salidas.teclado.Entradas;
import utiles.ConfigJuego;
import utiles.Recursos;
import utiles.Render;

public class PantallaConfig implements Screen {
	
	// CONSTANTES
	private final int TAMANIO_TEXTO = 120;
	private final int TAMANIO_SUB = 60;
	
	private Imagen menu;
	private Imagen serpiente;
	private Music musica;
	private Sound sonidoBoton;
	
	private Texto titulo;
	private Texto subtitulo1;
	private Texto subtitulo2;
	private Texto opcionElegida;
	
	private OrthographicCamera camara;
	private Viewport viewport;
	private Entradas entrada;
	
	private ConfigJuego config;
	private int indiceColorActual;
	
	private int opc = 1;
	private float tiempo = 0;
	private float a = 0;
	
	// Para vista previa de la serpiente
	private final int TAMANIO_PREVIEW = 40;
	private final int SEGMENTOS_PREVIEW = 5;
	
	public PantallaConfig() {
		config = ConfigJuego.getInstancia();
		indiceColorActual = config.getIndiceColorSeleccionado();
	}

	@Override
	public void show() {
		menu = new Imagen(Recursos.FONDO_MENU);
		serpiente = new Imagen(Recursos.ICONO);
		serpiente.setParametros(720, 140, 130, 130);
		entrada = new Entradas();
		Gdx.input.setInputProcessor(entrada);
		
		// Configurar cámara igual que en PantallaMenu
		camara = new OrthographicCamera();
		camara.setToOrtho(false, 1440, 900);
		viewport = new FitViewport(1440, 900, camara);
		
		sonidoBoton = Gdx.audio.newSound(Gdx.files.internal("sonidos/boton.ogg"));
		musica = Gdx.audio.newMusic(Gdx.files.internal("sonidos/musica.mp3"));
		musica.setLooping(true);
		musica.play();
		
		titulo = new Texto(Recursos.FUENTE, TAMANIO_TEXTO, Color.WHITE, Color.TEAL, -3, 3, false);
		subtitulo1 = new Texto(Recursos.FUENTE, TAMANIO_SUB, Color.WHITE, Color.BLACK, -4, 4, true);
		subtitulo2 = new Texto(Recursos.FUENTE, TAMANIO_SUB, Color.WHITE, Color.BLACK, -4, 4, true);
		opcionElegida = new Texto(Recursos.FUENTE, TAMANIO_SUB, Color.SKY, Color.BLACK, -4, 4, true);
	}

	@Override
	public void render(float delta) {
		Render.limpiarPantalla(0, 0, 0);
		
		procesarTransparencia();
		procesarEntradas(delta);
		actualizarInterfaz();
		
		viewport.apply();
		Render.batch.setProjectionMatrix(camara.combined);
		Render.shaper.setProjectionMatrix(camara.combined);
		
		// Primero dibujar el fondo
		Render.batch.begin();
		menu.dibujar();
		Render.batch.end();
		
		// Luego la serpiente preview (con ShapeRenderer)
		dibujarPreviewSerpiente();
		
		// Finalmente los textos
		Render.batch.begin();
		
		// Título
		titulo.dibujarTexto("Configuración", 300, 800);
		
		// Mostrar color actual con flechas
		subtitulo1.dibujarTexto("<  " + ConfigJuego.NOMBRES_COLORES[indiceColorActual] + " >", 
								550, 560);

		subtitulo2.dibujarTexto("Volver al Menu", 530, 270);

		if (opc == 2) {
			opcionElegida.dibujarTexto("> ", 400, 270);
		}
		
		Render.batch.end();
	}
	
	private void dibujarPreviewSerpiente() {
		Render.shaper.begin(ShapeType.Filled);
		
		Color[] coloresActuales = ConfigJuego.PALETA_COLORES[indiceColorActual];
		float centroX = 720 - (SEGMENTOS_PREVIEW * TAMANIO_PREVIEW) / 2;
		float centroY = 360;
		
		// Dibujar segmentos de la serpiente
		for (int i = 0; i < SEGMENTOS_PREVIEW; i++) {
			if (i == 0) {
				// Cabeza
				Render.shaper.setColor(coloresActuales[0]);
			} else {
				// Cuerpo
				Render.shaper.setColor(coloresActuales[1]);
			}
			
			float x = centroX + (i * TAMANIO_PREVIEW);
			Render.shaper.rect(x, centroY, TAMANIO_PREVIEW, TAMANIO_PREVIEW);
		}
		
		Render.shaper.end();
	}
	
	private void procesarTransparencia() {
		a += 0.012f;
		if (a > 1) {
			a = 1;
		}
		menu.setTransparencia(a);
	}
	
	private void procesarEntradas(float delta) {
		tiempo += delta;
		if (entrada.isAbajo()) {
			if (tiempo > 0.2f) {
				tiempo = 0;
				opc++;
				if (opc > 2) {
					opc = 1;
				}
			}
		}
		
		if (entrada.isArriba()) {
			if (tiempo > 0.2f) {
				tiempo = 0;
				opc--;
				if (opc < 1) {
					opc = 2;
				}
			}
		}
		if (opc == 1) {
			if (entrada.isDerecha()) {
				if (tiempo > 0.2f) {
					tiempo = 0;
					indiceColorActual++;
					if (indiceColorActual >= ConfigJuego.PALETA_COLORES.length) {
						indiceColorActual = 0;
					}
				}
			}
			
			if (entrada.isIzquierda()) {
				if (tiempo > 0.2f) {
					tiempo = 0;
					indiceColorActual--;
					if (indiceColorActual < 0) {
						indiceColorActual = ConfigJuego.PALETA_COLORES.length - 1;
					}
				}
			}
		}
		
		// Confirmar selección
		if (entrada.isEnter()) {
			sonidoBoton.play();
			if (opc == 1) {
				// Guardar el color seleccionado
				config.setColorPorIndice(indiceColorActual);
			} else if (opc == 2) {
				// Volver al menú (guardará automáticamente)
				config.setColorPorIndice(indiceColorActual);
				Render.app.setScreen(new PantallaMenu());
			}
		}
	}
	
	private void actualizarInterfaz() {
		if (opc == 1) {
			subtitulo1.setColor(Color.SKY);
			subtitulo2.setColor(Color.WHITE);
		} else if (opc == 2) {
			subtitulo1.setColor(Color.WHITE);
			subtitulo2.setColor(Color.SKY);
		}
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
		if (musica != null && musica.isPlaying()) {
			musica.stop();
		}
	}

	@Override
	public void dispose() {
		if (musica != null) {
			musica.dispose();
		}
		if (sonidoBoton != null) {
			sonidoBoton.dispose();
		}
		if (titulo != null) titulo.dispose();
		if (subtitulo1 != null) subtitulo1.dispose();
		if (subtitulo2 != null) subtitulo2.dispose();
		if (opcionElegida != null) opcionElegida.dispose();
	}
}