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

    //acceleration due ot gravity
    private double g;

    //mass of air in kg
    private double airmass;

    //vorticity of air
    private double vorticity;

    public Environment(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];
        this.cellSize = 1;
        this.density = 1;
        this.g = 9.81;
        this.airmass = 1.293;
        this.vorticity = 0.0001;
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
                ac.setPressure(rand.nextDouble());
                ac.setVelocityY((rand.nextDouble() * 200) - 100);
                ac.setVelocityX((rand.nextDouble() * 200) - 100);
                cells[i][j] = ac;
            }
        }
    }

    public void initCentralWall(int width, int height) {
        int x = (this.width / 2) - (width / 2);
        int y = (this.height / 2) - (height / 2);
        for(int i = x; i < x + width; i++) {
            for(int j = y; j < y + height; j++) {
                WallCell wc = new WallCell(i, j);
                cells[i][j] = wc;
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
        addForces(time);
        vorticityConfinement(time);
    }

    private AirCell[][] advection(double time) {
        AirCell[][] output = new AirCell[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Cell c = cells[i][j];
                if (c instanceof AirCell) {
                    double x = i*cellSize;
                    double y = j*cellSize;
                    x = x - (((AirCell) c).getVelocityX() * time);
                    y = y - (((AirCell) c).getVelocityY() * time);
                    double velocityX = bilinearInterpolateVelocityX(x, y);
                    double velocityY = bilinearInterpolateVelocityY(x, y);
                    AirCell ac = new AirCell(i, j);
                    ac.setVelocityX(velocityX);
                    ac.setVelocityY(velocityY);
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
                Cell top = advectionField[wrapXBounds(i)][wrapYBounds(j + 1)];
                Cell bottom = advectionField[wrapXBounds(i)][wrapYBounds(j - 1)];
                Cell left = advectionField[wrapXBounds(i - 1)][wrapYBounds(j)];
                Cell right = advectionField[wrapXBounds(i + 1)][wrapYBounds(j)];
                double next = 0;
                if (top instanceof AirCell) {
                    next += ((AirCell) top).getVelocityY();
                }
                if (bottom instanceof  AirCell) {
                    next -= ((AirCell) bottom).getVelocityY();
                }
                if (right instanceof  AirCell) {
                    next += ((AirCell) right).getVelocityX();
                }
                if (left instanceof  AirCell) {
                    next -= ((AirCell) left).getVelocityX();
                }
                next = next * ((-2 * cellSize * density) / time);
                output[i][j] = next;
            }
        }
        return output;
    }

    private double[][] pressure(double[][] divergance, int k) {
        double[][] output = new double[width][height];
        for(int l = 0; l < k; l++) {
            double temp[][] = new double[width][height];
            for(int i = 0; i < width; i++) {
                for(int j = 0; j < height; j++) {
                    if (l == 0) {
                        temp[i][j] = 0;
                    } else {
                        temp[i][j] = (divergance[i][j]
                                + output[wrapXBounds(i + 2)][wrapYBounds(j)]
                                + output[wrapXBounds(i - 2)][wrapYBounds(j)]
                                + output[wrapXBounds(i)][wrapYBounds(j + 2)]
                                + output[wrapXBounds(i)][wrapYBounds(j - 2)]) / 4;
                    }
                }
            }
            output = temp;
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
                            * (pressureField[wrapXBounds(i + 1)][j]
                            - pressureField[wrapXBounds(i - 1)][j]));
                    double velocityY = advection[i][j].getVelocityY()
                            - ((time / (2 * density * cellSize))
                            * (pressureField[i][wrapYBounds(j + 1)]
                            - pressureField[i][wrapYBounds(j - 1)]));
                    double cellPressure = pressureField[i][j];
                    ac.setVelocityX(velocityX);
                    ac.setVelocityY(velocityY);
                    ac.setPressure(cellPressure);
                    cells[i][j] = ac;
                }
            }
        }
    }

    private void addForces(double time) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Cell c = cells[i][j];
                if (c instanceof AirCell) {
                    AirCell ac = (AirCell) c;
                    double velocityX = ac.getVelocityX();
                    double velocityY = ac.getVelocityY();
                    double pressure = ac.getPressure();

//                    velocityY += forceOfGravity(time);
                    if (i >= 0 && i < 2 && j > (4 * height / 10) && j < (6 * height / 10)) {
                        velocityX += 100 * time;
                    }

                    ac.setVelocityX(velocityX);
                    ac.setVelocityY(velocityY);
                    ac.setPressure(pressure);
                }
            }
        }
    }

    private double curl(int x, int y) {
        if(checkXBounds(x) && checkYBounds(y)) {
            Cell c;
            AirCell ac;
            double curl = 0;
            c = cells[wrapXBounds(x + 1)][wrapYBounds(y)];
            if (c instanceof AirCell) {
                ac = (AirCell) c;
                curl += ac.getVelocityY();
            }
            c = cells[wrapXBounds(x - 1)][wrapYBounds(y)];
            if (c instanceof AirCell) {
                ac = (AirCell) c;
                curl -= ac.getVelocityY();
            }
            c = cells[wrapXBounds(x)][wrapYBounds(y + 1)];
            if (c instanceof AirCell) {
                ac = (AirCell) c;
                curl += ac.getVelocityX();
            }
            c = cells[wrapXBounds(x)][wrapYBounds(y - 1)];
            if (c instanceof AirCell) {
                ac = (AirCell) c;
                curl -= ac.getVelocityX();
            }
            return curl;
        }
        return 0;
    }

    private double forceOfGravity(double time) {
        return -(airmass * g) * time;
    }

    private void vorticityConfinement(double time) {
        double[][][] temp = new double[2][width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (checkXBounds(i) && checkYBounds(j) && cells[i][j] instanceof AirCell) {
                    AirCell ac = (AirCell) cells[i][j];
                    double dx = Math.abs(curl(i, j - 1)) - Math.abs(curl(i, j + 1));
                    double dy = Math.abs(curl(i + 1, j)) - Math.abs(curl(i - 1, j));
                    double len = Math.sqrt((dx * dx) + (dy * dy)) + 0.00001;
                    dx = vorticity / len * dx;
                    dy = vorticity / len * dy;
                    double velocityX = ac.getVelocityX();
                    double velocityY = ac.getVelocityY();
                    velocityX += time * curl(i, j) * dx;
                    velocityY += time * curl(i, j) * dy;
                    temp[0][i][j] = velocityX;
                    temp[1][i][j] = velocityY;
                }
            }
        }
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (checkXBounds(i) && checkYBounds(j) && cells[i][j] instanceof AirCell) {
                    AirCell ac = (AirCell) cells[i][j];
                    ac.setVelocityX(temp[0][i][j]);
                    ac.setVelocityY(temp[1][i][j]);
                }
            }
        }

    }

