package de.redlion.rts.render;

import com.badlogic.gdx.math.Vector2;

public class PathPoint {
	
	public float x;
	public float y;
	public float progress;
	public float xChange;
	public float yChange;
	public Vector2 point;
	public float length;
	public float angle;
	public PathPoint next;
	
	public PathPoint (float x, float y) {
		this.x = x;
		this.y = y;
		progress = -1;
	}
	
	public void setPoint (Vector2 point) {
		this.x = point.x;
		this.y = point.y;
		progress = -1;
	}
	
}
