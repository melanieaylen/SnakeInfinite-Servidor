package entradas.salidas.teclado;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;

public class Entradas implements InputProcessor {

	public boolean abajo = false, arriba = false, izquierda = false, derecha = false;
	public boolean enter = false, pausa = false ; 
	
	//getters y setters
	public boolean isEnter() {
		return enter;
	}

	public void setEnter(boolean enter) {
		this.enter = enter;
	}

	@Override
	//SE PRESIONA LA TECLA
	public boolean keyDown(int keycode) {
		if(keycode == Keys.DOWN) {
			abajo = true; 
		}
		else if(keycode == Keys.UP) {
			arriba = true; 
		}
		
		if(keycode == Keys.ENTER) {
			enter = true; 
		}
		
		if(keycode == Keys.LEFT) {
			izquierda = true; 
		}
		
		if (keycode == Keys.RIGHT) {
			derecha = true; 
		}
		
		if(keycode == Keys.P) {
			pausa = true; 
		}
		return false;
	}

	public  boolean isAbajo() {
		return abajo;
	}

	public void setAbajo(boolean abajo) {
		this.abajo = abajo;
	}

	public boolean isArriba() {
		return arriba;
	}

	public void setArriba(boolean arriba) {
		this.arriba = arriba;
	}

	@Override
	//SE ALZA LA TECLA 
	public boolean keyUp(int keycode) {
		if(keycode == Keys.DOWN) {
			abajo = false; 
		}
		
		if(keycode == Keys.UP) {
			arriba = false; 
		}
		
		if(keycode == Keys.ENTER) {
			enter = false; 
		}
		
		if(keycode == Keys.LEFT) {
			izquierda = false; 
		}
		
		if(keycode == Keys.RIGHT) {
			derecha = false; 
		}
		
		if(keycode == Keys.P) {
			pausa = false; 
		}
		return false;
	}

	public boolean isPausa() {
		return pausa;
	}

	public void setPausa(boolean pausa) {
		this.pausa = pausa;
	}

	public boolean isIzquierda() {
		return izquierda;
	}

	public void setIzquierda(boolean izquierda) {
		this.izquierda = izquierda;
	}

	public boolean isDerecha() {
		return derecha;
	}

	public void setDerecha(boolean derecha) {
		this.derecha = derecha;
	}

	@Override
	public boolean keyTyped(char character) {

		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}

}
