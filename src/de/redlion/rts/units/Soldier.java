package de.redlion.rts.units;

import com.badlogic.gdx.math.Vector3;

public class Soldier {

	public int id;
	public Vector3 position;
	public Vector3 direction;
	public float angle = 0;
	public int angleSpin = 1;

	public Soldier(int id, Vector3 position) {
		this.id = id;
		this.position = position;
	}
	
	public void update(float delta) {
		angle = angle + delta * 20.f * angleSpin;
		if(angle > 15) {
			angleSpin = -1;
		}
		if(angle < -15) {
			angleSpin = 1;
		}
		
	}

}
