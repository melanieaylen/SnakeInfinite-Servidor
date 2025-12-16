package elementos;

import jugadores.JugadorServidor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * ✅ MEJORADO: 
 * - Verifica colisiones con TODAS las serpientes
 * - No permite frutas superpuestas
 * - Inicialización mejorada
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
     * ✅ NUEVO: Inicializa frutas verificando TODAS las serpientes
     */
    public void inicializarFrutasConJugadores(JugadorServidor[] jugadores, int numJugadores) {
        System.out.println("🍎 Inicializando frutas...");
        
        // Crear todas las frutas
        for (TipoFruta tipo : TipoFruta.values()) {
            FrutaServidor fruta = new FrutaServidor(0, 0, tamanioElementos, tamanioElementos, tipo);
            frutas.add(fruta);
        }
        
        // Posicionar cada fruta verificando colisiones con todas las serpientes
        for (int i = 0; i < frutas.size(); i++) {
            FrutaServidor fruta = frutas.get(i);
            moverFrutaAleatoriaConJugadores(fruta, jugadores, numJugadores);
            System.out.println("   ✅ " + fruta.getTipo() + " en (" + fruta.getPosX() + ", " + fruta.getPosY() + ")");
        }
        
        System.out.println("✅ " + frutas.size() + " frutas inicializadas");
    }
    
    /**
     * ✅ COMPATIBILIDAD: Para un solo jugador (modo solitario)
     */
    public void inicializarFrutas(SerpienteServidor serpiente) {
        System.out.println("🍎 Inicializando frutas (modo solitario)...");
        
        for (TipoFruta tipo : TipoFruta.values()) {
            FrutaServidor fruta = new FrutaServidor(0, 0, tamanioElementos, tamanioElementos, tipo);
            frutas.add(fruta);
        }
        
        for (int i = 0; i < frutas.size(); i++) {
            FrutaServidor fruta = frutas.get(i);
            moverFrutaAleatoria(fruta, serpiente);
            System.out.println("   ✅ " + fruta.getTipo() + " en (" + fruta.getPosX() + ", " + fruta.getPosY() + ")");
        }
        
        System.out.println("✅ " + frutas.size() + " frutas inicializadas");
    }
    
    public FrutaServidor verificarColisiones(SerpienteServidor serpiente) {
        for (FrutaServidor fruta : frutas) {
            if (fruta.colisionaConPosicion(serpiente.getPosX(), serpiente.getPosY())) {
                return fruta;
            }
        }
        return null;
    }
    
    /**
     * ✅ NUEVO: Reubicar verificando TODOS los jugadores
     */
    public void reubicarFrutaConJugadores(FrutaServidor fruta, JugadorServidor[] jugadores, int numJugadores) {
        moverFrutaAleatoriaConJugadores(fruta, jugadores, numJugadores);
    }
    
    /**
     * Reubicar con una sola serpiente (compatibilidad)
     */
    public void reubicarFruta(FrutaServidor fruta, SerpienteServidor serpiente) {
        moverFrutaAleatoria(fruta, serpiente);
    }
    
    /**
     * ✅ CRÍTICO: Mueve fruta verificando TODAS las serpientes activas
     */
    private void moverFrutaAleatoriaConJugadores(FrutaServidor fruta, JugadorServidor[] jugadores, int numJugadores) {
        int rangoMin = -15;
        int rangoMax = 25;
        float nuevaX, nuevaY;
        int intentos = 0;
        int maxIntentos = 300;
        
        // Usar la primera serpiente como referencia
        SerpienteServidor serpienteRef = jugadores[0].getSerpiente();
        
        do {
            int offsetX = random.nextInt(rangoMax - rangoMin + 1) + rangoMin;
            int offsetY = random.nextInt(rangoMax - rangoMin + 1) + rangoMin;
            
            nuevaX = serpienteRef.getPosX() + (offsetX * tamanioElementos);
            nuevaY = serpienteRef.getPosY() + (offsetY * tamanioElementos);
            
            intentos++;
            
            if (intentos >= maxIntentos) {
                System.out.println("⚠️ Usando posición alejada tras " + intentos + " intentos");
                nuevaX = serpienteRef.getPosX() + ((random.nextInt(60) - 30) * tamanioElementos);
                nuevaY = serpienteRef.getPosY() + ((random.nextInt(60) - 30) * tamanioElementos);
                break;
            }
            
        } while (posicionOcupadaConJugadores(nuevaX, nuevaY, fruta, jugadores, numJugadores));
        
        fruta.reubicar(nuevaX, nuevaY);
    }
    
    /**
     * Mover fruta con una sola serpiente (compatibilidad)
     */
    private void moverFrutaAleatoria(FrutaServidor fruta, SerpienteServidor serpiente) {
        int rangoMin = -15;
        int rangoMax = 25;
        float nuevaX, nuevaY;
        int intentos = 0;
        int maxIntentos = 300;
        
        do {
            int offsetX = random.nextInt(rangoMax - rangoMin + 1) + rangoMin;
            int offsetY = random.nextInt(rangoMax - rangoMin + 1) + rangoMin;
            
            nuevaX = serpiente.getPosX() + (offsetX * tamanioElementos);
            nuevaY = serpiente.getPosY() + (offsetY * tamanioElementos);
            
            intentos++;
            
            if (intentos >= maxIntentos) {
                nuevaX = serpiente.getPosX() + ((random.nextInt(60) - 30) * tamanioElementos);
                nuevaY = serpiente.getPosY() + ((random.nextInt(60) - 30) * tamanioElementos);
                break;
            }
            
        } while (posicionOcupada(nuevaX, nuevaY, fruta, serpiente));
        
        fruta.reubicar(nuevaX, nuevaY);
    }
    
    /**
     * ✅ CRÍTICO: Verifica si una posición está ocupada por CUALQUIER serpiente
     */
    private boolean posicionOcupadaConJugadores(float x, float y, FrutaServidor frutaActual, 
                                                 JugadorServidor[] jugadores, int numJugadores) {
        // 1. Verificar colisión con TODAS las serpientes vivas
        for (int i = 0; i < numJugadores; i++) {
            if (jugadores[i] != null && jugadores[i].getVidas() > 0) {
                if (jugadores[i].getSerpiente().colisionConPosicion(x, y)) {
                    return true;
                }
            }
        }
        
        // 2. Verificar colisión con OTRAS frutas (no consigo misma)
        for (FrutaServidor otraFruta : frutas) {
            if (otraFruta != frutaActual && otraFruta.colisionaConPosicion(x, y)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Verificar ocupación con una sola serpiente (compatibilidad)
     */
    private boolean posicionOcupada(float x, float y, FrutaServidor frutaActual, SerpienteServidor serpiente) {
        // 1. Verificar colisión con la serpiente
        if (serpiente.colisionConPosicion(x, y)) {
            return true;
        }
        
        // 2. Verificar colisión con OTRAS frutas
        for (FrutaServidor otraFruta : frutas) {
            if (otraFruta != frutaActual && otraFruta.colisionaConPosicion(x, y)) {
                return true;
            }
        }
        
        return false;
    }
    
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
    
    public List<FrutaServidor> getFrutas() {
        return frutas;
    }
}