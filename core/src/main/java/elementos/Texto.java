package elementos;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import utiles.Render;

public class Texto {
	private BitmapFont fuente;

	public Texto(String rutaFuente, float dimension, Color colorLetra, Color colorSombra, int sombraX, int sombraY,
			boolean borde) {
		generarTexto(rutaFuente, dimension, colorLetra, colorSombra, sombraX, sombraY, borde);
	}
	
	private void generarTexto(String rutaFuente, float dimension, Color colorLetra, Color colorSombra, int sombraX, int sombraY,
			boolean borde) {
		FreeTypeFontGenerator generador = new FreeTypeFontGenerator(Gdx.files.internal(rutaFuente));
		FreeTypeFontParameter parametros = new FreeTypeFontGenerator.FreeTypeFontParameter();

		parametros.size = (int) dimension;
		parametros.color = colorLetra;
		parametros.shadowColor = colorSombra;
		parametros.shadowOffsetX = sombraX;
		parametros.shadowOffsetY = sombraY;

		if (borde) {
			parametros.borderWidth = 1;
			parametros.borderColor = Color.BLACK;
		}

		fuente = generador.generateFont(parametros);
		generador.dispose();
	}

	public void setColor(Color color) {
		fuente.setColor(color);
	}
	
	public void dibujarTexto(String texto, float anchoTexto, float altoTexto) {
		fuente.draw(Render.batch, texto, anchoTexto, altoTexto);
	}
	
	public void dispose() {
		fuente.dispose();
	}
}
