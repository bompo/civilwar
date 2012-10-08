package de.redlion.rts.render;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

public class LinePath2D {
	
	public float angle;
	public float totalLength;
	public List<PathPoint> points;
	public float progress;
	
	private PathPoint _first;
	private PathPointPool _pointPool;
	
	
	public LinePath2D () {
		points = new ArrayList<PathPoint>();
		totalLength = 0f;
		angle = 0f;
		_pointPool = new PathPointPool();
	}
	
	public void appendPoint (Vector2 point) {
		insertPoint(point, points.size(), false);
	}
	
	public void insertMultiplePoints(List<Vector2> points, int index) {
		int len = points.size();
		for (int i = 0; i < len; i++) {
			insertPoint(points.get(i), index + i, true);
		}
	}
	
	public Vector2 getPointAtProgress (float progress) {
		progress = Math.abs(progress);
		
		Vector2 point = new Vector2();
		if (progress > 1) progress = 1;
		PathPoint pp = _first;
		if (pp.next == null) return null;
		if (pp.next.progress < 0) return null;
		
		while (pp.next != null && pp.next.progress < progress) {
			pp = pp.next;
		}
		
		if (pp != null) {
			angle = pp.angle;
			float pathProg = (progress - pp.progress) / (pp.length / totalLength);
			point.x = pp.x + pathProg * pp.xChange;
			point.y = pp.y + pathProg * pp.yChange;
			
			return point;
			
		}
		return null;
	}
	
	public void renderObjectAt(Plane target, float progress) {
		
		if (progress > 1) {
			progress -= (int) progress;
		} else if (progress < 0) {
			progress -= (int) progress - 1;
		}
		
		if (_first == null) {
			return;
		}
		
		this.progress = progress;
		
		PathPoint pp = _first;
		while (pp.next != null && pp.next.progress < progress) {
			pp = pp.next;
		}
		
		if (pp != null) {
			angle = pp.angle;
			float pathProg = (progress - pp.progress) / (pp.length / totalLength);
			float px = pp.x + pathProg * pp.xChange;
			float py = pp.y + pathProg * pp.yChange;
			target.x = px;
			target.y = py;		
		}
	}
	
	public void dispose () {
		points.clear();
		_pointPool.dispose();
	}
	
	
	public List<PathPoint> getPoints() {
		return points;
	}
	
	public void setPoints(List<Vector2> value) {
		
		points.clear();
		insertMultiplePoints(value, 0);
	}
	
	
	private void insertPoint(Vector2 point, int index, boolean skipOrganize)  {
		
		PathPoint p = _pointPool.getObject();
		p.setPoint(point);
		
		if (points.size() == 0) {
			points.add(p);
		} else {
			points.add(index, p);
		}
		
		if (!skipOrganize) {
			organize();
		}
	}
	
	private void organize() {
		totalLength = 0;
		int last = points.size() - 1;
		if (last == -1) {
			_first = null;
		} else if (last == 0) {
			_first = points.get(0);
			_first.progress = _first.xChange = _first.yChange = _first.length = 0;
			return;
		}
		PathPoint pp;
		for (int i = 0; i <= last; i++) { 
			if (points.get(i) != null) {
				pp = points.get(i);
				pp.x = pp.x;
				pp.y = pp.y;
				if (i == last) {
					pp.length = 0;
					pp.next = null;
				} else {
					pp.next = points.get(i + 1);
					pp.xChange = pp.next.x - pp.x;
					pp.yChange = pp.next.y - pp.y;
					pp.length = (float) Math.sqrt(pp.xChange * pp.xChange + pp.yChange * pp.yChange);
					totalLength += pp.length;
				}
			}
		}
		_first = pp = points.get(0);
		float curTotal = 0f;
		while (pp != null) {
			pp.progress = curTotal / totalLength;
			curTotal += pp.length;
			pp = pp.next;
		}
		updateAngles();
	}
	
	private void updateAngles() {
		PathPoint pp = _first;
		while (pp != null) {
			pp.angle = (float) Math.atan2(pp.yChange, pp.xChange ) * (180 / (float) Math.PI);
			pp = pp.next;
		}
		
	}
	
}
