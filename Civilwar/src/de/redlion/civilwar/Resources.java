package de.redlion.civilwar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Resources {
	
	public final boolean debugMode = true;

	public BitmapFont font;
	public Sprite arrowhead;

	
	public static Resources instance;

	public static Resources getInstance() {
		if (instance == null) {
			instance = new Resources();
		}
		return instance;
	}

	public Resources() {		
		reInit();	
	}
	

	public void reInit() {			
		font = new BitmapFont();
		Texture arrowtex = new Texture(Gdx.files.internal("data/arrowhead.png"));
		arrowhead = new Sprite(arrowtex);
		arrowhead.setRegion(0, 0, 100, 80);
		arrowhead.setScale(0.5f);
	}

	public void dispose() {
		font.dispose();
	}
}
