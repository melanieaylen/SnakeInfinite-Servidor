package jugadores;

import elementos.Direcciones;
import elementos.SerpienteServidor;

/**
 * JugadorServidor - VersiÃ³n del jugador SIN dependencias de LibGDX
 * Usado en el servidor
 */
public class JugadorServidor {
    
    private int id;
    private String nombre;
    private int puntuacion;
    private int vidas;
    private SerpienteServidor serpiente;
    private Direcciones direccionActual;
    
    // Sistema de invulnerabilidad
    private boolean invulnerable;
    private float tiempoInvulnerabilidad;
    private static final float DURACION_INVULNERABILIDAD = 1.5f;
    
    public JugadorServidor(int id, String nombre, SerpienteServidor serpiente) {
        this.id = id;
        this.nombre = nombre;
        this.serpiente = serpiente;
        this.puntuacion = 0;
        this.vidas = 3;
        this.direccionActual = Direcciones.NINGUNA;
        this.invulnerable = false;
        this.tiempoInvulnerabilidad = 0;
    }
    
    /**
     * AÃ±ade puntos al jugador
     */
    public void agregarPuntos(int puntos) {
        this.puntuacion += puntos;
    }
    
    /**
     * Hace crecer la serpiente del jugador
     */
    public void crecerSerpiente() {
        serpiente.crecer();
    }
    
    /**
     * Pierde una vida
     * @return true si todavÃ­a tiene vidas, false si es Game Over
     */
    public boolean perderVida() {
        vidas--;
        if (vidas > 0) {
            activarInvulnerabilidad();
            return true;
        }
        return false;
    }
    
    /**
     * Activa la invulnerabilidad temporal
     */
    public void activarInvulnerabilidad() {
        invulnerable = true;
        tiempoInvulnerabilidad = 0;
    }
    
    /**
     * Actualiza el estado de invulnerabilidad
     */
    public void actualizarInvulnerabilidad(float delta) {
        if (invulnerable) {
            tiempoInvulnerabilidad += delta;
            if (tiempoInvulnerabilidad >= DURACION_INVULNERABILIDAD) {
                invulnerable = false;
                tiempoInvulnerabilidad = 0;
            }
        }
    }
    
    /**
     * Resetea al jugador a la posiciÃ³n inicial
     */
    public void resetearPosicion(float posX, float posY, int ancho, int alto) {
        this.serpiente = new SerpienteServidor(posX, posY, ancho, alto);
        this.direccionActual = Direcciones.NINGUNA;
    }
    
    /**
     * Cambia la direcciÃ³n del jugador
     * Previene movimientos en direcciÃ³n opuesta
     */
    public void cambiarDireccion(Direcciones nuevaDireccion) {
        // No permitir direcciÃ³n opuesta
        if (nuevaDireccion == Direcciones.ARRIBA && direccionActual != Direcciones.ABAJO) {
            direccionActual = nuevaDireccion;
        } else if (nuevaDireccion == Direcciones.ABAJO && direccionActual != Direcciones.ARRIBA) {
            direccionActual = nuevaDireccion;
        } else if (nuevaDireccion == Direcciones.DERECHA && direccionActual != Direcciones.IZQUIERDA) {
            direccionActual = nuevaDireccion;
        } else if (nuevaDireccion == Direcciones.IZQUIERDA && direccionActual != Direcciones.DERECHA) {
            direccionActual = nuevaDireccion;
        }
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public int getPuntuacion() {
        return puntuacion;
    }
    
    public int getVidas() {
        return vidas;
    }
    
    public SerpienteServidor getSerpiente() {
        return serpiente;
    }
    
    public Direcciones getDireccionActual() {
        return direccionActual;
    }
    
    public boolean isInvulnerable() {
        return invulnerable;
    }
    
    public float getTiempoInvulnerabilidad() {
        return tiempoInvulnerabilidad;
    }
    
    // Setters
    public void setSerpiente(SerpienteServidor serpiente) {
        this.serpiente = serpiente;
    }
    
    public void setDireccionActual(Direcciones direccion) {
        this.direccionActual = direccion;
    }
    
    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }
    
    public void setVidas(int vidas) {
        this.vidas = vidas;
    }
}