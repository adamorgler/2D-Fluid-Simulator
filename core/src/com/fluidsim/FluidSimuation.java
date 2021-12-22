package com.fluidsim;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public class FluidSimuation extends ApplicationAdapter {

	Environment e;

	GUIController gui;
	
	@Override
	public void create () {
		e = new Environment(200, 200);
		//e.initUniform(0, 30, 0);
		e.initRandom();
		e.initFloor();
		e.initCentralWall(20, 20);

		gui = new GUIController(e);
	}

	@Override
	public void render () {
		step();
		gui.render();
	}
	
	@Override
	public void dispose () {
	}

	private void step() {

		e.step(0.03, 10);
	}
}
