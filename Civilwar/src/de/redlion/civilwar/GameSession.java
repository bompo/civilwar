package de.redlion.civilwar;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import de.redlion.civilwar.collision.Octree;
import de.redlion.civilwar.units.Cannon;
import de.redlion.civilwar.units.EnemySoldier;
import de.redlion.civilwar.units.PlayerSoldier;
import de.redlion.civilwar.units.Soldier;

public class GameSession {

	public static GameSession instance;

	public Array<Soldier> soldiers = new Array<Soldier>();
	public Array<Cannon> cannons = new Array<Cannon>();
	public Octree tree = new Octree(30,3);

	public static GameSession getInstance() {
		if (instance == null) {
			instance = new GameSession();
		}
		return instance;
	}

	public void newSinglePlayerGame() {
		soldiers = new Array<Soldier>();

		for (int i = 0; i < 2; i++) {
			Cannon s = new Cannon(1, new Vector2(1,1), new Vector2(0, 1));
			cannons.add(s);
		}
		
		for (int i = 0; i < 20; i++) {
			Soldier s = new PlayerSoldier(1, new Vector2(MathUtils.sin(i) * 1.f + i * 0.2f,-10.f + MathUtils.sin(i) / 4.f + i * 0.1f), new Vector2(0, 1),i);
			soldiers.add(s);
			tree.insertObject(s);
		}
		
		for (int i = 0; i < 20; i++) {
			soldiers.add(new EnemySoldier(2, new Vector2(MathUtils.sin(i) * 1.f + i * 0.2f, 10.f + MathUtils.sin(i) / 4.f + i * 0.1f), new Vector2(0, -1)));
		}
		
	}

}
