package de.redlion.rts;

import java.awt.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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
import com.dollar.Dollar;
import com.dollar.DollarListener;

import de.redlion.rts.units.PlayerSoldier;
import de.redlion.rts.units.Soldier;

public class DrawController extends InputAdapter{

	final OrthographicCamera camera;
	final Vector3 curr = new Vector3();
	final Vector2 last = new Vector2(0, 0);
	final Vector2 delta = new Vector2();
	
	final Vector2 lastPoint = new Vector2();
	
	final Dollar dollar = new Dollar(4);
	final DollarListener listener;
	final double MINSCORE = 0.82;
	
	Ray picker;

	public DrawController (final OrthographicCamera camera) {
		this.camera = camera;
		listener  = new DollarListener() {
			
			@Override
			public void dollarDetected(Dollar dollar) {
				// TODO Auto-generated method stub
//				Gdx.app.log("", dollar.getName() + " " + dollar.getScore());
				
				if((dollar.getName().equals("circle CW") || dollar.getName().equals("circle CCW")) && dollar.getScore() > MINSCORE) {
//					int[] rec = dollar.getBounds();
					
//					Rectangle rec = dollar.getBoundingBox();
					
//					int x = rec[0];
//					int y = rec[1];
//					int x1 = rec[2];
//					int y1 = rec[3];
					
//					y = -y + Gdx.graphics.getHeight();
//					y1 = -y1 + Gdx.graphics.getHeight();
					
					SinglePlayerGameScreen.circleRay = camera.getPickRay(dollar.getPosition().x,-dollar.getPosition().y+Gdx.graphics.getHeight());
					
					Vector3[] tmpArray = new Vector3[SinglePlayerGameScreen.path.values().size()];
					SinglePlayerGameScreen.path.values().toArray(tmpArray);
					
					float[] polyArray = new float[tmpArray.length * 2 - 2];
					
					//don't consider the last dummy point
					int j = 0;
					for(int i=0;i<tmpArray.length - 1;i++) {
						Vector3 tmp = tmpArray[i];

						polyArray[j] = tmp.x;
						polyArray[j+1] = tmp.z;
						
						j+=2;
						
					}
					
					Polygon poly = new Polygon(polyArray);

					boolean addpoly = false;
					
					for(Soldier s : GameSession.getInstance().soldiers) {
						if(s instanceof PlayerSoldier) {
							if(poly.contains(s.position.x, s.position.y)) {
								addpoly = true;
							}
						}
					}
					
					if(addpoly)
						SinglePlayerGameScreen.circles.add(poly);
					else if (SinglePlayerGameScreen.circles.isEmpty())
						SinglePlayerGameScreen.path.clear();
					else if (!SinglePlayerGameScreen.circles.isEmpty()){
						int pos = 0;
						for(int i=0;i<SinglePlayerGameScreen.path.keySet().size();i++) {
							Vector2 v = (Vector2) SinglePlayerGameScreen.path.keySet().toArray()[i];
							if(v.x == -1 && i >pos)
								pos = i + 1;
						}
						Object[] delete = new Object[SinglePlayerGameScreen.path.keySet().size() - pos];
						ArrayList tempList = new ArrayList();
						int k=0;
						for(int i=pos;i<SinglePlayerGameScreen.path.keySet().size();i++) {
							delete[k] = SinglePlayerGameScreen.path.keySet().toArray()[i];
							tempList.add(delete[k]);
							j++;
						}
						Gdx.app.log("", "NOT EMPTY");
					}
						
						
						
					Gdx.app.log("", "" + SinglePlayerGameScreen.circles.size());
					
					
//					Vector3 p1 = new Vector3(x,y,0);
//					Vector3 p2 = new Vector3(x1,y1,0);
//				
//					camera.unproject(p1);
//					camera.unproject(p2);
//					
//					Quaternion rotation = new Quaternion();
//					camera.combined.getRotation(rotation);
//					rotation.transform(p1);
//					rotation.transform(p2);
					
//					Gdx.app.log("", "" + x + " " + y);
					
//					float width = rec.width;
//					float height = rec.height;
					
//					double d = (width * width) + (height * height);
//					double d = p1.dst(p2);
//					
//					double rad = Math.sqrt(d) / 2;
//					float width = p2.x - p1.x;
//					float height = p2.y - p1.y;
//					
//					SinglePlayerGameScreen.circleRadius = (float) rad;
//					SinglePlayerGameScreen.sphereHeight = height;
//					SinglePlayerGameScreen.sphereWidth = width;
					
				}
				else {

					if(SinglePlayerGameScreen.circles.isEmpty()) {
						SinglePlayerGameScreen.path.clear();
					}
					else {
						int pos = 0;
						for(int i=0;i<SinglePlayerGameScreen.path.keySet().size();i++) {
							Vector2 v = (Vector2) SinglePlayerGameScreen.path.keySet().toArray()[i];
							if(v.x == -1 && i >pos)
								pos = i + 1;
						}
						Object[] delete = new Object[SinglePlayerGameScreen.path.keySet().size() - pos];
						ArrayList tempList = new ArrayList();
						int j=0;
						for(int i=pos;i<SinglePlayerGameScreen.path.keySet().size();i++) {
							delete[j] = SinglePlayerGameScreen.path.keySet().toArray()[i];
							tempList.add(delete[j]);
							j++;
						}
						SinglePlayerGameScreen.path.keySet().removeAll(tempList);
					}
						
					
				}
				
			}
		};
		
		dollar.setListener(listener);
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
			Vector2 temp = new Vector2(x, y);
			if(temp.dst(lastPoint) > 10) {
//				Gdx.app.log("point", temp.toString());
				Vector3 projected = new Vector3();
//				camera.unproject(projected.set(temp.x,temp.y,0));

				picker = camera.getPickRay(temp.x, temp.y);
				
				Intersector.intersectRayTriangles(picker, SinglePlayerGameScreen.renderMap.heightMap.map, projected);
				
				SinglePlayerGameScreen.path.put(temp,projected);
				lastPoint.set(temp);
				dollar.pointerDragged(x, y);
			}
		}
		return true;
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		last.set(0, 0);
		
		if(SinglePlayerGameScreen.paused && !Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
			//add dummy point
			SinglePlayerGameScreen.path.put(new Vector2(-1,-1),new Vector3(-1,-1,-1));
			dollar.pointerReleased(x, y);
			dollar.setActive(false);
		}
		
		return true;
	}
	
	@Override
	public boolean touchDown (int x, int y, int pointer, int button) {
		last.set(x, y);
		dollar.pointerPressed(x, y);
		dollar.setActive(true);
		return true;
	}
	
	@Override
	public boolean scrolled (int amount) {
		camera.zoom -= -amount * Gdx.graphics.getDeltaTime() / 50;
		camera.update();
		return true;
	}
}
