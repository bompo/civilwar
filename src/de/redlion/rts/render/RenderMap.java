package de.redlion.rts.render;

import java.util.ArrayList;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.GdxRuntimeException;

import de.redlion.rts.GameSession;
import de.redlion.rts.SinglePlayerGameScreen;
import de.redlion.rts.collision.HeightMap;
import de.redlion.rts.shader.Bloom;
import de.redlion.rts.units.EnemySoldier;
import de.redlion.rts.units.PlayerSoldier;
import de.redlion.rts.units.Soldier;

public class RenderMap {

	StillModel modelLandscapeObj;
	
	StillModel modelSoldierObj;
	Texture texSoldierDiff;
	Texture texEnemySoldierDiff;	
	
	StillModel modelWeaponObj;
	Texture texWeaponDiff;
	
	
	Texture texAOMap;	
	Texture imageLightning;	
	
	float time;

	Bloom bloom = new Bloom();

	Texture whiteTex;

	// GLES20
	Matrix4 model = new Matrix4().idt();
	Matrix4 normal = new Matrix4().idt();
	Matrix4 tmp = new Matrix4().idt();

	public OrthographicCamera cam;
	PerspectiveCamera lightCam;
	PerspectiveCamera currCam;

	ShaderProgram shadowGenShader;
	ShaderProgram shadowMapShader;
	ShaderProgram currShader;
	FrameBuffer shadowMap;
	public HeightMap heightMap;
	
	public RenderMap() {
		setupScene();
		setupShadowMap();
		
		bloom = new Bloom();
		bloom.setBloomIntesity(1.5f);
		bloom.setTreshold(0.65f);
		bloom.setOriginalIntesity(1.0f);
	}

	private void setupScene() {
		texAOMap = new Texture(Gdx.files.internal("data/landscape_diff.png"), true);
		texAOMap.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		modelLandscapeObj = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/landscape.g3dt"));	
		heightMap = new HeightMap(modelLandscapeObj);
		
		modelSoldierObj = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/soldier.g3dt"));
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
	}

	private void setupShadowMap() {
		shadowMap = new FrameBuffer(Format.RGBA4444, 512, 512, true);
		lightCam = new PerspectiveCamera(40, shadowMap.getWidth(),	shadowMap.getHeight());
		lightCam.position.set(-1, 20, 0);
		lightCam.lookAt(0, 0, 0);
		
		lightCam.update();

		shadowGenShader = new ShaderProgram(Gdx.files.internal(
				"data/shaders/shadowgen-vert.glsl").readString(), Gdx.files
				.internal("data/shaders/shadowgen-frag.glsl").readString());
		if (!shadowGenShader.isCompiled())
			throw new GdxRuntimeException(
					"Couldn't compile shadow gen shader: "
							+ shadowGenShader.getLog());

		shadowMapShader = new ShaderProgram(Gdx.files.internal(
				"data/shaders/shadowmap-vert.glsl").readString(), Gdx.files
				.internal("data/shaders/shadowmap-frag.glsl").readString());
		if (!shadowMapShader.isCompiled())
			throw new GdxRuntimeException(
					"Couldn't compile shadow map shader: "
							+ shadowMapShader.getLog());
	}

	public void updateCamera(PerspectiveCamera cam) {
		// this.cam = cam;
	}

	public void render() {
		
		time += Gdx.graphics.getDeltaTime();		
		
		lightCam.position.x = cam.position.x + 9;
		lightCam.position.y = cam.position.y + 10;
		lightCam.position.z = cam.position.z - 10;
		lightCam.update();
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

		shadowMap.begin();
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		shadowGenShader.begin();
		shadowGenShader.setUniformMatrix("u_projTrans", lightCam.combined);
		model.idt();
		shadowGenShader.setUniformMatrix("u_model", model);
		
		modelLandscapeObj.render(shadowGenShader);
		
		
		for(Soldier soldier:GameSession.getInstance().soldiers) {			
			tmp.idt();
			model.idt();
			
			Ray ray = new Ray(new Vector3(soldier.position.x, -100, soldier.position.y), Vector3.Y);
			Vector3 localIntersection = new Vector3();
			if (Intersector.intersectRayTriangles(ray, heightMap.map, localIntersection)) {
			}
			
			tmp.setToTranslation(localIntersection);
			model.mul(tmp);
			
			tmp.setToRotation(Vector3.Z, soldier.angle);
			model.mul(tmp);

			shadowGenShader.setUniformMatrix("u_model", model);
			
			shadowMapShader.setUniformf("u_waterOn", 0);
			shadowMapShader.setUniformf("u_color", 1.0f, 1.0f, 1.0f);
			modelSoldierObj.render(shadowGenShader);
		}
		
		shadowMap.end();


		//Gdx.gl.glDisable(GL20.GL_CULL_FACE);
		
		bloom.capture();
		
		shadowMapShader.begin();
		shadowMap.getColorBufferTexture().bind(0);
		imageLightning.bind(2);
		
		shadowMapShader.setUniformi("s_shadowMap", 0);
		shadowMapShader.setUniformi("s_diffMap", 1);
		shadowMapShader.setUniformi("s_IrradianceMap", 2);
		shadowMapShader.setUniformf("u_time", time);
		model.idt();
		shadowMapShader.setUniformMatrix("u_model", model);
		
		shadowMapShader.setUniformf("u_viewerPosition", cam.position);
		shadowMapShader.setUniformMatrix("u_projTrans", cam.combined);
		shadowMapShader.setUniformMatrix("u_lightProjTrans", lightCam.combined);
		
		texAOMap.bind(1);	

		shadowMapShader.setUniformf("u_waterOn", 0);
		shadowMapShader.setUniformf("u_color", 1.0f, 1f, 1f);
		
		modelLandscapeObj.render(shadowMapShader);
		shadowMapShader.setUniformf("u_color", 0.96f, 0.75f, 0.47f);
		
		//render soldier
		for(Soldier soldier:GameSession.getInstance().soldiers) {
			if((soldier instanceof PlayerSoldier)) {
				texSoldierDiff.bind(1);	
			}
			
			if((soldier instanceof EnemySoldier)) {
				texEnemySoldierDiff.bind(1);
			}
			
			
			shadowMapShader.setUniformf("u_time", time);
			
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
			shadowMapShader.setUniformf("u_selected", selected);
			
			
			tmp.idt();
			model.idt();			
			
			Ray ray = new Ray(new Vector3(soldier.position.x, -100, soldier.position.y), Vector3.Y);
			Vector3 localIntersection = new Vector3();
			if (Intersector.intersectRayTriangles(ray, heightMap.map, localIntersection)) {
			}
			
			tmp.setToTranslation(localIntersection);
			model.mul(tmp);
			
			tmp.setToRotation(Vector3.Y, soldier.facing.angle());
			model.mul(tmp);
			
			tmp.setToRotation(Vector3.Y, -90);
			model.mul(tmp);
			
			tmp.setToRotation(Vector3.Z, soldier.angle);
			model.mul(tmp);

			shadowMapShader.setUniformMatrix("u_model", model);
			
			shadowMapShader.setUniformf("u_waterOn", 0);
			shadowMapShader.setUniformf("u_color", 1.0f, 1.0f, 1.0f);
			modelSoldierObj.render(shadowMapShader);

			texWeaponDiff.bind(1);
			modelWeaponObj.render(shadowMapShader);			
		}
		
		shadowMapShader.end();
		
		
		bloom.render();

	}

}
