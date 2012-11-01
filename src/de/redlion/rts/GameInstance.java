package de.redlion.rts;

import com.badlogic.gdx.utils.Array;

import de.redlion.rts.units.Soldier;

public class GameInstance {

	public static GameInstance instance;

	public static GameInstance getInstance() {
		if (instance == null) {
			instance = new GameInstance();
		}
		return instance;
	}

	public void resetGame() {
	}
}