package de.redlion.rts.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import de.redlion.rts.Resources;

public class RenderDebug {

	SpriteBatch batch;
	BitmapFont font;

	public RenderDebug() {
		batch = new SpriteBatch();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
		font = Resources.getInstance().font;
		font.setScale(1);
	}

	public void render() {
		batch.begin();
		font.draw(batch, Gdx.graphics.getFramesPerSecond() + " fps", 20, 30);
		batch.end();

	}

}
