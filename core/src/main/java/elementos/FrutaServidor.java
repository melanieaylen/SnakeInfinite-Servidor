package elementos;

public class FrutaServidor {
    
    private float posX;
    private float posY;
    private int ancho;
    private int alto;
    private TipoFruta tipo;
    
    public FrutaServidor(float posX, float posY, int ancho, int alto, TipoFruta tipo) {
        this.posX = posX;
        this.posY = posY;
        this.ancho = ancho;
        this.alto = alto;
        this.tipo = tipo;
    }
    
    public void reubicar(float nuevaX, float nuevaY) {
        this.posX = nuevaX;
        this.posY = nuevaY;
    }
    
    public boolean colisionaConPosicion(float x, float y) {
        return posX == x && posY == y;
    }
    
    // Getters
    public float getPosX() {
        return posX;
    }
    
    public float getPosY() {
        return posY;
    }
    
    public TipoFruta getTipo() {
        return tipo;
    }
    
    public int getPuntos() {
        return tipo.getPuntos();
    }
    
    public String serializar() {
        return tipo.name() + ":" + posX + ":" + posY;
    }
}