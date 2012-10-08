package de.redlion.rts.render;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Plane {
	
	public float x = 0;
	public float y = 0;
	public int width = 0;
	public int height = 0;
	public float rotation = 0f;
	
	public TextureRegion skin;
	
	
	public Plane (float x, float y) {
		this.x = x;
		this.y = y;
		skin = null;
	}
	

	public void setSkin (TextureRegion texture) {
		this.skin = texture;
		width = skin.getRegionWidth();
		height = skin.getRegionHeight();
	}
	
	public Rectangle bounds () {
		return new Rectangle (
				x - width * 0.5f,
				y - height * 0.5f,
				width,
				height
				);
	}
	
	public void update (float dt) {}
	
}

