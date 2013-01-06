package de.redlion.civilwar.units;

import com.badlogic.gdx.math.Vector2;

public class EnemySoldier extends Soldier {

	public EnemySoldier(int id, Vector2 position, Vector2 facing) {
		super(id, position, facing);
		// TODO Auto-generated constructor stub
	}
	
	public void update(float delta) {
		super.update(delta);
		if(!alive) return;
	}


}
