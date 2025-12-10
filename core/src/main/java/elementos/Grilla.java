package elementos;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import utiles.Render;

public class Grilla {
    
    private int tamanioCelda;
    private Color color1;
    private Color color2;
    
    public Grilla(int tamanioCelda) {
        this.tamanioCelda = tamanioCelda;
        this.color1 = new Color(0.96f, 0.86f, 0.90f, 1f); // Rosa claro
        this.color2 = new Color(0.92f, 0.80f, 0.86f, 1f); // Rosa más oscuro
    }
    
    public void dibujarGrilla(OrthographicCamera camara) {
        // POSICION DE LA CAMARA
        float posCamX = camara.position.x;
        float posCamY = camara.position.y;
        //MITAD DE PANTALLA
        float medioX = (float) camara.viewportWidth / 2;
        float medioY = (float) camara.viewportHeight / 2f;
        
        // BORDES PANTALLA 
        float bordeIzquierdo = posCamX - medioX;
        float bordeDerecho = posCamX + medioX;
        float bordeInferior = posCamY - medioY;
        float bordeSuperior = posCamY + medioY;
        
        // CELDAS
        int inicioX = (int)(bordeIzquierdo / tamanioCelda) - 2; 
        int finX = (int)(bordeDerecho / tamanioCelda) + 2;   
        int inicioY = (int)(bordeInferior / tamanioCelda) - 2;
        int finY = (int)(bordeSuperior / tamanioCelda) + 2;
        
        // DIBUJAR CUADRADOS
        for (int x = inicioX; x <= finX; x++) {
            for (int y = inicioY; y <= finY; y++) {
                // TABLERO AJEDREZ 
                if ((x + y) % 2 == 0) {
                    Render.shaper.setColor(color1);
                } else {
                    Render.shaper.setColor(color2);
                }
                
                float posX = x * tamanioCelda;
                float posY = y * tamanioCelda;
                Render.shaper.rect(posX, posY, tamanioCelda, tamanioCelda);
            }
        }
    }
    
    public void setColores(Color color1, Color color2) {
        this.color1 = color1;
        this.color2 = color2;
    }
}