package elementos;

import utiles.Recursos;

public enum TipoFruta {
    MANZANA(Recursos.MANZANA, 1, 1.0f),
    BANANA(Recursos.BANANA, 2, 1.0f),
    CEREZA(Recursos.CEREZA, 3, 1.0f),
    SANDIA(Recursos.SANDIA, 5, 1.5f),
    FRUTILLA(Recursos.FRUTILLA, 2, 1.5f),
    NARANAJA(Recursos.NARANJA, 1, 1.0f),
    MORA(Recursos.MORA, 1, 1.0f),
    PIÑA(Recursos.PIÑA, 1, 1.0f),
    UVA(Recursos.UVA, 1, 1.0f);

    private final String rutaImagen;
    private final int puntos;
    private final float factorVelocidad; // Por si quieres efectos especiales

    TipoFruta(String rutaImagen, int puntos, float factorVelocidad) {
        this.rutaImagen = rutaImagen;
        this.puntos = puntos;
        this.factorVelocidad = factorVelocidad;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public int getPuntos() {
        return puntos;
    }

    public float getFactorVelocidad() {
        return factorVelocidad;
    }
}