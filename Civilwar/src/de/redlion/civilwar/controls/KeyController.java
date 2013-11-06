package de.redlion.civilwar.controls;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

import de.redlion.civilwar.SinglePlayerGameScreen;


public class KeyController extends InputAdapter{

	public boolean keyDown(int keycode) {
	
		
		if(keycode == Input.Keys.SPACE || keycode == Input.Keys.VOLUME_DOWN) {
			
			SinglePlayerGameScreen.paused = !SinglePlayerGameScreen.paused;
			
			if(SinglePlayerGameScreen.paused)
				SinglePlayerGameScreen.generateDoodles();
			
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
