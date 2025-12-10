package pantallas;
import com.badlogic.gdx.Screen;

import elementos.Imagen;
import utiles.Recursos;
import utiles.Render;

public class PantallaEjemplo implements Screen {

	private Imagen fondo; 
	@Override
	public void show() {
		fondo = new Imagen(Recursos.FONDO_MENU);
	}

	@Override
	public void render(float delta) {
		Render.batch.begin();
		fondo.dibujar();
		Render.batch.end();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
	}

}
