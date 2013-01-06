package de.redlion.civilwar;

import com.badlogic.gdx.Game;

public class Civilwar extends Game {
	@Override 
	public void create () {

		Configuration.getInstance().setConfiguration();
		setScreen(new SinglePlayerGameScreen(this));
	}
}
