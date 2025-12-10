package elementos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GestorFrutas {
    private List<Fruta> frutas;
    private int tamanioElementos;
    private Random random;

    public GestorFrutas(int tamanioElementos) {
        this.tamanioElementos = tamanioElementos;
        this.frutas = new ArrayList<>();
        this.random = new Random();
    }

    /**
     * Inicializa todas las frutas definidas en el enum
     */
    public void inicializarFrutas(Serpiente serpiente) {
        for (TipoFruta tipo : TipoFruta.values()) {
            Fruta fruta = new Fruta(0, 0, tamanioElementos, tamanioElementos, tipo);
            moverFrutaAleatoria(fruta, serpiente);
            frutas.add(fruta);
        }
    }

    /**
     * Verifica colisiones y devuelve la fruta colisionada (si existe)
     */
    public Fruta verificarColisiones(Serpiente serpiente) {
        for (Fruta fruta : frutas) {
            if (fruta.getPosX() == serpiente.getPosX() && 
                fruta.getPosY() == serpiente.getPosY()) {
                return fruta;
            }
        }
        return null;
    }

    /**
     * Reubica una fruta después de ser comida
     */
    public void reubicarFruta(Fruta fruta, Serpiente serpiente) {
        moverFrutaAleatoria(fruta, serpiente);
    }

    /**
     * Dibuja todas las frutas
     */
    public void dibujarTodas() {
        for (Fruta fruta : frutas) {
            fruta.dibujar();
        }
    }

    private void moverFrutaAleatoria(Fruta fruta, Serpiente serpiente) {
        int rangoMin = -10, rangoMax = 20;
        float nuevaX, nuevaY;

        do {
            int offsetX = random.nextInt(rangoMax - rangoMin + 1) + rangoMin;
            int offsetY = random.nextInt(rangoMax - rangoMin + 1) + rangoMin;

            nuevaX = serpiente.getPosX() + (offsetX * tamanioElementos);
            nuevaY = serpiente.getPosY() + (offsetY * tamanioElementos);

        } while (serpiente.colisionConPosicion(nuevaX, nuevaY));

        fruta.setPosX(nuevaX);
        fruta.setPosY(nuevaY);
    }

    public void dispose() {
        for (Fruta fruta : frutas) {
            fruta.dispose();
        }
        frutas.clear();
    }

    public List<Fruta> getFrutas() {
        return frutas;
    }
}