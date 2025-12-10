package elementos;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

import utiles.Render;

public class Imagen {
	private Texture textura;
	private Sprite sprite;

	public Imagen(String ruta) {
		textura = new Texture(ruta);
		sprite = new Sprite(textura);
	}

	public void dibujar() {
		sprite.draw(Render.batch);
	}

	public void setParametros(float x, float y, float ancho, float alto) {
		sprite.setBounds(x, y, ancho, alto);
	}

	public void setTransparencia(float a) {
		sprite.setAlpha(a);
	}
	
	public void dispose() {
		textura.dispose();
	}

}
