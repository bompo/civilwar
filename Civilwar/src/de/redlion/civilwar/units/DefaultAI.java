package de.redlion.civilwar.units;

import com.badlogic.gdx.math.Vector2;

import de.redlion.civilwar.Targeting;

public class DefaultAI {
	
	public enum STATE {
		IDLE, MOVING, RUNNING, SHOOTING, AIMING;
	}
	
	public STATE state = STATE.IDLE;
	
	// shot range
	protected float shot_range = 7;

	// try to stay this far away when you're out of ammo
	protected float run_distance = 14f;

	public Soldier target;
	
	//recycle vars
	Vector2 to_target = new Vector2();

	protected Soldier soldier;

	public DefaultAI(Soldier soldier) {
		this.soldier = soldier;
		retarget();
	}

	public void retarget() {
		target = Targeting.getNearestOfType(soldier, 0);
		state = STATE.MOVING;
	}

	public void target(Soldier soldier) {
		target = soldier;
		state = STATE.MOVING;
	}
	
	public void update() {

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
