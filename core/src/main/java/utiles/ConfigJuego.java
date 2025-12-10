package utiles;

import com.badlogic.gdx.graphics.Color;

public class ConfigJuego {
	private static ConfigJuego instancia;

	// Colores de la serpiente
	private Color colorCabeza;
	private Color colorCuerpo;

	// Configuración de audio
	private float volumenMusica = 0.7f;
	private float volumenSonidos = 1.0f;
	private boolean musicaActivada = true;
	private boolean sonidosActivados = true;

	// Paleta de colores optimizada para fondo rosa claro
	public static final Color[][] PALETA_COLORES = {
		// Negro + Maroon (clásico LibGDX)
		{Color.BLACK, Color.MAROON},
		
		// Verde suave + Verde medio (contraste natural con rosa)
		{new Color(0.25f, 0.60f, 0.40f, 1), new Color(0.45f, 0.75f, 0.55f, 1)},
		
		// Púrpura equilibrado + Púrpura suave (elegante con rosa)
		{new Color(0.45f, 0.30f, 0.60f, 1), new Color(0.65f, 0.50f, 0.75f, 1)},
		
		// Azul medio + Azul suave (contraste fresco)
		{new Color(0.25f, 0.45f, 0.70f, 1), new Color(0.45f, 0.65f, 0.85f, 1)},
		
		// Rojo suave + Rosa fuerte (complementa el fondo)
		{new Color(0.70f, 0.25f, 0.35f, 1), new Color(0.85f, 0.45f, 0.55f, 1)},
		
		// Naranja cálido + Naranja suave (alegre pero no chillón)
		{new Color(0.80f, 0.45f, 0.20f, 1), new Color(0.90f, 0.65f, 0.45f, 1)},
		
		// Turquesa moderado + Turquesa claro (vibrante pero suave)
		{new Color(0.20f, 0.60f, 0.65f, 1), new Color(0.45f, 0.75f, 0.80f, 1)},
		
		// Dorado suave + Dorado claro (elegante)
		{new Color(0.75f, 0.60f, 0.25f, 1), new Color(0.90f, 0.80f, 0.50f, 1)}
	};
	
	public static final String[] NOMBRES_COLORES = {
		"Clásico",
		"Verde",
		"Violeta",
		"Azul",
		"Rojo",
		"Naranja",
		"Turquesa",
		"Amarillo"
	};

	private int indiceColorSeleccionado = 0;

	private ConfigJuego() {
		// Color por defecto (Clásico - Negro y Maroon)
		colorCabeza = Color.BLACK;
		colorCuerpo = Color.MAROON;
	}

	public static ConfigJuego getInstancia() {
		if (instancia == null) {
			instancia = new ConfigJuego();
		}
		return instancia;
	}

	public void setColores(Color cabeza, Color cuerpo) {
		this.colorCabeza = cabeza;
		this.colorCuerpo = cuerpo;
	}

	public void setColorPorIndice(int indice) {
		if (indice >= 0 && indice < PALETA_COLORES.length) {
			this.indiceColorSeleccionado = indice;
			this.colorCabeza = PALETA_COLORES[indice][0];
			this.colorCuerpo = PALETA_COLORES[indice][1];
		}
	}

	public Color getColorCabeza() {
		return colorCabeza;
	}

	public Color getColorCuerpo() {
		return colorCuerpo;
	}

	public int getIndiceColorSeleccionado() {
		return indiceColorSeleccionado;
	}

	// Métodos de audio
	public float getVolumenMusica() {
		return volumenMusica;
	}

	public void setVolumenMusica(float volumen) {
		this.volumenMusica = Math.max(0f, Math.min(1f, volumen));
	}

	public float getVolumenSonidos() {
		return volumenSonidos;
	}

	public void setVolumenSonidos(float volumen) {
		this.volumenSonidos = Math.max(0f, Math.min(1f, volumen));
	}

	public boolean isMusicaActivada() {
		return musicaActivada;
	}

	public void setMusicaActivada(boolean activada) {
		this.musicaActivada = activada;
	}

	public boolean isSonidosActivados() {
		return sonidosActivados;
	}

	public void setSonidosActivados(boolean activados) {
		this.sonidosActivados = activados;
	}
}