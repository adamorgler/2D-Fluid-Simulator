package com.fluidsim;

import java.util.Random;

public class Environment {
    private Cell[][] cells;

    private int width;

    private int height;

    // cell size in meters
    private double cellSize;

    // air density in atm. 1 atm at sea level
    private double density;

    public Environment(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new AirCell[width][height];
        this.cellSize = 0.1;
        this.density = 1;
    }

    public void initUniform(double pressure, double velocityX, double velocityY) {
        for(int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                AirCell ac = new AirCell(i, j);
                ac.setPressure(pressure);
                ac.setVelocityY(velocityY);
                ac.setVelocityX(velocityX);
                cells[i][j] = ac;
            }
        }
    }

    public void initRandom() {
        Random rand = new Random();
        for(int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                AirCell ac = new AirCell(i, j);
                ac.setVelocityY(rand.nextInt(200) - 100);
                ac.setVelocityX(rand.nextInt(200) - 100);
                cells[i][j] = ac;
            }
        }
    }

    public void setPoint(int xPos, int yPos, double pressure, double velocityX, double velocityY) {
        Cell c = cells[xPos][yPos];
        if (c instanceof AirCell) {
            AirCell ac = (AirCell) c;
            ac.setPressure(pressure);
            ac.setVelocityX(velocityX);
            ac.setVelocityY(velocityY);
        }
    }

    public Cell[][] getCells() {
        return cells;
    }

    public void setCells(AirCell[][] cells) {
        this.cells = cells;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Cell getCell(int xPos, int yPos) {
        return cells[xPos][yPos];
    }

    /**
     * Step on iteration of the sim
     * @param time time elapsed in seconds
     * @param accuracy accuracy of pressure gradient, recommend k = 10
     */
    public void step(double time, int accuracy) {
        AirCell[][] advectionField = advection(time);
        double[][] divergence = divergence(advectionField, time);
        double[][] pressureField = pressure(divergence, accuracy);
        finalCalculation(advectionField, pressureField, time);
    }

    // fix this
    private AirCell[][] advection(double time) {
        AirCell[][] output = new AirCell[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Cell c = cells[i][j];
                if (c instanceof AirCell) {
                    AirCell ac;
                    double x = i*cellSize;
                    double y = j*cellSize;
                    x = x - (c.getxPos() * time);
                    y = y - (c.getyPos() * time);
                    if (cells[x][y] instanceof AirCell) {
                        ac = (AirCell) cells[x][y];
                    } else {
                        ac = new AirCell(x, y);
                        ac.setPressure(0);
                        ac.setVelocityY(0);
                        ac.setVelocityX(0);
                    }
                    output[i][j] = ac;
                }
            }
        }
        return output;
    }

    private double[][] divergence(Cell[][] advectionField, double time) {
        double[][] output = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Cell top = advectionField[checkXBounds(i)][checkYBounds(j + 1)];
                Cell bottom = advectionField[checkXBounds(i)][checkYBounds(j - 1)];
                Cell left = advectionField[checkXBounds(i + 1)][checkYBounds(j)];
                Cell right = advectionField[checkXBounds(i + 1)][checkYBounds(j)];
                double next = 0;
                if (top instanceof AirCell) {
                    next += ((AirCell) top).getVelocityY();
                }
                if (bottom instanceof  AirCell) {
                    next -= ((AirCell) bottom).getVelocityY();
                }
                if (left instanceof  AirCell) {
                    next += ((AirCell) left).getVelocityX();
                }
                if (right instanceof  AirCell) {
                    next -= ((AirCell) right).getVelocityX();
                }
                next = next * (((-2) * cellSize * density) / time);
                output[i][j] = next;
            }
        }
        return output;
    }

    private double[][] pressure(double[][] divergance, int k) {
        double[][] output = new double[width][height];
        for(int l = 0; l < k; l++) {
            for(int i = 0; i < width; i++) {
                double temp[][] = new double[width][height];
                for(int j = 0; j < height; j++) {
                    if (l == 0) {
                        output[i][j] = 0;
                    } else {
                        temp[i][j] = (divergance[i][j]
                                + output[checkXBounds(i + 2)][checkYBounds(j)]
                                + output[checkXBounds(i - 2)][checkYBounds(j)]
                                + output[checkXBounds(i)][checkYBounds(j + 2)]
                                + output[checkXBounds(i)][checkYBounds(j - 2)]) / 4;
                    }
                }
                if (l != 0) {
                    output = temp;
                }
            }
        }
        return output;
    }

    private void finalCalculation(AirCell[][] advection, double[][] pressureField, double time) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Cell c = cells[i][j];
                if (c instanceof AirCell) {
                    AirCell ac = (AirCell) c;
                    double velocityX = advection[i][j].getVelocityX()
                            - ((time / (2 * density * cellSize))
                            * (pressureField[checkXBounds(i + 1)][j]
                            - pressureField[checkXBounds(i - 1)][j]));
                    double velocityY = advection[i][j].getVelocityY()
                            - ((time / (2 * density * cellSize))
                            * (pressureField[i][checkYBounds(j + 1)]
                            - pressureField[i][checkYBounds(j - 1)]));
                    double cellPressure = pressureField[i][j];
                    ac.setVelocityX(velocityX);
                    ac.setVelocityY(velocityY);
                    ac.setPressure(cellPressure);
                    cells[i][j] = ac;
                }
            }
        }
    }

    private int checkXBounds(int xPos) {
        while (xPos < 0 || xPos >= width) {
            if (xPos < 0) {
                xPos += width;
            } else if (xPos >= width) {
                xPos -= width;
            }
        }
        return xPos;
    }
    private int checkYBounds(int yPos) {
        while (yPos < 0 || yPos >= height) {
            if (yPos < 0) {
                yPos += height;
            } else if (yPos >= height) {
                yPos -= height;
            }
        }
        return yPos;
    }
}
