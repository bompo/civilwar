package de.redlion.rts.units;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class Soldier {

	public int id;
	public Vector3 position;
	public Vector3 direction;
	public float angle = MathUtils.random(-90, 90);
	public int angleSpin = 1;
	
	public boolean death = false;

	public Soldier(int id, Vector3 position) {
		this.id = id;
		this.position = position;
	}
	
	public void update(float delta) {
		
		//death check
		if(death){
			if(angle < 90 && angle > -90) {
				angle = angle + delta * 100.f * angleSpin;
			}
			return;
		}
		
		angle = angle + delta * 20.f * angleSpin;
		if(angle > 15) {
			angleSpin = -1;
		}
		if(angle < -15) {
			angleSpin = 1;
		}
		
	}
	
	public void hit() {
		death = true;
	}

}