package de.redlion.civilwar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.StillModelNode;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import de.redlion.civilwar.controls.DrawController;
import de.redlion.civilwar.controls.OrthoCamController;
import de.redlion.civilwar.render.RenderDebug;
import de.redlion.civilwar.render.RenderMap;
import de.redlion.civilwar.units.PlayerSoldier;
import de.redlion.civilwar.units.Soldier;

public class SinglePlayerGameScreen extends DefaultScreen {

	float startTime = 0;

	SpriteBatch batch;
	SpriteBatch fadeBatch;
	Sprite blackFade;
	
	BitmapFont font;
	
	public static RenderMap renderMap;
	RenderDebug renderDebug;

	float fade = 1.0f;
	boolean finished = false;

	float delta;
	
	OrthoCamController camController;
	DrawController  drawController;
	KeyController keyController;
	InputMultiplexer multiplexer;
	
	// GLES20
	Matrix4 model = new Matrix4().idt();
	Matrix4 normal = new Matrix4().idt();
	Matrix4 tmp = new Matrix4().idt();
	
	float edgeScrollingSpeed;
	
	ShapeRenderer r;
	StillModel sphere;
	
	public static boolean paused = false;

	public static HashMap<Polygon, ArrayList<Vector3>> paths;  //maps projected polygons to their associated paths
	public static HashMap<Polygon, ArrayList<PlayerSoldier>> circles; //maps projected polygons to what soldiers they encompass
	public static HashMap<Polygon, ArrayList<Vector2>> doodles; //maps polygons to what has been drawn on screen
	public static ArrayList<Polygon> circleHasPath;
	public static ArrayList<Vector2> currentDoodle; //what is currently being drawn
//	public static ArrayList<Vector2> currentDoodleCollisionPoints; //doodle coordinates in real screen values
	
//	public static Ray circleRay;

	public SinglePlayerGameScreen(Game game) {
		super(game);
		
		GameSession.getInstance().newSinglePlayerGame();
		
		//refresh references 
		//TODO Observer Pattern for newGame
		renderMap = new RenderMap();
		renderDebug = new RenderDebug();
		
		sphere = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/sphere.g3dt"));		
		
		Material mat = new Material("sphere", new TextureAttribute(new Texture(Gdx.files.internal("data/black.png"), true), 0, TextureAttribute.diffuseTexture));
		sphere.setMaterial(mat);
		
//		Gdx.input.setInputProcessor(new SinglePlayerControls(player));

		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		
		blackFade = new Sprite(	new Texture(Gdx.files.internal("data/black.png")));
		fadeBatch = new SpriteBatch();
		fadeBatch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
		
		font = Resources.getInstance().font;
		font.setScale(1);
		
		r = new ShapeRenderer();
		r.setProjectionMatrix(new Matrix4().setToOrtho2D(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight()));
		
		edgeScrollingSpeed = 1.0f;
		
		circles = new  LinkedHashMap<Polygon, ArrayList<PlayerSoldier>>();
		paths = new LinkedHashMap<Polygon, ArrayList<Vector3>>();
		doodles = new LinkedHashMap<Polygon, ArrayList<Vector2>>();
		circleHasPath = new ArrayList<Polygon>();
		currentDoodle = new ArrayList<Vector2>();
//		currentDoodleCollisionPoints = new ArrayList<Vector2>();
		
		initRender();
	}

	public void initRender() {
		Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	
		initRender();
		renderMap = new RenderMap();
		
		camController = new OrthoCamController(renderMap.cam);
		keyController = new KeyController();
		drawController = new DrawController(renderMap.cam);
		multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(camController);
		multiplexer.addProcessor(keyController);

		Gdx.input.setInputProcessor(multiplexer);
	}

	@Override
	public void show() {
	}
	
	private float deltaCount = 0;	
	@Override
	public void render(float deltaTime) {
		deltaCount += deltaTime;
		if(deltaCount > 0.01) {
			deltaCount = 0;
			renderFrame(0.02f);
		}
		
		if(paused) {
			multiplexer = new InputMultiplexer();
			multiplexer.removeProcessor(camController);
			multiplexer.addProcessor(drawController);
			multiplexer.addProcessor(keyController);
			Gdx.input.setInputProcessor(multiplexer);
		} else {
			doodles.clear();
			currentDoodle.clear();
			multiplexer = new InputMultiplexer();
			multiplexer.removeProcessor(drawController);
			multiplexer.addProcessor(camController);
			multiplexer.addProcessor(keyController);
			Gdx.input.setInputProcessor(multiplexer);
		}
		
	}

