package de.redlion.civilwar.units;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class PlayerSoldier extends Soldier {
	
	public int dogTag;
	public boolean circled;
	
	public ArrayList<Vector3> wayPoints = new ArrayList<Vector3>();

	public PlayerSoldier(int id, Vector2 position, Vector2 facing, int dogTag) {
		super(id, position, facing);
		this.dogTag =  dogTag;
		this.ai = new DefaultAI(this);
		circled = false;
	}
	
	public void update(float delta) {
		super.update(delta);
		
		if(!wayPoints.isEmpty())
			followPath();
		
		if(!alive) return;
	}
	
	public void followPath() {
		
		Vector2 next = null;
		Vector3 next3D = new Vector3();
		
		for(Vector3 point : wayPoints) {
			
			Vector2 point2D = new Vector2(point.x, point.z);
			
			if(next == null || position.dst(next) > position.dst(point2D)) {
				next = point2D;
				next3D = point;
			}
		}
		
		if(position.dst(next) > 0.3f)
			super.goTowards(next, false);
		else {
			int index = wayPoints.indexOf(next3D);
			
			if(index != -1) {
				
				if(index >= wayPoints.size() -1)
					return;
				
				for(int i=0;i<=index;i++) {
					if(i >= wayPoints.size() -1) {
						wayPoints.clear();
						return;
					}
					else
						wayPoints.remove(i);
				}
				
			}
			
		}	
		
		
	}

}
