package de.redlion.civilwar;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import de.redlion.civilwar.Civilwar;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Civilwar";
		cfg.useGL20 = true;
		cfg.samples = 4;
		cfg.width = 480;
		cfg.height = 320;
		
		new LwjglApplication(new Civilwar(), cfg);
	}
}
