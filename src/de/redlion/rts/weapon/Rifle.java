package de.redlion.rts.weapon;

import com.badlogic.gdx.math.MathUtils;

public class Rifle {

	public float angle = MathUtils.random(-90, 90);
	public int angleSpin = 1;
	
	public boolean death = false;

	public Rifle() {
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
		if(angle > 10) {
			angleSpin = -1;
		}
		if(angle < -10) {
			angleSpin = 1;
		}
		
	}
	
	public void hit() {
		death = true;
	}

}
