package com.fluidsim.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.fluidsim.FluidSimuation;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.foregroundFPS = 30;
		config.height = 800;
		config.width = 800;
		new LwjglApplication(new FluidSimuation(), config);
	}
}
