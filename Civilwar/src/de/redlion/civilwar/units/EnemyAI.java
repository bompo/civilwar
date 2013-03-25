package de.redlion.civilwar.units;

import com.badlogic.gdx.math.MathUtils;

public class EnemyAI extends DefaultAI {

	public EnemyAI(Soldier soldier) {
		super(soldier);
		retarget();
	}

	public void update() {	
		if (target == null || !target.alive || MathUtils.random() < 0.005f) {
			retarget();
		}
		
		super.update();		
	}
}
