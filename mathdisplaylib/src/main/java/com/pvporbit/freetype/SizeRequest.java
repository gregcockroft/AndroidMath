package com.pvporbit.freetype;

import com.pvporbit.freetype.FreeTypeConstants.FT_Size_Request_Type;

public class SizeRequest {

    private int type;
    private int width, height;
    private int horiResolution, vertResolution;

    public SizeRequest(FT_Size_Request_Type type, int width, int height, int horiResolution, int vertResolution) {
        this.type = type.ordinal();
        this.width = width;
        this.height = height;
        this.horiResolution = horiResolution;
        this.vertResolution = vertResolution;
    }

    public FT_Size_Request_Type getType() {
        return FT_Size_Request_Type.values()[type];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getVertResolution() {
        return vertResolution;
    }

    public int getHoriResolution() {
        return horiResolution;
    }

    public void setType(FT_Size_Request_Type type) {
        this.type = type.ordinal();
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setHoriResolution(int horiResolution) {
        this.horiResolution = horiResolution;
    }

    public void setVertResolution(int vertResolution) {
        this.vertResolution = vertResolution;
    }
}