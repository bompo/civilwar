package de.redlion.civilwar.units;

import com.badlogic.gdx.math.Vector2;

public class PlayerSoldier extends Soldier {
	
	public int dogTag;
	public boolean circled;

	public PlayerSoldier(int id, Vector2 position, Vector2 facing, int dogTag) {
		super(id, position, facing);
		// TODO Auto-generated constructor stub
		this.dogTag =  dogTag;
		circled = false;
	}
	
	public void update(float delta) {
		super.update(delta);
		if(!alive) return;
	}

}
