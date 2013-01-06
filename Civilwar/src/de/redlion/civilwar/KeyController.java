package de.redlion.civilwar;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;


public class KeyController extends InputAdapter{

	public boolean keyDown(int keycode) {
	
		
		if(keycode == Input.Keys.SPACE) {
			
			SinglePlayerGameScreen.paused = !SinglePlayerGameScreen.paused;
			
		}
		
		return false;
	}
	
	public boolean keyUp(int keycode) {
		
		
		return false;
	}
	
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}	
	
}