	public void renderFrame(float deltaTime) {

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		delta = Math.min(0.1f, deltaTime);

		startTime += delta;

		if(!paused) {
			updateUnits();
			updatePolygons();
		}

		collisionTest();
		updateAI();
		
		renderMap.render();

		if (Configuration.getInstance().debug) {
			renderDebug.render(renderMap.cam);
		}

		// FadeInOut
		if (!finished && fade > 0) {
			fade = Math.max(fade - (delta), 0);
			fadeBatch.begin();
			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g,
					blackFade.getColor().b, fade);
			blackFade.draw(fadeBatch);
			fadeBatch.end();
		}

		if (finished) {
			fade = Math.min(fade + (delta), 1);
			fadeBatch.begin();
			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g,
					blackFade.getColor().b, fade);
			blackFade.draw(fadeBatch);
			fadeBatch.end();
			if (fade >= 1) {
				Gdx.app.exit();
			}
		}
		
		//draw waypoints for debugging
//		for(ArrayList<Vector3> pathPoints : paths.values()) {
//			
//			if(!pathPoints.isEmpty()) {
//				
//				for(Vector3 pPoint : pathPoints) {
//
//					StillModelNode node = new StillModelNode();
//					node.matrix.translate(pPoint);
//					node.matrix.scl(0.05f);
//					RenderMap.protoRenderer.draw(sphere, node);
//					
//				}
//				
//			}
//			
//		}
		
		//draw polygons for debugging
		for(Polygon pol : circles.keySet()) {
			
			float[] vertices = pol.getTransformedVertices();
			
			if(vertices.length > 0) {
				
				for(int i=0; i<vertices.length;i+=2) {

					StillModelNode node = new StillModelNode();
					node.matrix.translate(vertices[i],0,vertices[i+1]);
					node.matrix.scl(0.1f);
					RenderMap.protoRenderer.draw(sphere, node);
					
				}
				
			}
			
		}
		
		
		if(paused) {
			batch.begin();
			font.draw(batch, "PAUSED", 700, 35);
			batch.end();
			
			for(Polygon pol : doodles.keySet()) {
				
				ArrayList<Vector2> doodle = doodles.get(pol);
				
				if(!doodle.isEmpty()) {
					r.setColor(1, 0, 0, 1);
					r.begin(ShapeType.Line);
					
					Vector2 v0 = doodle.get(0);
					
					for(Vector2 v1 : doodle) {
						
						r.line(v0.x, v0.y, v1.x, v1.y);
						v0 = v1;
						
					}
					r.end();
					
					
					if(paths.containsKey(pol)) {
						Sprite arrowhead = Resources.getInstance().arrowhead;
						arrowhead.setPosition(doodle.get(doodle.size()-3).x - arrowhead.getOriginX(),doodle.get(doodle.size()-1).y - arrowhead.getOriginY());

						Vector2 a = doodle.get(doodle.size()-1).cpy();
						Vector2 b = doodle.get(doodle.size()-3).cpy();
						Vector2 c = a.cpy().sub(b);
						c = a.cpy().add(c);
						
						float dot = Math.abs(a.cpy().dot(c));
						float angle = dot / (a.len() * c.len());
						
						angle = (float) Math.acos(angle);
						angle = (float) Math.toDegrees(angle);
						
//						if(Float.isNaN(angle))
//							angle = 0;
						
//						r.end();
//						r.begin(ShapeType.Circle);
//						r.circle(a.x, a.y, 7);
//						r.circle(b.x, b.y, 7);
						
						
						if(a.x < b.x) {
							if(a.y < b.y)
								angle+=90;
							if(a.y == b.y)
								angle+=45;
						}
						else if(a.x > b.x){
							if(a.y < b.y)
								angle+=180;
							else if(a.y > b.y)
								angle+=270;
							
							if(a.y == b.y)
								angle+=225;
						}
						else {
							if(a.y > b.y)
								angle+=315;
							else
								angle+=135;
						}
						
						
//						Gdx.app.log("", angle + "");
						arrowhead.setRotation(angle);
						batch.begin();
						arrowhead.draw(batch);
						batch.end();
					}
				}
				
				
			}
			
			if(!currentDoodle.isEmpty()) {
				r.setColor(1, 0, 0, 1);
				r.begin(ShapeType.Line);
				
				Vector2 v0 = currentDoodle.get(0);
				
				for(Vector2 v1 : currentDoodle) {
					
					r.line(v0.x, v0.y, v1.x, v1.y);
					v0 = v1;
					
				}
				
				r.end();
				
				
				/* edge scrolling */
				
				if(v0.x > Gdx.graphics.getWidth() - 50) {
					
					Vector3 temp = new Vector3(Vector3.Z);
					temp.mul(0.01f * Constants.MOVESPEED);
					temp.mul(edgeScrollingSpeed);
					Quaternion rotation = new Quaternion();
					drawController.camera.combined.getRotation(rotation);
					rotation.transform(temp);
					drawController.camera.translate(temp);
					drawController.camera.update();
					
					if(edgeScrollingSpeed < 25)
						edgeScrollingSpeed += Gdx.graphics.getDeltaTime() * Constants.EDGE_SCROLL_SPEED;
					
					for(ArrayList<Vector2> doodle : doodles.values()) {
						
						
						ArrayList<Vector2> newDoodle = new ArrayList<Vector2>();
						for(Vector2 v : doodle) {
							
							v.add(new Vector2(-1 * edgeScrollingSpeed,0));
									
							newDoodle.add(v);
							
						}
						
						doodle.clear();
						doodle.addAll(newDoodle);
						
					}
					
					ArrayList<Vector2> newCurrentDoodle = new ArrayList<Vector2>();
					for(int i = 0; i<currentDoodle.size() -1; i++) {
						
						Vector2 v = currentDoodle.get(i);
						
						v.add(new Vector2(-1 * edgeScrollingSpeed,0));
						
						newCurrentDoodle.add(v);
						
					}
					
					newCurrentDoodle.add(currentDoodle.get(currentDoodle.size()-1));
					currentDoodle.clear();
					currentDoodle.addAll(newCurrentDoodle);
					
				}
				
				if(v0.x < 50) {
					
					Vector3 temp = new Vector3(Vector3.Z);
					temp.mul(0.01f * Constants.MOVESPEED);
					temp.mul(edgeScrollingSpeed);
					Quaternion rotation = new Quaternion();
					drawController.camera.combined.getRotation(rotation);
					rotation.transform(temp);
					drawController.camera.translate(temp.mul(-1));
					drawController.camera.update();
					
					if(edgeScrollingSpeed < 25)
						edgeScrollingSpeed += Gdx.graphics.getDeltaTime() * Constants.EDGE_SCROLL_SPEED;
					
					for(ArrayList<Vector2> doodle : doodles.values()) {
						
						
						ArrayList<Vector2> newDoodle = new ArrayList<Vector2>();
						for(Vector2 v : doodle) {
							
							v.add(new Vector2(edgeScrollingSpeed,0));
									
							newDoodle.add(v);
							
						}
						
						doodle.clear();
						doodle.addAll(newDoodle);
						
					}
					
					ArrayList<Vector2> newCurrentDoodle = new ArrayList<Vector2>();
					for(int i = 0; i<currentDoodle.size() -1; i++) {
						
						Vector2 v = currentDoodle.get(i);
						
						v.add(new Vector2(edgeScrollingSpeed,0));
						
						newCurrentDoodle.add(v);
						
					}
					
					newCurrentDoodle.add(currentDoodle.get(currentDoodle.size()-1));
					currentDoodle.clear();
					currentDoodle.addAll(newCurrentDoodle);
					
				}
				
				if(v0.y > Gdx.graphics.getHeight() - 50) {
					
					Vector3 temp = new Vector3(Vector3.X);
					temp.mul(0.01f * Constants.MOVESPEED);
					temp.mul(edgeScrollingSpeed);
					Quaternion rotation = new Quaternion();
					drawController.camera.combined.getRotation(rotation);
					rotation.transform(temp);
					drawController.camera.translate(temp);
					drawController.camera.update();
					
					if(edgeScrollingSpeed < 25)
						edgeScrollingSpeed += Gdx.graphics.getDeltaTime() * Constants.EDGE_SCROLL_SPEED;
					
					for(ArrayList<Vector2> doodle : doodles.values()) {
						
						
						ArrayList<Vector2> newDoodle = new ArrayList<Vector2>();
						for(Vector2 v : doodle) {
							
							v.add(new Vector2(0,-1 * edgeScrollingSpeed));
									
							newDoodle.add(v);
							
						}
						
						doodle.clear();
						doodle.addAll(newDoodle);
						
					}
					
					ArrayList<Vector2> newCurrentDoodle = new ArrayList<Vector2>();
					for(int i = 0; i<currentDoodle.size() -1; i++) {
						
						Vector2 v = currentDoodle.get(i);
						
						v.add(new Vector2(0,-1 * edgeScrollingSpeed));
						
						newCurrentDoodle.add(v);
						
					}
					
					newCurrentDoodle.add(currentDoodle.get(currentDoodle.size()-1));
					currentDoodle.clear();
					currentDoodle.addAll(newCurrentDoodle);
					
				}
				
				if(v0.y < 50) {
					
					Vector3 temp = new Vector3(Vector3.X);
					temp.mul(0.01f * Constants.MOVESPEED);
					temp.mul(edgeScrollingSpeed);
					Quaternion rotation = new Quaternion();
					drawController.camera.combined.getRotation(rotation);
					rotation.transform(temp);
					drawController.camera.translate(temp.mul(-1));
					drawController.camera.update();
					
					if(edgeScrollingSpeed < 25)
						edgeScrollingSpeed += Gdx.graphics.getDeltaTime() * Constants.EDGE_SCROLL_SPEED;
					
					for(ArrayList<Vector2> doodle : doodles.values()) {
						
						
						ArrayList<Vector2> newDoodle = new ArrayList<Vector2>();
						for(Vector2 v : doodle) {
							
							v.add(new Vector2(0,edgeScrollingSpeed));
									
							newDoodle.add(v);
							
						}
						
						doodle.clear();
						doodle.addAll(newDoodle);
						
					}
					
					ArrayList<Vector2> newCurrentDoodle = new ArrayList<Vector2>();
					for(int i = 0; i<currentDoodle.size() -1; i++) {
						
						Vector2 v = currentDoodle.get(i);
						
						v.add(new Vector2(0,edgeScrollingSpeed));
						
						newCurrentDoodle.add(v);
						
					}
					
					newCurrentDoodle.add(currentDoodle.get(currentDoodle.size()-1));
					currentDoodle.clear();
					currentDoodle.addAll(newCurrentDoodle);
					
				}
				
				if(v0.x >= 50 && v0.x <= Gdx.graphics.getWidth() - 50 && v0.y >= 50 && v0.y <= Gdx.graphics.getHeight() - 50)					
					edgeScrollingSpeed = 1.0f;
				
			}			

		}
	}
	
	private void updatePolygons() {
		
		HashMap<Polygon, ArrayList<PlayerSoldier>> updatedList = new HashMap<Polygon, ArrayList<PlayerSoldier>>();
		ArrayList<Polygon> updatedCircleHasPath = new ArrayList<Polygon>();
		HashMap<Polygon, ArrayList<Vector3>> updatedPaths = new HashMap<Polygon, ArrayList<Vector3>>();

		for(Polygon pol : circles.keySet()) {
			
			Array<Vector2> positions = new Array<Vector2>();
			
			for(PlayerSoldier soldier : circles.get(pol)) {
				positions.add(soldier.position);
			}
			
			Array<Vector2> bound = BoundingPolygon.createGiftWrapConvexHull(positions);
			
			float[] newVertices = new float[bound.size * 2];
			
			int j=0;
			for(int i=0;i<newVertices.length;i+=2) {
				
				newVertices[i] = bound.get(j).x;
				newVertices[i+1] = bound.get(j).y;
				
				j++;
			}
			
			Polygon newPoly = new Polygon(newVertices);
			updatedList.put(newPoly, circles.get(pol));
			
			if(paths.containsKey(pol)) {
				updatedPaths.put(newPoly, paths.get(pol));
				updatedCircleHasPath.add(newPoly);
				paths.remove(pol);
			}
			
		}
		
		circleHasPath.clear();
		circleHasPath.addAll(updatedCircleHasPath);
		
		paths.putAll(updatedPaths);
		
		circles.clear();
		circles.putAll(updatedList);
		
	}

	private void updateUnits() {
		
		for(Soldier soldier:GameSession.getInstance().soldiers) {
			soldier.update(delta);
		}
		
		for(Polygon pol : circles.keySet()) {
			
			ArrayList<PlayerSoldier> soldiers = circles.get(pol);
			
			for(PlayerSoldier playerSoldier : soldiers) {
				
				if(playerSoldier.wayPoints != null && playerSoldier.wayPoints.size() > 0) {
//					Gdx.app.log("", playerSoldier.wayPoints.get(playerSoldier.wayPoints.size() -1).toString());
					playerSoldier.goTowards(new Vector2(playerSoldier.wayPoints.get(0).x, playerSoldier.wayPoints.get(0).z), false);
					if(playerSoldier.position.dst(new Vector2(playerSoldier.wayPoints.get(0).x, playerSoldier.wayPoints.get(0).z))<0.5f) {
						playerSoldier.wayPoints.remove(0);
					}
				}
				
			}
			
		}
	
	}

	private void updateAI() {		
	}

	private void collisionTest() {
		
		for(int soldier1ID = 0; soldier1ID < GameSession.getInstance().soldiers.size; soldier1ID++) {
			Soldier soldier1 = GameSession.getInstance().soldiers.get(soldier1ID);
			for(int soldier2ID = 0; soldier2ID < GameSession.getInstance().soldiers.size; soldier2ID++) {
				Soldier soldier2 = GameSession.getInstance().soldiers.get(soldier2ID);	
				if(!soldier1.equals(soldier2) && soldier1.position.dst(soldier2.position) < .2f) {
					soldier1.position.add(soldier1.position.cpy().sub(soldier2.position).mul(0.1f));
					soldier2.position.add(soldier2.position.cpy().sub(soldier1.position).mul(0.1f));
				}
			}			
		}
		
	}

	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
	}

}
