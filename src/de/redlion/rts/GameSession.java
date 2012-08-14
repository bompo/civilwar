package de.redlion.rts;


public class GameSession {
		
	public static GameSession instance;

	public static GameSession getInstance() {
		if (instance == null) {
			instance = new GameSession();
		}
		return instance;
	}
	
	public void newSinglePlayerGame() {
	}


}
