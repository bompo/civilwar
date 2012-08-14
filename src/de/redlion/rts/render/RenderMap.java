package de.redlion.rts.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.GdxRuntimeException;

import de.redlion.rts.OrthoCamController;
import de.redlion.rts.shader.Bloom;

public class RenderMap {

	StillModel modelWaterObj;
	StillModel modelRocksObj;	
	StillModel modelBigRockObj;
	StillModel modelHouseObj;
	
	Texture texAOMap;	
	Texture imageLightning;	
	
	float time;

	Bloom bloom = new Bloom();

	Mesh plane;
	Mesh cube;
	Texture whiteTex;

	// GLES20
	Matrix4 model = new Matrix4().idt();
	Matrix4 normal = new Matrix4().idt();
	Matrix4 tmp = new Matrix4().idt();

	OrthographicCamera cam;
	PerspectiveCamera lightCam;
	PerspectiveCamera currCam;

	OrthoCamController camController;

	ShaderProgram flatShader;
	ShaderProgram shadowGenShader;
	ShaderProgram shadowMapShader;
	ShaderProgram currShader;
	FrameBuffer shadowMap;
	InputMultiplexer multiplexer;

	public RenderMap() {
		setupScene();
		setupShadowMap();

		camController = new OrthoCamController(cam);
		multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(camController);

		Gdx.input.setInputProcessor(multiplexer);
		bloom = new Bloom();
		bloom.setBloomIntesity(1.5f);
		bloom.setTreshold(0.65f);
		bloom.setOriginalIntesity(1.0f);
	}

	private void setupScene() {
		plane = new Mesh(true, 4, 4, new VertexAttribute(Usage.Position, 3,
				ShaderProgram.POSITION_ATTRIBUTE));
		plane.setVertices(new float[] { -10, -1, 10, 10, -1, 10, 10, -1, -10,
				-10, -1, -10 });
		plane.setIndices(new short[] { 3, 2, 1, 0 });
		modelHouseObj = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/building.g3dt"));
		texAOMap = new Texture(Gdx.files.internal("data/ao_map.png"), true);
		texAOMap.setFilter(TextureFilter.Linear, TextureFilter.Linear);
		
		modelRocksObj = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/rocks.g3dt"));
			
		modelBigRockObj = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/bigrock.g3dt"));
	
		modelWaterObj = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/water.g3dt"));
		
		
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
		
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

		shadowMap.begin();
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
		Gdx.gl.glCullFace(GL20.GL_BACK);
		shadowGenShader.begin();
		shadowGenShader.setUniformMatrix("u_projTrans", lightCam.combined);
		
		modelWaterObj.render(shadowGenShader);
		modelHouseObj.render(shadowGenShader);
		modelRocksObj.render(shadowGenShader);
		modelBigRockObj.render(shadowGenShader);
		
		shadowGenShader.end();
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
		
		shadowMapShader.setUniformf("u_viewerPosition", cam.position);
		shadowMapShader.setUniformMatrix("u_projTrans", cam.combined);
		shadowMapShader.setUniformMatrix("u_lightProjTrans", lightCam.combined);
		
		texAOMap.bind(1);		

		shadowMapShader.setUniformf("u_waterOn", 1);
		shadowMapShader.setUniformf("u_color", 0.86f, 1.0f, 0.98f);
		modelWaterObj.render(shadowMapShader);
		
		shadowMapShader.setUniformf("u_waterOn", 0);
		shadowMapShader.setUniformf("u_color", 1.0f, 0.93f, 0.9f);
		modelHouseObj.render(shadowMapShader);
		shadowMapShader.setUniformf("u_color", 0.96f, 0.75f, 0.47f);
		modelRocksObj.render(shadowMapShader);
		shadowMapShader.setUniformf("u_color", 0.8f, 0.58f, 0.28f);
		modelBigRockObj.render(shadowMapShader);
		
		shadowMapShader.end();
		
		
		bloom.render();


	}

}
