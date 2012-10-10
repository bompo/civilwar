package de.redlion.rts;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class DrawController extends InputAdapter{

	final OrthographicCamera camera;
	final Vector3 curr = new Vector3();
	final Vector2 last = new Vector2(0, 0);
	final Vector2 delta = new Vector2();
	
	ShapeRenderer r = new ShapeRenderer();

	public DrawController (OrthographicCamera camera) {
		this.camera = camera;
	}

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		y = -y + Gdx.graphics.getHeight();
		
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
			Vector2 temp = new Vector2(x, y);
			SinglePlayerGameScreen.path.appendPoint(temp);
		}
		return true;
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		last.set(0, 0);
		
		
		return true;
	}
	
	@Override
	public boolean touchDown (int x, int y, int pointer, int button) {
		last.set(x, y);
		
		return true;
	}
	
	@Override
	public boolean scrolled (int amount) {
		camera.zoom -= -amount * Gdx.graphics.getDeltaTime() / 50;
		camera.update();
		return true;
	}
}
