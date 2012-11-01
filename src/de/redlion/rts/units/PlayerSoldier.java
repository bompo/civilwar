package de.redlion.rts.units;

import com.badlogic.gdx.math.Vector2;

public class PlayerSoldier extends Soldier {

	public PlayerSoldier(int id, Vector2 position, Vector2 facing) {
		super(id, position, facing);
		// TODO Auto-generated constructor stub
	}
	
	public void update(float delta) {
		super.update(delta);
		if(!alive) return;
	}

}
