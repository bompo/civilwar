package de.redlion.rts.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Assets {
	
	public static Texture planes;
	public static TextureRegion planeFrame1;
	public static TextureRegion dash;
	public static TextureRegion dot;
	public static Animation planeAnimation;
	
	
	public static Texture loadTexture (String file) {
		return new Texture(Gdx.files.internal(file));
	}

	public static void load () {
		planes = loadTexture("data/planeSprites.png");
		planeFrame1 =  new TextureRegion (planes, 14, 2, 63, 64);
		dash =  new TextureRegion (planes, 2, 2, 5, 2);
		dot =  new TextureRegion (planes, 9, 2, 3, 3);
		
		planeAnimation = new Animation (0.2f, 
				planeFrame1,
				new TextureRegion (planes, 2, 68, 63, 64)
				);
	}
	
}
