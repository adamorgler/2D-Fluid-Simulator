package com.fluidsim;

public abstract class Cell {

    private final int xPos;

    private final int yPos;

    protected Cell(int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }
}
