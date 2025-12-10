package pantallas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;

import elementos.Imagen;
import utiles.Recursos;
import utiles.Render;

public class PantallaGameOver implements Screen {
    
    private float transparencia = 0; // Empieza invisible
    private Imagen fondo; // o los elementos que tengas
    private Music musica;
    
    @Override
    public void show() {
        // Inicializa tus elementos
        fondo = new Imagen(Recursos.GAME_OVER);
        musica = Gdx.audio.newMusic(Gdx.files.internal("sonidos/gameOver.mp3"));
        musica.play();
    }
    
    @Override
    public void render(float delta) {
        Render.limpiarPantalla(0, 0, 0);
        
        // Incrementar transparencia gradualmente
        procesarTransparencia();
        
        Render.batch.begin();
        fondo.setTransparencia(transparencia);
        fondo.dibujar();
        
        Render.batch.end();
    }
    
    private void procesarTransparencia() {
        transparencia += 0.004f; // Misma velocidad que usas en PantallaJuego
        if(transparencia > 1) {
            transparencia = 1;
        }
    }

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public void pause() {
        // ✅ Se llama cuando la app pasa a segundo plano (Android)
        if (musica != null && musica.isPlaying()) {
            musica.pause();
        }
    }

    @Override
    public void resume() {
        // ✅ Se llama cuando la app vuelve del segundo plano (Android)
        if (musica != null && !musica.isPlaying()) {
            musica.play();
        }
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
       
    }
}