package elementos;

/**
 * SerpienteServidor - Versión sin LibGDX para el servidor
 */
public class SerpienteServidor {
    private int ancho, alto;
    private float[][] posiciones;
    private int tamanioMaximo = 1000, tamanioActual;
    private boolean debeCrecer = false;
    
    public SerpienteServidor(float posX, float posY, int ancho, int alto) {
        this.ancho = ancho;
        this.alto = alto;
        posiciones = new float[tamanioMaximo][2];
        posiciones[0][0] = posX;
        posiciones[0][1] = posY;
        tamanioActual = 1;
    }
    
    public void mover(float nuevaX, float nuevaY) {
        float ultimaX = posiciones[tamanioActual - 1][0];
        float ultimaY = posiciones[tamanioActual - 1][1];
        
        // Mover cuerpo
        for (int i = tamanioActual - 1; i > 0; i--) {
            posiciones[i][0] = posiciones[i - 1][0];
            posiciones[i][1] = posiciones[i - 1][1];
        }
        
        // Mover cabeza
        posiciones[0][0] = nuevaX;
        posiciones[0][1] = nuevaY;
        
        // Crecer si es necesario
        if (debeCrecer) {
            posiciones[tamanioActual][0] = ultimaX;
            posiciones[tamanioActual][1] = ultimaY;
            tamanioActual++;
            debeCrecer = false;
        }
    }
    
    public void crecer() {
        if (tamanioActual < tamanioMaximo) {
            debeCrecer = true;
        }
    }
    
    public boolean colisionSerpiente() {
        for (int i = 1; i < tamanioActual; i++) {
            if (posiciones[0][0] == posiciones[i][0] && posiciones[0][1] == posiciones[i][1]) {
                return true;
            }
        }
        return false;
    }
    
    public boolean colisionConPosicion(float x, float y) {
        for (int i = 0; i < tamanioActual; i++) {
            if (posiciones[i][0] == x && posiciones[i][1] == y) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Serializa todos los segmentos en formato: x1:y1,x2:y2,x3:y3,...
     */
    public String serializar() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tamanioActual; i++) {
            if (i > 0) sb.append(",");
            sb.append(posiciones[i][0]).append(":").append(posiciones[i][1]);
        }
        return sb.toString();
    }
    
    public int getAncho() {
        return ancho;
    }
    
    public int getAlto() {
        return alto;
    }
    
    public float getPosX() {
        return posiciones[0][0];
    }
    
    public float getPosY() {
        return posiciones[0][1];
    }
    
    public int getTamanioActual() {
        return tamanioActual;
    }
}