package utiles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import mi.juego.snake.Principal;

public class Render {
	// Declaro el objeto
	public static SpriteBatch batch;
	public static Principal app;
	public static ShapeRenderer shaper;
	
	public static void limpiarPantalla(float r, float g, float b) {
		Gdx.gl.glClearColor(r, g, b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	public static void begin() {
		batch.begin();
	}

	public static void end() {
		batch.end();
	}
	
	public static void dispose() {
		batch.dispose();
	}

}
