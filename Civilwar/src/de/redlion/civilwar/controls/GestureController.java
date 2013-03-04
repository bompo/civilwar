package de.redlion.civilwar.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;

import de.redlion.civilwar.SinglePlayerGameScreen;

public class GestureController extends GestureAdapter {

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {

//		DrawController.fling = true;
		Gdx.app.log("fling", "fling velocityX: " + velocityX + " veclocityY: " + velocityY);
		
		return false;
	}

}
