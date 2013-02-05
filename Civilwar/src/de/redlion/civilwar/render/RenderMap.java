package de.redlion.civilwar.render;

import java.util.ArrayList;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.AnimatedModelNode;
import com.badlogic.gdx.graphics.g3d.StillModelNode;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.materials.Material;
import com.badlogic.gdx.graphics.g3d.materials.MaterialAttribute;
import com.badlogic.gdx.graphics.g3d.materials.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.keyframe.KeyframedAnimation;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.sun.xml.internal.ws.resources.ModelerMessages;

import de.redlion.civilwar.Configuration;
import de.redlion.civilwar.GameSession;
import de.redlion.civilwar.SinglePlayerGameScreen;
import de.redlion.civilwar.collision.HeightMap;
import de.redlion.civilwar.render.LightManager.LightQuality;
import de.redlion.civilwar.units.EnemySoldier;
import de.redlion.civilwar.units.PlayerSoldier;
import de.redlion.civilwar.units.Soldier;

public class RenderMap {
	
	static final int LIGHTS_NUM = 0;
	static final float LIGHT_INTESITY = 0f;
	
	boolean highQuality = true;

	LightManager lightManager;
	PrototypeRendererGL20 protoRenderer;

	StillModel modelLandscapeObj;
	
	StillModel modelSoldierObj;
	StillModel modelEnemySoldierObj;
	Texture texSoldierDiff;
	Texture texEnemySoldierDiff;	
	
	StillModel modelWeaponObj;
	Texture texWeaponDiff;
	
	Texture texAOMap;	
	Texture imageLightning;	
	
	float time;

	Texture whiteTex;
	
	StillModelNode instanceLand;	
	BoundingBox instanceLandBB;

	public OrthographicCamera cam;
	PerspectiveCamera currCam;
	
	public HeightMap heightMap;
	
	Matrix4 tmp = new Matrix4().idt();

	Preferences prefs;
	
	public RenderMap() {
		setupScene();
	}

	private void setupScene() {
		prefs = Gdx.app.getPreferences(Configuration.getInstance().TAG);
		
		highQuality = prefs.getBoolean("highQuality", false);
		if(highQuality) {
			lightManager = new LightManager(LIGHTS_NUM, LightQuality.FRAGMENT);
		} else {
			lightManager = new LightManager(LIGHTS_NUM, LightQuality.VERTEX);
		}
		
		lightManager.dirLight = new DirectionalLight();
		lightManager.dirLight.color.set(1f, 1f, 1f, 1);
		lightManager.dirLight.direction.set(-0.4f, -1.0f, 0.03f).nor();

		lightManager.ambientLight.set(0.0f, 0.0f, 0.0f, 0.0f);
		
		protoRenderer = new PrototypeRendererGL20(lightManager);

		
		texAOMap = new Texture(Gdx.files.internal("data/landscape_diff.png"), true);
		texAOMap.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		modelLandscapeObj = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/landscape.g3dt"));	
		heightMap = new HeightMap(modelLandscapeObj);
		
		modelSoldierObj = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/soldier.g3dt"));
		modelEnemySoldierObj = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/soldier.g3dt"));
		texSoldierDiff = new Texture(Gdx.files.internal("data/soldier_diff.png"), true);		
		texEnemySoldierDiff = new Texture(Gdx.files.internal("data/enemy_soldier_diff.png"), true);
		
		modelWeaponObj = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/rifle.g3dt"));
		texWeaponDiff = new Texture(Gdx.files.internal("data/rifle_diff.png"), true);
		
		imageLightning = new Texture(Gdx.files.internal("data/beach_probe_diffuse.png"), true);
		imageLightning.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		imageLightning.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.zoom = 0.008f;
		cam.position.set(-17, 18, 6);
		cam.lookAt(0, 0, 0);
		cam.update();
		
		// set materials
		MaterialAttribute materialAttributeSoldierDiffTex = new TextureAttribute(texSoldierDiff, 0, TextureAttribute.diffuseTexture);
		MaterialAttribute materialAttributeEnemySoldierDiffTex = new TextureAttribute(texEnemySoldierDiff, 0, TextureAttribute.diffuseTexture);

		MaterialAttribute materialAttributeWeaponDiffTex = new TextureAttribute(texWeaponDiff, 0, TextureAttribute.diffuseTexture);
		
		MaterialAttribute materialAttributeLandscapeDiffTex = new TextureAttribute(texAOMap, 0, TextureAttribute.diffuseTexture);
		
		Material materialSoldier = new Material("soldier", materialAttributeSoldierDiffTex);
		Material materialEnemySoldier = new Material("enemySoldier", materialAttributeEnemySoldierDiffTex);
		Material materialLandscape = new Material("landscape", materialAttributeLandscapeDiffTex);
		Material materialWeapon = new Material("weapon", materialAttributeWeaponDiffTex);
		
		modelWeaponObj.setMaterial(materialWeapon);
		modelSoldierObj.setMaterial(materialSoldier);
		modelEnemySoldierObj.setMaterial(materialEnemySoldier);
		modelLandscapeObj.setMaterial(materialLandscape);
		
		
		// create instances
		{
			instanceLandBB = new BoundingBox();		
			instanceLand = new StillModelNode();
			modelLandscapeObj.getBoundingBox(instanceLandBB);
			instanceLandBB.mul(instanceLand.matrix);
			instanceLand.radius = (instanceLandBB.getDimensions().len() / 2);
		}
	}

	public void updateCamera(PerspectiveCamera cam) {
//		protoRenderer.cam = cam;
	}

	public void render() {
		Gdx.gl.glClearColor(0.6f, 0.7f, 1, 1);
		
		time += Gdx.graphics.getDeltaTime();		
		
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		
		protoRenderer.cam = cam;
		
		
		protoRenderer.begin();
		protoRenderer.draw(modelLandscapeObj, instanceLand);
		
		//render soldier
		for(Soldier soldier:GameSession.getInstance().soldiers) {
			
			//search in all circles if current soldier is selected... TODO cache this...
			int selected = 0;
			for (Entry<Polygon, ArrayList<PlayerSoldier>> entry : SinglePlayerGameScreen.circles.entrySet()) {
			    for(Soldier soldierToSelect: entry.getValue()) {
			    	if(soldier.equals(soldierToSelect)) {
			    		selected = 1;
			    		break;
			    	}
			    	
			    }
			}
			
			Ray ray = new Ray(new Vector3(soldier.position.x, -100, soldier.position.y), Vector3.Y);
			Vector3 localIntersection = new Vector3();
			if (Intersector.intersectRayTriangles(ray, heightMap.map, localIntersection)) {
			}
			

			
			modelSoldierObj.getBoundingBox(soldier.instanceBB);
			soldier.instanceBB.mul(soldier.instance.matrix);
			soldier.instance.radius = (soldier.instanceBB.getDimensions().len() / 2);

			soldier.instance.matrix.idt();
			soldier.instance.matrix.trn(localIntersection.x, localIntersection.y, localIntersection.z);
			soldier.instance.matrix.rotate(Vector3.Y, soldier.facing.angle());
			soldier.instance.matrix.rotate(Vector3.Y, -90);
			soldier.instance.matrix.rotate(Vector3.Z, soldier.angle);
			
			
			if((soldier instanceof PlayerSoldier)) {
				protoRenderer.draw(modelSoldierObj, soldier.instance);
			}
			
			if((soldier instanceof EnemySoldier)) {
				protoRenderer.draw(modelEnemySoldierObj, soldier.instance);
			}
			
			
		}
		

		protoRenderer.end();	
		

	}

}
