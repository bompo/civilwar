package de.redlion.rts;

import java.util.ArrayList;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import de.redlion.rts.units.EnemySoldier;
import de.redlion.rts.units.PlayerSoldier;

public class GameSession {

	public static GameSession instance;

	public ArrayList<PlayerSoldier> playerSoldiers = new ArrayList<PlayerSoldier>();
	public ArrayList<EnemySoldier> enemySoldiers = new ArrayList<EnemySoldier>();

	public static GameSession getInstance() {
		if (instance == null) {
			instance = new GameSession();
		}
		return instance;
	}

	public void newSinglePlayerGame() {
		playerSoldiers = new ArrayList<PlayerSoldier>();
		enemySoldiers = new ArrayList<EnemySoldier>();

		for (int i = 0; i < 20; i++) {
			playerSoldiers.add(new PlayerSoldier(i, new Vector3(MathUtils.sin(i) * 1.f + i * 0.1f, 0, -20.f + MathUtils.sin(i) / 4.f + i * 0.1f)));
		}
		
		for (int i = 0; i < 20; i++) {
			enemySoldiers.add(new EnemySoldier(i, new Vector3(MathUtils.sin(i) * 1.f + i * 0.1f, 0, 20.f + MathUtils.sin(i) / 4.f + i * 0.1f)));
		}
	}

}
