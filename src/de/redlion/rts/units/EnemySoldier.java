package de.redlion.rts.units;

import com.badlogic.gdx.math.Vector3;

public class EnemySoldier extends Soldier {

	public EnemySoldier(int id, Vector3 position) {
		super(id, position);
		// TODO Auto-generated constructor stub
	}
	
	public void update(float delta) {
		super.update(delta);
		if(death) return;
		
		position.add(Vector3.Z.tmp().mul(-delta));
	}


}