//    private double bilinearInterpolatePressure(double x, double y) {
//        double output = 0;
//        x = checkXBoundsDouble(x);
//        y = checkYBoundsDouble(y);
//        int x1 = checkXBounds((int) Math.floor(x));
//        int x2 = checkXBounds((int) Math.ceil(x));
//        int y1 = checkYBounds((int) Math.floor(y));
//        int y2 = checkYBounds((int) Math.ceil(y));
//        double p11 = 0;
//        double p12 = 0;
//        double p21 = 0;
//        double p22 = 0;
//        Cell c;
//        c = cells[x1][y1];
//        if (c instanceof AirCell) {
//            p11 = ((AirCell) c).getPressure();
//        }
//        c = cells[x1][y2];
//        if (c instanceof AirCell) {
//            p12 = ((AirCell) c).getPressure();
//        }
//        c = cells[x2][y1];
//        if (c instanceof AirCell) {
//            p21 = ((AirCell) c).getPressure();
//        }
//        c = cells[x2][y2];
//        if (c instanceof AirCell) {
//            p22 = ((AirCell) c).getPressure();
//        }
//        // https://en.wikipedia.org/wiki/Bilinear_interpolation
//        double xy1 = (((x2 - x) / (x2 - x1)) * p11) + (((x - x1) / (x2 - x1)) * p21);
//        double xy2 = (((x2 - x) / (x2 - x1)) * p12) + (((x - x1) / (x2 - x1)) * p22);
//        output = (((y2 - y) / (y2 - y1)) * xy1) + (((y - y1) / (y2 - y1)) * xy2);
//
//        return output;
//    }

    private double bilinearInterpolateVelocityX(double x, double y) {
        double output = 0;
        x = wrapXBoundsDouble(x);
        y = wrapYBoundsDouble(y);
        int x1 = wrapXBounds((int) Math.floor(x));
        int x2 = wrapXBounds((int) Math.ceil(x));
        int y1 = wrapYBounds((int) Math.floor(y));
        int y2 = wrapYBounds((int) Math.ceil(y));
        double v11 = 0;
        double v12 = 0;
        double v21 = 0;
        double v22 = 0;
        Cell c;
        c = cells[x1][y1];
        if (c instanceof AirCell) {
            v11 = ((AirCell) c).getVelocityX();
        }
        c = cells[x1][y2];
        if (c instanceof AirCell) {
            v12 = ((AirCell) c).getVelocityX();
        }
        c = cells[x2][y1];
        if (c instanceof AirCell) {
            v21 = ((AirCell) c).getVelocityX();
        }
        c = cells[x2][y2];
        if (c instanceof AirCell) {
            v22 = ((AirCell) c).getVelocityX();
        }
        // https://en.wikipedia.org/wiki/Bilinear_interpolation
        double xy1 = (((x2 - x) / (x2 - x1)) * v11) + (((x - x1) / (x2 - x1)) * v21);
        double xy2 = (((x2 - x) / (x2 - x1)) * v12) + (((x - x1) / (x2 - x1)) * v22);
        output = (((y2 - y) / (y2 - y1)) * xy1) + (((y - y1) / (y2 - y1)) * xy2);
        return output;
    }

    private double bilinearInterpolateVelocityY(double x, double y) {
        double output = 0;
        x = wrapXBoundsDouble(x);
        y = wrapYBoundsDouble(y);
        int x1 = wrapXBounds((int) Math.floor(x));
        int x2 = wrapXBounds((int) Math.ceil(x));
        int y1 = wrapYBounds((int) Math.floor(y));
        int y2 = wrapYBounds((int) Math.ceil(y));
        double v11 = 0;
        double v12 = 0;
        double v21 = 0;
        double v22 = 0;
        Cell c;
        c = cells[x1][y1];
        if (c instanceof AirCell) {
            v11 = ((AirCell) c).getVelocityY();
        }
        c = cells[x1][y2];
        if (c instanceof AirCell) {
            v12 = ((AirCell) c).getVelocityY();
        }
        c = cells[x2][y1];
        if (c instanceof AirCell) {
            v21 = ((AirCell) c).getVelocityY();
        }
        c = cells[x2][y2];
        if (c instanceof AirCell) {
            v22 = ((AirCell) c).getVelocityY();
        }
        // https://en.wikipedia.org/wiki/Bilinear_interpolation
        double xy1 = (((x2 - x) / (x2 - x1)) * v11) + (((x - x1) / (x2 - x1)) * v21);
        double xy2 = (((x2 - x) / (x2 - x1)) * v12) + (((x - x1) / (x2 - x1)) * v22);
        output = (((y2 - y) / (y2 - y1)) * xy1) + (((y - y1) / (y2 - y1)) * xy2);
        return output;
    }

    private int wrapXBounds(int xPos) {
        while (xPos < 0 || xPos >= width) {
            if (xPos < 0) {
                xPos += width;
            } else if (xPos >= width) {
                xPos -= width;
            }
        }
        return xPos;
    }
    private int wrapYBounds(int yPos) {
        while(yPos < 0 || yPos >= height) {
            if (yPos < 0) {
                yPos += height;
            } else if (yPos >= height) {
                yPos -= height;
            }
        }
        return yPos;
    }

    private double wrapXBoundsDouble(double xPos) {
        while (xPos < 0 || xPos >= width) {
            if (xPos < 0) {
                xPos += width;
            } else if (xPos >= width) {
                xPos -= width;
            }
        }
        return xPos;
    }
    private double wrapYBoundsDouble(double yPos) {
        while(yPos < 0 || yPos >= height) {
            if (yPos < 0) {
                yPos += height;
            } else if (yPos >= height) {
                yPos -= height;
            }
        }
        return yPos;
    }

    private boolean checkXBounds(int xPos) {
        return (xPos >= 0 && xPos < width);
    }
    private boolean checkYBounds(int yPos) {
        return (yPos >= 0 && yPos < height);
    }
}
