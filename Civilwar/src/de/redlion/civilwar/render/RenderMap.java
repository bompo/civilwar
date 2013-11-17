package de.redlion.civilwar.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

import de.redlion.civilwar.Configuration;
import de.redlion.civilwar.GameSession;
import de.redlion.civilwar.collision.HeightMap;
import de.redlion.civilwar.shader.Bloom;
import de.redlion.civilwar.units.PlayerSoldier;
import de.redlion.civilwar.units.Soldier;

public class RenderMap {

	ModelBatch modelBatch;
	AssetManager assets;
	float counter;

	ModelInstance modelLandscapeObj;
	ModelInstance modelSoldierObj;
	ModelInstance modelShadowPlaneObj;
	ModelInstance modelEnemySoldierObj;
	ModelInstance modelSelectedSoldierObj;
	ModelInstance modelWeaponObj;

	int soldierSelector = 0;

	float time;

	BoundingBox instanceLandBB;

	public OrthographicCamera cam;
	public OrthographicCamera birdsEye;
	PerspectiveCamera currCam;

	Environment lights;
	
	public HeightMap heightMap;

	Matrix4 tmp = new Matrix4().idt();

	Preferences prefs;

	boolean isAssetsLoaded = false;

	Bloom bloom = new Bloom();

	public RenderMap() {
		setupScene(null, null, null);
	}

	public RenderMap(Vector3 camPos, Vector3 camDir, Vector3 camUp) {
		setupScene(camPos, camDir, camUp);
	}

	private void setupScene(Vector3 camPos, Vector3 camDir, Vector3 camUp) {
		prefs = Gdx.app.getPreferences(Configuration.getInstance().TAG);
		
		lights = new Environment();
        lights.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.0f, 0.0f, 0.0f, 1.f));
        lights.add(new DirectionalLight().set(0.99f, 0.99f, 0.99f, -0.4f, -1.0f, 0.2f));

		assets = new AssetManager();
		assets.load("data/soldier.g3db", Model.class);
		assets.load("data/soldier_enemy.g3db", Model.class);
		assets.load("data/soldier_selected.g3db", Model.class);
		assets.load("data/landscape.g3db", Model.class);
		assets.load("data/shadow_plane.g3db", Model.class);		

		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.zoom = 0.015f;
		if (camPos == null) {
			cam.position.set(-17, 18, 6);
		} else {
			cam.position.set(camPos);
		}
		if (camUp == null) {
			cam.lookAt(0, 0, 0);
		} else {
			cam.direction.set(camDir);
			cam.up.set(camUp);
		}
		cam.update();

		birdsEye = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		birdsEye.zoom = 0.015f;
		birdsEye.position.set(-1, 18, 0);
		birdsEye.lookAt(0, 0, 0);
		birdsEye.update();

		// cam = birdsEye;

		modelBatch = new ModelBatch();

		bloom.setBloomIntesity(1.0f);
		bloom.setClearColor(0.6f, 0.7f, 1, 1);

		assets.finishLoading();
		doneLoading();
	}

	private void doneLoading() {
		modelLandscapeObj = new ModelInstance(assets.get("data/landscape.g3db", Model.class));
		modelSoldierObj = new ModelInstance(assets.get("data/soldier.g3db", Model.class));
		modelShadowPlaneObj = new ModelInstance(assets.get("data/shadow_plane.g3db", Model.class));
		BlendingAttribute blendingAttribute1 = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 1.0f);
		modelShadowPlaneObj.materials.get(0).set(blendingAttribute1);
	    
		modelEnemySoldierObj = new ModelInstance(assets.get("data/soldier_enemy.g3db", Model.class));
		modelSelectedSoldierObj = new ModelInstance(assets.get("data/soldier_selected.g3db", Model.class));

		heightMap = new HeightMap(modelLandscapeObj.model);

		isAssetsLoaded = true;
	}

	public void updateCamera(PerspectiveCamera cam) {
		// protoRenderer.cam = cam;
	}

	public void render() {
		Gdx.gl.glClearColor(0.6f, 0.7f, 1, 1);

		time += Gdx.graphics.getDeltaTime();

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDisable(GL20.GL_BLEND);
		Gdx.gl.glDepthMask(true);		
		
		bloom.capture();
		
		modelBatch.begin(cam);
		modelBatch.render(modelLandscapeObj, lights);

		/*
		 * //update height per frame Soldier tempSoldier =
		 * GameSession.getInstance().soldiers.get(soldierSelector); Ray ray =
		 * new Ray(new Vector3(tempSoldier.position.x + (tempSoldier.velocity.x
		 * *
		 * Gdx.graphics.getDeltaTime()*GameSession.getInstance().soldiers.size),
		 * -100, tempSoldier.position.y + (tempSoldier.velocity.y*
		 * Gdx.graphics.getDeltaTime
		 * ()*GameSession.getInstance().soldiers.size)), Vector3.Y); Vector3
		 * localIntersection = new Vector3();
		 * Intersector.intersectRayTriangles(ray, heightMap.map,
		 * localIntersection); tempSoldier.height = tempSoldier.heightTarget;
		 * tempSoldier.heightTarget = localIntersection.y;
		 * tempSoldier.heightInterpolator = 0; soldierSelector =
		 * (soldierSelector + 1) % GameSession.getInstance().soldiers.size;
		 */

		// render soldier
		for (int i = 0; i < GameSession.getInstance().soldiers.size; i++) {
			Soldier soldier = GameSession.getInstance().soldiers.get(i);

//			soldier.heightInterpolator = soldier.heightInterpolator + (1.f / (float) GameSession.getInstance().soldiers.size);
//			float height = Interpolation.linear.apply(soldier.height, soldier.heightTarget, soldier.heightInterpolator);

			float height = heightMap.getHeight(soldier.position.x, soldier.position.y);

			tmp.idt();
			tmp.trn(soldier.position.x, height + 0.05f, soldier.position.y);
			tmp.scl(0.15f);
			modelShadowPlaneObj.transform.set(tmp);
			modelBatch.render(modelShadowPlaneObj);

			tmp.idt();
			tmp.trn(soldier.position.x, height + 0.05f, soldier.position.y);
			tmp.scl(1f);
			tmp.trn(0, (soldier.bounce / 10.f) - 0.05f, 0);
			tmp.rotate(Vector3.Y, -soldier.facing.angle());
			tmp.rotate(Vector3.X, -soldier.bounce * 10.f);
			tmp.rotate(Vector3.X, -soldier.angle * 1.f);
			modelSoldierObj.transform.set(tmp);
			modelSelectedSoldierObj.transform.set(tmp);
			modelEnemySoldierObj.transform.set(tmp);

			if ((soldier instanceof PlayerSoldier)) {
				if (!((PlayerSoldier) soldier).circled) {
					modelBatch.render(modelSoldierObj);
				} else {
					modelBatch.render(modelSelectedSoldierObj);
				}
			} else {
				modelBatch.render(modelEnemySoldierObj);
			}
		}

		modelBatch.end();

		bloom.render();

	}

}
