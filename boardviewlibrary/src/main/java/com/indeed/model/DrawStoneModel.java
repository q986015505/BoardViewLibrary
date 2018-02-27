package com.indeed.model;

/**
 * Created by yandequan on 18/1/23.
 */

public class DrawStoneModel {

    public DrawStoneModel(int x, int y, int c) {
        this.x = x;
        this.y = y;
        this.c = c;
    }

    public DrawStoneModel() {
    }

    private int x = -1;
    private int y = -1;
    private int c;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }
}
