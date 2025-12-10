package pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import elementos.Imagen;
import elementos.Texto;
import entradas.salidas.teclado.Entradas;
import utiles.Recursos;
import utiles.Render;

public class PantallaMenu implements Screen {

    // CONSTANTES
    private final int TAMANIO_TEXTO = 120;
    private final int TAMANIO_SUB = 50;

    private Imagen menu;
    private Imagen serpiente;
    private Music musica;
    private Texto titulo;
    private Texto subtitulo1;
    private Texto subtitulo2;
    private Texto subtitulo3;
    private Texto subtitulo4; 
    private Texto opcionElegida;
    private Entradas entrada = new Entradas();
    private int opc = 1;
    private float tiempo = 0;
    private float a = 0;

    private Sound sonidoBoton; 
    private OrthographicCamera camara;
    private Viewport viewport;

    @Override
    public void show() {
        menu = new Imagen(Recursos.FONDO_MENU);
        serpiente = new Imagen(Recursos.ICONO);
        serpiente.setParametros(900, 200, 300, 300);
        Gdx.input.setInputProcessor(entrada);
        camara = new OrthographicCamera();
        viewport = new FitViewport(1440, 900, camara);
        sonidoBoton = Gdx.audio.newSound(Gdx.files.internal("sonidos/boton.ogg"));
        musica = Gdx.audio.newMusic(Gdx.files.internal("sonidos/musica.mp3"));
        musica.setLooping(true); // ✅ Para que se repita automáticamente
        musica.play();

        titulo = new Texto(Recursos.FUENTE, TAMANIO_TEXTO, Color.WHITE, Color.TEAL, -3, 3, false);
        subtitulo1 = new Texto(Recursos.FUENTE, TAMANIO_SUB, Color.WHITE, Color.BLACK, -4, 4, true);
        subtitulo2 = new Texto(Recursos.FUENTE, TAMANIO_SUB, Color.WHITE, Color.BLACK, -4, 4, true);
        subtitulo3 = new Texto(Recursos.FUENTE, TAMANIO_SUB, Color.WHITE, Color.BLACK, -4, 4, true);
        subtitulo4 = new Texto(Recursos.FUENTE, TAMANIO_SUB, Color.WHITE, Color.BLACK, -4, 4, true);
        opcionElegida = new Texto(Recursos.FUENTE, TAMANIO_SUB, Color.SKY, Color.BLACK, -4, 4, true);
    }

    @Override
    public void render(float delta) {
        Render.limpiarPantalla(0, 0, 0);
        viewport.apply();
        procesarTransparencia();
        procesarEntradas(delta);
        actualizarInterfaz();

        Render.batch.begin();
        menu.dibujar();
        serpiente.dibujar();

        titulo.dibujarTexto("Snake Infinite", 330, 750);
        subtitulo1.dibujarTexto("   Un Jugador", 360, 530);
        subtitulo2.dibujarTexto("   Multijugador", 360, 430);
        subtitulo3.dibujarTexto("   Configuracion", 360, 330);
    	subtitulo4.dibujarTexto("   Salir", 360, 230);

        if (opc == 1) {
            opcionElegida.dibujarTexto("> ", 330, 530);
        } else if (opc == 2) {
            opcionElegida.dibujarTexto("> ", 330, 430);
        } else if (opc == 3) {
        	opcionElegida.dibujarTexto("> ", 330, 330);
        } else if (opc == 4) {
        	opcionElegida.dibujarTexto("> ", 330, 230);
        }
        Render.batch.end();
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
                if (opc > 4) {
                    opc = 1;
                }
            }
        }

        if (entrada.isArriba()) {
            if (tiempo > 0.2f) {
                tiempo = 0;
                opc--;
                if (opc < 1) {
                    opc = 4;
                }
            }
        }

        if (entrada.isEnter()) {
        	sonidoBoton.play();
            if (opc == 1) {
      		  
                Render.app.setScreen(new PantallaJuego());
            } else if (opc == 2) {
                Render.app.setScreen(new PantallaEjemplo());
            } else if (opc == 3) {
            	Render.app.setScreen(new PantallaConfig());
            } else if (opc == 4) {
            	Gdx.app.exit();
            }
        }
    }

    private void actualizarInterfaz() {
        if (opc == 1) {
            subtitulo1.setColor(Color.SKY);
            subtitulo2.setColor(Color.WHITE);
            subtitulo3.setColor(Color.WHITE);
            subtitulo4.setColor(Color.WHITE);
        } else if (opc == 2) {
            subtitulo1.setColor(Color.WHITE);
            subtitulo2.setColor(Color.SKY);
            subtitulo3.setColor(Color.WHITE);
            subtitulo4.setColor(Color.WHITE);
        } else if (opc == 3) {
        	subtitulo1.setColor(Color.WHITE);
            subtitulo2.setColor(Color.WHITE);
            subtitulo3.setColor(Color.SKY);
            subtitulo4.setColor(Color.WHITE);
        } else if (opc == 4) {
        	subtitulo1.setColor(Color.WHITE);
            subtitulo2.setColor(Color.WHITE);
            subtitulo3.setColor(Color.WHITE);
            subtitulo4.setColor(Color.SKY);
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
        // ✅ CLAVE: Se llama cuando cambias de pantalla
        // Aquí pausas o detienes la música
        if (musica != null && musica.isPlaying()) {
            musica.stop(); // Para detener completamente
            // O usa musica.pause() si quieres reanudarla después
        }
    }

    @Override
    public void dispose() {
        // ✅ Liberar recursos cuando ya no se necesiten
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