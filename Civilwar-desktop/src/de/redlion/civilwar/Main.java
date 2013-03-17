package de.redlion.civilwar;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import de.redlion.civilwar.Civilwar;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Civilwar";
		cfg.useGL20 = true;
		cfg.samples = 8;
		cfg.width = 1280;
		cfg.height = 800;
		
		new LwjglApplication(new Civilwar(), cfg);
	}
}
