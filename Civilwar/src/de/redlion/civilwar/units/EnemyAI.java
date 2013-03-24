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

		if (target != null) {			
			to_target.set(target.position.x - soldier.position.x, target.position.y - soldier.position.y);
			float dist_squared = to_target.dot(to_target);

			if (state.equals(STATE.RUNNING)) {
				// run away until you have full ammo and are far enough away
				boolean too_close = dist_squared < Math.pow(run_distance, 2);
				// if you're too close to the target then turn away
				if (too_close) {
					soldier.goAway(target.position, true);
				} else {
					soldier.thrust();
				}

				if (!soldier.isEmpty() && !too_close) {
					state = STATE.MOVING;
				}
			}
			if (state.equals(STATE.MOVING)) {
				// go towards the target and attack!
				soldier.goTowards(target.position, true);

				// is target enemy in range?
				if (dist_squared <= shot_range * shot_range && to_target.dot(soldier.facing) > 0 && Math.pow(to_target.dot(soldier.facing), 2) > 0.97 * dist_squared) {
					if (soldier.isReloaded()) {
						state = STATE.AIMING;
						soldier.aim();
					} else {
						state = STATE.RUNNING;
					}
				} 				

				// if out of shots then run away
				if (soldier.isEmpty()) {
					state = STATE.RUNNING;
				}
			}
			if (state.equals(STATE.AIMING)) {
				if(soldier.isReadyToShoot()) {
					soldier.shoot();
				}
				
			}
		}
	}
}
