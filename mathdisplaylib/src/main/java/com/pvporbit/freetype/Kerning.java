package com.pvporbit.freetype;

public class Kerning {

    private final int x, y;

    public Kerning(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getHorizontalKerning() {
        return x;
    }

    public int getVerticalKerning() {
        return y;
    }

    @Override
    public String toString() {
        return "Kerning(" + x + ", " + y + ")";
    }
}