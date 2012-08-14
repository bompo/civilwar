package de.redlion.rts;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class Resources {
	
	public final boolean debugMode = true;

	public BitmapFont font;

	
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
	}

	public void dispose() {
		font.dispose();
	}
}
