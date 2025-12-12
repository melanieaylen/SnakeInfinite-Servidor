package elementos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * GestorFrutasServidor - Versión del gestor de frutas SIN dependencias de LibGDX
 */
public class GestorFrutasServidor {
    
    private List<FrutaServidor> frutas;
    private int tamanioElementos;
    private Random random;
    
    public GestorFrutasServidor(int tamanioElementos) {
        this.tamanioElementos = tamanioElementos;
        this.frutas = new ArrayList<>();
        this.random = new Random();
    }
    
    /**
     * Inicializa todas las frutas del juego
     */
    public void inicializarFrutas(SerpienteServidor serpiente) {
        for (TipoFruta tipo : TipoFruta.values()) {
            FrutaServidor fruta = new FrutaServidor(0, 0, tamanioElementos, tamanioElementos, tipo);
            moverFrutaAleatoria(fruta, serpiente);
            frutas.add(fruta);
        }
        System.out.println("✅ Inicializadas " + frutas.size() + " frutas");
    }
    
    /**
     * Verifica si una serpiente colisionó con alguna fruta
     * @return La fruta colisionada o null
     */
    public FrutaServidor verificarColisiones(SerpienteServidor serpiente) {
        for (FrutaServidor fruta : frutas) {
            if (fruta.colisionaConPosicion(serpiente.getPosX(), serpiente.getPosY())) {
                return fruta;
            }
        }
        return null;
    }
    
    /**
     * Reubica una fruta después de ser comida
     */
    public void reubicarFruta(FrutaServidor fruta, SerpienteServidor serpiente) {
        moverFrutaAleatoria(fruta, serpiente);
    }
    
    /**
     * Mueve una fruta a una posición aleatoria válida
     */
    private void moverFrutaAleatoria(FrutaServidor fruta, SerpienteServidor serpiente) {
        int rangoMin = -10;
        int rangoMax = 20;
        float nuevaX, nuevaY;
        
        do {
            int offsetX = random.nextInt(rangoMax - rangoMin + 1) + rangoMin;
            int offsetY = random.nextInt(rangoMax - rangoMin + 1) + rangoMin;
            
            nuevaX = serpiente.getPosX() + (offsetX * tamanioElementos);
            nuevaY = serpiente.getPosY() + (offsetY * tamanioElementos);
            
        } while (serpiente.colisionConPosicion(nuevaX, nuevaY));
        
        fruta.reubicar(nuevaX, nuevaY);
    }
    
    /**
     * Serializa todas las frutas para enviar al cliente
     * Formato: "TIPO:X:Y|TIPO:X:Y|..."
     */
    public String serializar() {
        StringBuilder sb = new StringBuilder();
        boolean primera = true;
        
        for (FrutaServidor fruta : frutas) {
            if (!primera) sb.append("|");
            sb.append(fruta.getTipo().name()).append(":");
            sb.append((int) fruta.getPosX()).append(":");
            sb.append((int) fruta.getPosY());
            primera = false;
        }
        
        return sb.toString();
    }
    
    // Getter
    public List<FrutaServidor> getFrutas() {
        return frutas;
    }
}