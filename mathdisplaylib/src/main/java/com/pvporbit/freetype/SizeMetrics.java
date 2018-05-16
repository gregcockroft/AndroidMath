package com.pvporbit.freetype;

import com.pvporbit.freetype.Utils.Pointer;

public class SizeMetrics extends Pointer {

    public SizeMetrics(long pointer) {
        super(pointer);
    }

    public int getAscender() {
        return FreeType.FT_Size_Metrics_Get_ascender(pointer);
    }

    public int getDescender() {
        return FreeType.FT_Size_Metrics_Get_descender(pointer);
    }

    public int getHeight() {
        return FreeType.FT_Size_Metrics_Get_height(pointer);
    }

    public int getMaxAdvance() {
        return FreeType.FT_Size_Metrics_Get_max_advance(pointer);
    }

    public int getXppem() {
        return FreeType.FT_Size_Metrics_Get_x_ppem(pointer);
    }

    public int getYppem() {
        return FreeType.FT_Size_Metrics_Get_y_ppem(pointer);
    }

    public int getXScale() {
        return FreeType.FT_Size_Metrics_Get_x_scale(pointer);
    }

    public int getYScale() {
        return FreeType.FT_Size_Metrics_Get_y_scale(pointer);
    }
}