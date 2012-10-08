package de.redlion.rts.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

import de.redlion.rts.DrawController;
import de.redlion.rts.GameSession;
import de.redlion.rts.KeyController;
import de.redlion.rts.OrthoCamController;
import de.redlion.rts.PerspectiveCamController;
import de.redlion.rts.SinglePlayerGameScreen;
import de.redlion.rts.collision.HeightMap;
import de.redlion.rts.shader.Bloom;
import de.redlion.rts.units.Soldier;

public class RenderMap {

	StillModel modelLandscapeObj;
	
	StillModel modelSoldierObj;
	Texture texSoldierDiff;
	Texture texEnemySoldierDiff;	
	
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

	ShaderProgram flatShader;
	ShaderProgram shadowGenShader;
	ShaderProgram shadowMapShader;
	ShaderProgram currShader;
	FrameBuffer shadowMap;
	

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
		
		modelSoldierObj = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/soldier.g3dt"));
		texSoldierDiff = new Texture(Gdx.files.internal("data/soldier_diff.png"), true);
		
		texEnemySoldierDiff = new Texture(Gdx.files.internal("data/enemy_soldier_diff.png"), true);
		
		imageLightning = new Texture(Gdx.files.internal("data/beach_probe_diffuse.png"), true);
		imageLightning.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
		imageLightning.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		cam.zoom = 0.012f;
		cam.position.set(-10, 10, 10);
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
		
		
		for(Soldier soldier:GameSession.getInstance().playerSoldiers) {
			tmp.idt();
			model.idt();
			tmp.setToRotation(Vector3.Z, soldier.angle);
			model.mul(tmp);
			
			tmp.setToTranslation(-0.7f,0.f,0);
			model.mul(tmp);
			tmp.setToTranslation(soldier.position);
			model.mul(tmp);

			shadowGenShader.setUniformMatrix("u_model", model);
			
			shadowMapShader.setUniformf("u_waterOn", 0);
			shadowMapShader.setUniformf("u_color", 1.0f, 1.0f, 1.0f);
			modelSoldierObj.render(shadowGenShader);
		}
		
		
		for(Soldier soldier:GameSession.getInstance().enemySoldiers) {
			tmp.idt();
			model.idt();
			tmp.setToRotation(Vector3.Z, soldier.angle);
			model.mul(tmp);
			
			tmp.setToTranslation(-0.7f,0.f,0);
			model.mul(tmp);
			tmp.setToTranslation(soldier.position);
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
		shadowMapShader.setUniformf("u_color", 1.0f, 0.93f, 0.9f);
		
		modelLandscapeObj.render(shadowMapShader);
		shadowMapShader.setUniformf("u_color", 0.96f, 0.75f, 0.47f);
		
		//render soldier
		texSoldierDiff.bind(1);
		for(Soldier soldier:GameSession.getInstance().playerSoldiers) {
			tmp.idt();
			model.idt();			
			
			tmp.setToTranslation(-0.7f,0.f,0);
			model.mul(tmp);
			tmp.setToTranslation(soldier.position);
			model.mul(tmp);
			
			tmp.setToRotation(Vector3.Z, soldier.angle);
			model.mul(tmp);

			shadowMapShader.setUniformMatrix("u_model", model);
			
			shadowMapShader.setUniformf("u_waterOn", 0);
			shadowMapShader.setUniformf("u_color", 1.0f, 1.0f, 1.0f);
			modelSoldierObj.render(shadowMapShader);
		}
		
		texEnemySoldierDiff.bind(1);
		for(Soldier soldier:GameSession.getInstance().enemySoldiers) {
			tmp.idt();
			model.idt();			
			
			tmp.setToTranslation(-0.7f,0.f,0);
			model.mul(tmp);
			tmp.setToTranslation(soldier.position);
			model.mul(tmp);
			
			tmp.setToRotation(Vector3.Z, soldier.angle);
			model.mul(tmp);

			shadowMapShader.setUniformMatrix("u_model", model);
			
			shadowMapShader.setUniformf("u_waterOn", 0);
			shadowMapShader.setUniformf("u_color", 1.0f, 1.0f, 1.0f);
			modelSoldierObj.render(shadowMapShader);
		}
		
		shadowMapShader.end();
		
		
		bloom.render();

	}

}
