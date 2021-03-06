package de.redlion.civilwar.units;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Cannon {

	public int id;
	public float angle = 0;
	public int angleSpin = 1;

	private float shotCooldownTime = 6f;
	private float shotCapacity = 1f;
	private float shotReloadRate = 1f;
	private float shotAimingTime = 4f;

	private float shots = shotCapacity;
	private float cooldown = 0;
	private float aimingTime = 0;
	
	protected float turnSpeed = 100.0f;
	protected float accel = 1.0f;
	protected float hitPoints = 1;

	private float delta = 0.0f;

	public float aliveTime = 0.0f;

	public Vector2 position = new Vector2();
	public Vector2 velocity = new Vector2();
	public Vector2 facing = new Vector2();
	public float height = 0;
	public float heightTarget = 0;
	public float heightInterpolator = 0;
	
	public boolean alive = true;
	public boolean stopped = false;
	
	public ArrayList<Vector3> path = new ArrayList<Vector3>();
	
	//render stuff
	public BoundingBox instanceBB;
	

	private float bounceSpeed = 5;
	public float bounce = MathUtils.random();
	public boolean inAir = false;
	public Vector2 lastPosition = new Vector2();

	public Cannon(int id, Vector2 position, Vector2 facing) {
		this.id = id;

		this.position.set(position);
		this.lastPosition.set(position);
		this.facing.set(facing);
		this.facing.rotate(angle);
		
		instanceBB = new BoundingBox();
	}
	
	public void update(float delta) {
		
		this.delta = delta;
		
		//death check
		if(!alive){
			if(angle < 90 && angle > -90) {
				angle = angle + delta * 100.f * angleSpin;
			}
			return;
		}
		
		
		
		cooldown = Math.max(0, cooldown - delta);
		aimingTime = Math.max(0, aimingTime - delta);
		shots = Math.min(shots + (shotReloadRate * delta), shotCapacity);
		
//		ai.update();

		if(!stopped) {

			velocity.scl( (float) Math.pow(0.97f, delta * 30.f));
			position.add(velocity.x * delta, velocity.y * delta);

		}		
		
		this.lastPosition.set(position);
	}
	
	public void turn(float direction) {

		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
		
		facing.rotate(direction * turnSpeed * delta).nor();

	}

	public void thrust() {
		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
		
		velocity.add(facing.x * accel * delta, facing.y * accel * delta);
	}
	
	public void thrust(float amount) {
		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
		
		velocity.add(facing.x * accel * delta, facing.y * accel * amount * delta);		
	}

	public void goTowardsOrAway(Vector2 targetPos, boolean forceThrust, boolean isAway) {
		Vector2 target_direction = targetPos.cpy().sub(position);
		if (isAway) {
			target_direction.scl(-1);
		}

		if (facing.crs(target_direction) > 0) {
			turn(1);
		} else {
			turn(-1);
		}

		if (forceThrust || facing.dot(target_direction) > 0) {
			thrust();
		}
	}
	
	// automatically thrusts and turns according to the target
	public void goTowards(Vector2 targetPos, boolean forceThrust) {
		goTowardsOrAway(targetPos, forceThrust, false);
	}

	public void goAway(Vector2 targetPos, boolean forceThrust) {
		goTowardsOrAway(targetPos, forceThrust, true);
	}
	
	public boolean isEmpty() {
		return shots < 1;
	}
	
	public void aim() {
		aimingTime = shotAimingTime;
	}

	public boolean isReloaded() {
		return shots == shotCapacity;
	}

	public boolean isCooledDown() {
		return cooldown == 0;
	}

	public boolean isAimed() {
		return aimingTime == 0;
	}
	
	public boolean isReadyToShoot() {
		return isCooledDown() && !isEmpty() && isAimed();
	}

	public void shoot() {
		if (cooldown == 0 && shots >= 1) {
			shots -= 1;
			cooldown = shotCooldownTime;
		}
	}
	
	public void hit() {
		hitPoints = hitPoints - 1;
		if(hitPoints <= 0) {
			alive = false;
		}
	}

}
