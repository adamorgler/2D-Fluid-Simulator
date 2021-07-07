package com.fluidsim;

public class AirCell extends Cell {

    private double velocityX;

    private double velocityY;

    private double pressure;

    public AirCell(int xPos, int yPos) {
        super(xPos, yPos);
    }

    public double getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }
}
