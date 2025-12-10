package elementos;

public class Fruta {
    private float posX, posY;
    private int ancho, alto;
    private TipoFruta tipo;
    private Imagen imagen;

    public Fruta(float posX, float posY, int ancho, int alto, TipoFruta tipo) {
        this.posX = posX;
        this.posY = posY;
        this.ancho = ancho;
        this.alto = alto;
        this.tipo = tipo;
        // ✅ Eliminamos el switch - el enum maneja la configuración
        this.imagen = new Imagen(tipo.getRutaImagen());
    }

    public void dibujar() {
        imagen.setParametros(posX, posY, ancho - 1, alto - 1);
        imagen.dibujar();
    }

    // Getters y setters
    public float getPosX() { return posX; }
    public void setPosX(float posX) { this.posX = posX; }
    public float getPosY() { return posY; }
    public void setPosY(float posY) { this.posY = posY; }
    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }
    public TipoFruta getTipo() { return tipo; }

    public void dispose() {
        imagen.dispose();
    }
}
