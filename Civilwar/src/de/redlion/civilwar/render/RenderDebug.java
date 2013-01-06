package de.redlion.civilwar.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Polygon;

import de.redlion.civilwar.Resources;
import de.redlion.civilwar.SinglePlayerGameScreen;

public class RenderDebug {

	SpriteBatch batch;
	BitmapFont font;

	ShapeRenderer shapeRenderer;

	public RenderDebug() {
		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		font = Resources.getInstance().font;
		font.setScale(1);

		shapeRenderer = new ShapeRenderer();	}

	public void render(OrthographicCamera cam) {
		batch.begin();
		font.draw(batch, Gdx.graphics.getFramesPerSecond() + " fps", 20, 30);
		batch.end();

		shapeRenderer.setProjectionMatrix(cam.combined);
		shapeRenderer.setColor(1, 0, 0, 1);
		shapeRenderer.begin(ShapeType.Line);
		for (Polygon p : SinglePlayerGameScreen.circles.keySet()) {

			float[] temp = p.getTransformedVertices();
			float x1 = temp[0];
			float y1 = temp[1];
			for (int j = 0; j < temp.length; j += 2) {
				shapeRenderer.line(x1, y1, temp[j], temp[j+1]);
				x1 = temp[j];
				y1 = temp[j+1];
			}
			
		}	
		
		shapeRenderer.end();

		/*
		diffuseShader.begin();
		for (Polygon p : SinglePlayerGameScreen.circles.keySet()) {

			float[] temp = p.getWorldVertices();
			float[] vertices = new float[(int) Math.ceil((temp.length / 2) * 3)];
			short[] indices = new short[vertices.length];

			for (short i = 0; i < vertices.length; i++) {
				indices[i] = i;

			}
			ArrayList<Vector3> points = new ArrayList<Vector3>();
			for (int j = 0; j < temp.length; j += 2) {
				Vector3 v = new Vector3();
				v.x = temp[j];
				v.y = 0;
				v.z = temp[j + 1];
				points.add(v);
			}
			int k = 0;

			// Gdx.app.log("", "" + temp.length + " " + vertices.length + " " +
			// indices.length + " " + points.size());
			for (Vector3 vec : points) {
				vertices[k] = vec.x;
				vertices[k + 1] = vec.y;
				vertices[k + 2] = vec.z;

				k += 3;
			}

			Mesh polygon = new Mesh(true, vertices.length, vertices.length,
					new VertexAttribute(Usage.Position, 3, "a_position"));
			polygon.setVertices(vertices);
			polygon.setIndices(indices);

			diffuseShader.setUniformf("a_color", 0.5f, 0.1f, 0.0f, 1.0f);
			polygon.render(diffuseShader, Gdx.gl20.GL_LINE_STRIP);

		}
		diffuseShader.end();
		*/

	}

}
