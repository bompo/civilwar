package de.redlion.civilwar.controls;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

import de.redlion.civilwar.SinglePlayerGameScreen;


public class KeyController extends InputAdapter{

        private SinglePlayerGameScreen singlePlayerGameScreen;
        
        public KeyController(SinglePlayerGameScreen singlePlayerGameScreen) {
                this.singlePlayerGameScreen = singlePlayerGameScreen;
        }
        
        public boolean keyDown(int keycode) {        
                
                if(keycode == Input.Keys.SPACE || keycode == Input.Keys.VOLUME_DOWN) {
                        
                        SinglePlayerGameScreen.paused = !SinglePlayerGameScreen.paused;
                        
                        if(SinglePlayerGameScreen.paused)
                                singlePlayerGameScreen.generateDoodles();
                        else
                        	singlePlayerGameScreen.drawController.divideCircles();
                        
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