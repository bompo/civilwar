/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package de.redlion.civilwar.controls;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import de.redlion.civilwar.Constants;
import de.redlion.civilwar.SinglePlayerGameScreen;

public class OrthoCamController extends InputAdapter {
	final OrthographicCamera camera;
	final Vector3 curr = new Vector3();
	final Vector2 last = new Vector2(0, 0);
	final Vector2 delta = new Vector2();
	
	private boolean oneFingerDown = false;
	private boolean twoFingerDown = false;
	private boolean threeFingerDown = false;
	private boolean fourFingerDown = false;
	private boolean fiveFingerDown = false;
	private int howmanyfingers = 0;

	public OrthoCamController (OrthographicCamera camera) {
		this.camera = camera;
	}

	@Override
	public boolean touchDragged (int x, int y, int pointer) {
		
		if(howmanyfingers == 1 && pointer == 0) {
			delta.set(x, y).sub(last);
			delta.mul(0.01f * Constants.MOVESPEED);
			Vector3 temp = new Vector3(delta.y, 0, -delta.x);
			Quaternion rotation = new Quaternion();
			camera.combined.getRotation(rotation);
			rotation.transform(temp);
			camera.translate(temp);
			camera.update();
			last.set(x, y);
		}
		
		if(pointer == 0)
			last.set(x,y);
		
		return true;
	}

	@Override
	public boolean touchUp (int x, int y, int pointer, int button) {
		
		switch(pointer) {
		case 0: oneFingerDown = false;
				howmanyfingers--;
				break;
		case 1: twoFingerDown = false;
				howmanyfingers--;
				break;
		case 2: threeFingerDown = false;
				howmanyfingers--;
				break;
		case 3: fourFingerDown = false;
				howmanyfingers--;
				break;
		case 4: fiveFingerDown = false;
				howmanyfingers--;
				break;
		default: break;
		}
		
		if(howmanyfingers == 0)
			last.set(0, 0);
		
		return true;
	}
	
	@Override
	public boolean touchDown (int x, int y, int pointer, int button) {
		
		switch(pointer) {
		case 0: oneFingerDown = true;
				howmanyfingers = 1;
				break;
		case 1: twoFingerDown = true;
				howmanyfingers = 2;
				break;
		case 2: threeFingerDown = true;
				howmanyfingers = 3;
				break;
		case 3: fourFingerDown = true;
				howmanyfingers = 4;
				break;
		case 4: fiveFingerDown = true;
				howmanyfingers = 5;
				break;
		default: break;
		}
		
		if(howmanyfingers == 1)
			last.set(x, y);
		
		if(howmanyfingers == 5)
			SinglePlayerGameScreen.paused = !SinglePlayerGameScreen.paused;
		
		return true;
	}
	
	@Override
	public boolean scrolled (int amount) {
		camera.zoom -= -amount * Gdx.graphics.getDeltaTime() / 50;
		camera.update();
		return true;
	}
}
