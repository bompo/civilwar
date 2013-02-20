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
import com.badlogic.gdx.graphics.g3d.StillModelInstance;
import com.badlogic.gdx.graphics.g3d.StillModelNode;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

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
	
	ShapeRenderer r;
	StillModel sphere;
	
	public static boolean paused = false;

	public static HashMap<Polygon, ArrayList<Vector3>> paths;  //maps projected polygons to their associated paths
	public static HashMap<Polygon,ArrayList<PlayerSoldier>> circles; //maps projected polygons to what soldiers they encompass
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
		
		Material mat = new Material("sphere", new TextureAttribute(new Texture(Gdx.files.internal("data/white.png"), true), 0, TextureAttribute.diffuseTexture));
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

		if(!paused)
			updateUnits();

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
		for(ArrayList<Vector3> pathPoints : paths.values()) {
			
			if(!pathPoints.isEmpty()) {
				
				for(Vector3 pPoint : pathPoints) {

					StillModelNode node = new StillModelNode();
					node.matrix.translate(pPoint);
					node.matrix.scl(0.05f);
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
			}			

//			if(circleRay != null) {	
//				
//				//TODO fix me for Vector2 usage instead of Vector3
////				for(Soldier s : GameSession.getInstance().soldiers) {
////					if(s instanceof PlayerSoldier) {
////						for(Polygon pathPolygon : circles) {
////							if(pathPolygon.contains(s.position.x, s.position.y))
////								Gdx.app.log("", s.id + "");
////						}
////					}
////					if(s.position.dst(localIntersection) < circleRadius) {
////						Gdx.app.log("", "" + s.id);
////					}
////				}
//			}
		}
	}
	
	private void updateUnits() {
		
		for(Soldier soldier:GameSession.getInstance().soldiers) {
			soldier.update(delta);
		}
		
		for(Polygon pol : circles.keySet()) {
			
			ArrayList<PlayerSoldier> soldiers = circles.get(pol);
			
//			for(PlayerSoldier playerSoldier : soldiers) {
//				
//				ArrayList<Vector3> wayPoints = paths.get(pol);
//				
//				if(wayPoints != null) {
//					playerSoldier.goTowards(new Vector2(wayPoints.get(wayPoints.size() -1).x, wayPoints.get(wayPoints.size() -1).z), false);
//				}
//				
//			}
			
		}
	
	}

	private void updateAI() {		
	}

	private void collisionTest() {
		
		
	}

	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
	}

}
