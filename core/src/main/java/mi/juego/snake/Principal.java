package mi.juego.snake;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import pantallas.PantallaMenu;
import utiles.Render;

public class Principal extends Game {
	
	public void create() {
		// Creo el objeto
		Render.app = this;
		Render.batch = new SpriteBatch();
		Render.shaper = new ShapeRenderer();
		this.setScreen(new PantallaMenu());
	}

	@Override
	public void render() {
		ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);
		super.render();
	}
	
	@Override
	public void dispose() {
		Render.batch.dispose();
	}
}
