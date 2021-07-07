package com.fluidsim;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public class FluidSimuation extends ApplicationAdapter {
	ShapeRenderer shapeRenderer;
	Environment e;
	int cellSize;
	
	@Override
	public void create () {
		shapeRenderer = new ShapeRenderer();
		e = new Environment(400, 400);
		//e.initUniform(1, 100, 0);
		//e.setPoint(50, 50, 1, 50, 0);
		e.initRandom();
		cellSize = 2;
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		renderPressure();
		e.step(0.03, 10);
		shapeRenderer.end();
	}
	
	@Override
	public void dispose () {
	}

	private void renderPressure() {
		for(int i = 0; i < e.getWidth(); i++) {
			for (int j = 0; j < e.getHeight(); j++) {
				Cell c = e.getCell(i, j);
				if (c instanceof AirCell) {
					AirCell ac = (AirCell) c;
					double pressure = ac.getPressure() / 10;
					Color color = new Color((float) pressure, 0, 0 ,1);
					shapeRenderer.setColor(color);
					shapeRenderer.rect(i * cellSize, j * cellSize, cellSize, cellSize);
				}
			}
		}
	}
	private void renderVelocityX() {
		for(int i = 0; i < e.getWidth(); i++) {
			for (int j = 0; j < e.getHeight(); j++) {
				Cell c = e.getCell(i, j);
				if (c instanceof AirCell) {
					AirCell ac = (AirCell) c;
					double velocityX = ac.getVelocityX() / 100;
					Color color;
					if (velocityX > 0) {
						color = new Color((float) velocityX, 0, 0 ,1);
					} else {
						color = new Color(0, 0, (float) velocityX,1);
					}
					shapeRenderer.setColor(color);
					shapeRenderer.rect(i * cellSize, j * cellSize, cellSize, cellSize);
				}
			}
		}
	}
}
