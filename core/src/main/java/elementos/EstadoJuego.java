package elementos;

public class EstadoJuego {
    private Serpiente serpiente;
    private GestorFrutas gestorFrutas;
    private int puntuacion;
    private float posElementosX;
    private float posElementosY;
    private Direcciones direccionActual;
    private float tiempo;
    private int vida;

    public EstadoJuego(Serpiente serpiente, GestorFrutas gestorFrutas, 
                       int puntuacion, float posElementosX, float posElementosY,
                       Direcciones direccionActual, float tiempo, int vida) {
        this.serpiente = serpiente;
        this.gestorFrutas = gestorFrutas;
        this.puntuacion = puntuacion;
        this.posElementosX = posElementosX;
        this.posElementosY = posElementosY;
        this.direccionActual = direccionActual;
        this.tiempo = tiempo;
        this.vida = vida;
    }

    // Getters
    public Serpiente getSerpiente() {
        return serpiente;
    }

    public GestorFrutas getGestorFrutas() {
        return gestorFrutas;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public float getPosElementosX() {
        return posElementosX;
    }

    public float getPosElementosY() {
        return posElementosY;
    }

    public Direcciones getDireccionActual() {
        return direccionActual;
    }

    public float getTiempo() {
        return tiempo;
    }

    public int getVida() {
        return vida;
    }
}