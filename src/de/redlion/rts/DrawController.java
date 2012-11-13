package de.redlion.rts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;

import javax.swing.plaf.basic.BasicScrollPaneUI.HSBChangeListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

import de.redlion.rts.units.PlayerSoldier;
import de.redlion.rts.units.Soldier;

public class DrawController extends InputAdapter{

	final OrthographicCamera camera;
	final Vector3 curr = new Vector3();
	final Vector2 last = new Vector2(0, 0);
	final Vector2 delta = new Vector2();
	
	final Vector2 lastPoint = new Vector2();
	
//	final Dollar dollar = new Dollar(4);
//	final DollarListener listener;
//	final double MINSCORE = 0.82;
	final float MAX_DISTANCE = 45.0f;
	
	Ray picker;

	public DrawController (final OrthographicCamera camera) {
		this.camera = camera;
	}

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		
		if(SinglePlayerGameScreen.paused && Gdx.input.isButtonPressed(Input.Buttons.RIGHT) ) {
			delta.set(x, y).sub(last);
			delta.mul(0.01f);
			Vector3 temp = new Vector3(-delta.x,delta.y,0);
			Quaternion rotation = new Quaternion();
			camera.combined.getRotation(rotation);
			rotation.transform(temp);
			camera.translate(temp);
			camera.update();
			last.set(x, y);
		}
		else {
			y = -y + Gdx.graphics.getHeight();
			
			x = Math.max(Math.min(x, Gdx.graphics.getWidth()), 0);
			y = Math.max(Math.min(y, Gdx.graphics.getHeight()), 0);
			
			Gdx.app.log("", x + " " + y);
			Vector2 temp = new Vector2(x, y);
			if(temp.dst(lastPoint) > 10 || SinglePlayerGameScreen.currentDoodle.isEmpty()) {
				SinglePlayerGameScreen.currentDoodle.add(temp);
				lastPoint.set(temp);
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		last.set(0, 0);
		
		if(SinglePlayerGameScreen.paused && !Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {

			if(SinglePlayerGameScreen.currentDoodle.size()%2 != 0)
				SinglePlayerGameScreen.currentDoodle.add(SinglePlayerGameScreen.currentDoodle.get(SinglePlayerGameScreen.currentDoodle.size() - 1));
			
			ArrayList<Vector3> tempList = new ArrayList<Vector3>();
			
			for(Vector2 temp : SinglePlayerGameScreen.currentDoodle) {
			
				Vector3 projected = new Vector3();
	
				picker = camera.getPickRay(temp.x, temp.y);
				
				Intersector.intersectRayTriangles(picker, SinglePlayerGameScreen.renderMap.heightMap.map, projected);
				
				tempList.add(projected);
			}
			
			if(SinglePlayerGameScreen.currentDoodle.size() > 11) {
				if(SinglePlayerGameScreen.circles.isEmpty() && circleTest((ArrayList<Vector2>) SinglePlayerGameScreen.currentDoodle.clone())) {
					
					float[] points = new float[tempList.size() * 2 + 2];
					
					int i = 0;
					for(Vector3 v : tempList) {
					
						points[i] = v.x;
						points[i+1] = v.z;
						
						i+=2;
						
					}
					
					points[i] = tempList.get(0).x;
					points[i+1] = tempList.get(0).z;
					
					
					Polygon poly = new Polygon(points);
					
					ArrayList<PlayerSoldier> so = soldierTest(poly);
					
					if(!so.isEmpty()) {
						SinglePlayerGameScreen.doodles.put(poly, (ArrayList<Vector2>) SinglePlayerGameScreen.currentDoodle.clone());
						SinglePlayerGameScreen.circles.put(poly, so);
						
						Gdx.app.log("POLYGON ADDED: ", "Polygon Number: " + SinglePlayerGameScreen.circles.size() + " with id " + poly.toString());
					}
					else
						SinglePlayerGameScreen.currentDoodle.clear();
				}
				else if(!SinglePlayerGameScreen.circles.isEmpty()) {
					
					if(circleTest((ArrayList<Vector2>) SinglePlayerGameScreen.currentDoodle.clone())) {
					
						float[] points = new float[tempList.size() * 2 + 2];
						
						int i = 0;
						for(Vector3 v : tempList) {
						
							points[i] = v.x;
							points[i+1] = v.z;
							
							i+=2;
							
						}
						
						points[i] = tempList.get(0).x;
						points[i+1] = tempList.get(0).z;
						
						Polygon poly = new Polygon(points);
						
						boolean disjoint = checkDisjoint(poly);
						
						if(disjoint) {
							ArrayList<PlayerSoldier> so = soldierTest(poly);
							if(!so.isEmpty()) {
								SinglePlayerGameScreen.doodles.put(poly, (ArrayList<Vector2>) SinglePlayerGameScreen.currentDoodle.clone());
								SinglePlayerGameScreen.circles.put(poly, so);
								
								Gdx.app.log("POLYGON ADDED: ", "Polygon Number: " + SinglePlayerGameScreen.circles.size() + " with id " + poly.toString());
							}
							else
								SinglePlayerGameScreen.currentDoodle.clear();
						}
						else
							SinglePlayerGameScreen.currentDoodle.clear();
					}
					else {
						boolean deletedoodle = true;
						for(Polygon p : SinglePlayerGameScreen.circles.keySet()) {
							if(p.contains(tempList.get(0).x, tempList.get(0).z) && SinglePlayerGameScreen.paths.get(p) == null) {
								SinglePlayerGameScreen.doodles.put(p,(ArrayList<Vector2>) SinglePlayerGameScreen.currentDoodle.clone());
								SinglePlayerGameScreen.paths.put(p, tempList);

								Gdx.app.log("PATH ADDED: ", "Path Number: " + SinglePlayerGameScreen.paths.size() + " from Polygon " + p.toString());
								Gdx.app.log("","" + SinglePlayerGameScreen.currentDoodle.get(0));
								deletedoodle = false;
								break;
							}
						}
						if(deletedoodle)
							SinglePlayerGameScreen.currentDoodle.clear();
					}
					
				}
				else
					SinglePlayerGameScreen.currentDoodle.clear();
			}
			else
				SinglePlayerGameScreen.currentDoodle.clear();
			
			tempList.clear();
		}
		
		return true;
	}
	
	@Override
	public boolean touchDown (int x, int y, int pointer, int button) {
		last.set(x, y);
	
		SinglePlayerGameScreen.currentDoodle.clear();

		return true;
	}
	
	@Override
	public boolean scrolled (int amount) {
		camera.zoom -= -amount * Gdx.graphics.getDeltaTime() / 50;
		camera.update();
		return true;
	}
	
	public boolean circleTest(ArrayList<Vector2> doodle) {
		
		if(doodle.get(0).dst(doodle.get(doodle.size() -1)) < MAX_DISTANCE)
			return true;
		else
			return false;
	}
	
	public ArrayList<PlayerSoldier> soldierTest(Polygon polygon) {
		
		ArrayList<PlayerSoldier> soldiers = new ArrayList<PlayerSoldier>();
		
		for(Soldier s : GameSession.getInstance().soldiers) {
			if(s instanceof PlayerSoldier) {
				PlayerSoldier p = (PlayerSoldier) s;
				if(polygon.contains(p.position.x, p.position.y)) {
					Gdx.app.log("", p.dogTag + "");
					soldiers.add(p);
				}
			}
		}
		
		return soldiers;
	}
	
	public boolean checkDisjoint(Polygon polygon) {
		
		float[] list = polygon.getWorldVertices();
		
		for(int j=0;j<list.length;j+=2) {
			for(Polygon po : SinglePlayerGameScreen.circles.keySet()) {
				if(po.contains(list[j], list[j+1]))
					return false;
			}
		}
		
		ArrayList<Polygon> contained = new ArrayList<Polygon>();
		for(Polygon po : SinglePlayerGameScreen.circles.keySet()) {
			list = po.getWorldVertices();
			for(int j=0;j<list.length;j+=2) {
				if(polygon.contains(list[j], list[j+1]))
					contained.add(po);
			}
		}
		
		if(!contained.isEmpty()) {
			for(Polygon pop : contained) {
				list = polygon.getWorldVertices();
				for(int j=0;j<list.length;j+=2) {
					if(pop.contains(list[j], list[j+1]))
						return false;
				}
				SinglePlayerGameScreen.circles.remove(pop);
				SinglePlayerGameScreen.doodles.remove(pop);
			}
			
		}
		
		return true;
		
	}
	
}
