package com.fluidsim;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public class GUIController {

    private Environment env;

    private ShapeRenderer shapeRenderer;

    int cellSize;

    // 1 - pressure
    // 2 - velocity X
    // 3 - velocity Y
    int displayMode;

    public GUIController(Environment env) {
        this.shapeRenderer = new ShapeRenderer();
        this.env = env;

        this.cellSize = 4;
        this.displayMode = 2;
    }

    public void render() {
        checkInput();
        ScreenUtils.clear(0, 0, 0, 1);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        renderCells();
        shapeRenderer.end();
    }

    // checks for user inputs
    private void checkInput() {
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

    private void renderCells() {
        int width = env.getWidth();
        int height = env.getHeight();

        for(int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {

                Cell c = env.getCell(i, j);
                Color color = new Color(Color.PINK);

                if (c instanceof AirCell) {
                    AirCell ac = (AirCell) c;
                    switch(displayMode) {
                        case 1: {
                            color = getPressureColor(ac);
                            break;
                        }
                        case 2: {
                            color = getVelocityXColor(ac);
                            break;
                        }
                        case 3: {
                            color = getVelocityYColor(ac);
                            break;
                        }
                        default: {
                            color = getVelocityXColor(ac);
                            displayMode = 2;
                            break;
                        }
                    }
                } else if(c instanceof WallCell) {
                    color = new Color(Color.BROWN);
                }
                shapeRenderer.setColor(color);
                shapeRenderer.rect(i * cellSize, j * cellSize, cellSize, cellSize);
            }
        }
    }

    private Color getVelocityXColor(AirCell ac) {
        double v = ac.getVelocityX();
        double minV = env.getMinVelocityX();
        double maxV = env.getMaxVelocityX();
        double grad = normalizeColorGradient(v, minV, maxV);
        Color color = normalizedColor(grad);
        return color;
    }

    private Color getVelocityYColor(AirCell ac) {
        double v = ac.getVelocityY();
        double minV = env.getMinVelocityY();
        double maxV = env.getMaxVelocityY();
        double grad = normalizeColorGradient(v, minV, maxV);
        Color color = normalizedColor(grad);
        return color;
    }

    private Color getPressureColor(AirCell ac) {
        double v = ac.getPressure();
        double minV = env.getMinPressure();
        double maxV = env.getMaxPressure();
        double grad = normalizeColorGradient(v, minV, maxV);
        Color color = normalizedColor(grad);
        return color;
    }

    // returns a value between -1 and 1 proportional to the inputs position in the range between the min and max values
    private double normalizeColorGradient(double value, double min, double max) {
        if (min > max) {
            return 0;
        }
        if (value > max) {
            return 1;
        }
        if (value < min) {
            return -1;
        }
        double top;
        if (Math.abs(min) < Math.abs(max)) {
            top = Math.abs(max);
        } else {
            top = Math.abs(min);
        }
        return value / top;
    }

    private Color normalizedColor(double value) {
        Color color;
        if (value > 0) {
            color = new Color((float) value, 0, 0 ,1);
        } else {
            color = new Color(0, 0, Math.abs((float) value),1);
        }
        return color;
    }
}
