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
	ShapeRenderer shapeRenderer;

	Environment e;

	int cellSize;

	// 1 - pressure
	// 2 - velocity X
	// 3 - velocity Y
	int displayMode;
	
	@Override
	public void create () {
		shapeRenderer = new ShapeRenderer();
		e = new Environment(200, 200);
		//e.initUniform(0, 30, 0);
		e.initRandom();
		e.initCentralWall(20,20);

		cellSize = 4;
		displayMode = 2;
	}

	@Override
	public void render () {
		ScreenUtils.clear(0, 0, 0, 1);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		renderMode();
		shapeRenderer.end();

		step();
		keyBinds();
	}
	
	@Override
	public void dispose () {
	}

	private void keyBinds() {
		Input input = Gdx.input;

		if (input.isKeyPressed(Input.Keys.NUM_1)) {
			displayMode = 1;
			return;
		}
		if (input.isKeyPressed(Input.Keys.NUM_2)) {
			displayMode = 2;
			return;
		}
		if (input.isKeyPressed(Input.Keys.NUM_3)) {
			displayMode = 3;
			return;
		}
	}

	private void renderMode() {
		switch(displayMode) {
			case(1):
				renderPressure();
				break;
			case(2):
				renderVelocityX();
				break;
			case(3):
				renderVelocityY();
				break;
			default:
				renderVelocityX();
				displayMode = 2;
				break;
		}
	}

	private void step() {
		e.step(0.03, 10);
	}

	private void renderPressure() {
		for(int i = 0; i < e.getWidth(); i++) {
			for (int j = 0; j < e.getHeight(); j++) {
				Cell c = e.getCell(i, j);
				Color color = new Color(Color.PINK);
				if (c instanceof AirCell) {
					AirCell ac = (AirCell) c;
					double pressure = ac.getPressure() / 10;
					if (pressure > 0) {
						color = new Color((float) pressure, 0, 0 ,1);
					} else {
						color = new Color(0, 0, Math.abs((float) pressure),1);
					}
				}
				else if(c instanceof WallCell) {
					color = new Color(Color.BROWN);
				}
				shapeRenderer.setColor(color);
				shapeRenderer.rect(i * cellSize, j * cellSize, cellSize, cellSize);
			}
		}
	}
	private void renderVelocityX() {
		for(int i = 0; i < e.getWidth(); i++) {
			for (int j = 0; j < e.getHeight(); j++) {
				Cell c = e.getCell(i, j);
				Color color = new Color(Color.PINK);
				if (c instanceof AirCell) {
					AirCell ac = (AirCell) c;
					double velocityX = ac.getVelocityX() / 10;
					if (velocityX > 0) {
						color = new Color((float) velocityX, 0, 0 ,1);
					} else {
						color = new Color(0, 0, Math.abs((float) velocityX),1);
					}
				} else if(c instanceof WallCell) {
					color = new Color(Color.BROWN);
				}
				shapeRenderer.setColor(color);
				shapeRenderer.rect(i * cellSize, j * cellSize, cellSize, cellSize);
			}
		}
	}

	private void renderVelocityY() {
		for(int i = 0; i < e.getWidth(); i++) {
			for (int j = 0; j < e.getHeight(); j++) {
				Cell c = e.getCell(i, j);
				Color color = new Color(Color.PINK);
				if (c instanceof AirCell) {
					AirCell ac = (AirCell) c;
					double velocityY = ac.getVelocityY() / 10;
					if (velocityY > 0) {
						color = new Color((float) velocityY, 0, 0 ,1);
					} else {
						color = new Color(0, 0, Math.abs((float) velocityY),1);
					}
				} else if(c instanceof WallCell) {
					color = new Color(Color.BROWN);
				}
				shapeRenderer.setColor(color);
				shapeRenderer.rect(i * cellSize, j * cellSize, cellSize, cellSize);
			}
		}
	}
}
