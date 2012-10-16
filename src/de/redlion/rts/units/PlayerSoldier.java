package de.redlion.rts.units;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

public class PlayerSoldier extends Soldier {

	public PlayerSoldier(int id, Vector3 position) {
		super(id, position);
		// TODO Auto-generated constructor stub
	}
	
	public void update(float delta) {
		super.update(delta);
		if(death) return;
		
		position.add(Vector3.Z.tmp().mul(delta));
	}

}
