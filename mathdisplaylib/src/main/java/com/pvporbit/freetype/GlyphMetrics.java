package com.pvporbit.freetype;

import com.pvporbit.freetype.Utils.Pointer;

public class GlyphMetrics extends Pointer {

    public GlyphMetrics(long pointer) {
        super(pointer);
    }

    public int getWidth() {
        return FreeType.FT_Glyph_Metrics_Get_width(pointer);
    }

    public int getHeight() {
        return FreeType.FT_Glyph_Metrics_Get_height(pointer);
    }

    public int getHoriAdvance() {
        return FreeType.FT_Glyph_Metrics_Get_horiAdvance(pointer);
    }

    public int getVertAdvance() {
        return FreeType.FT_Glyph_Metrics_Get_vertAdvance(pointer);
    }

    public int getHoriBearingX() {
        return FreeType.FT_Glyph_Metrics_Get_horiBearingX(pointer);
    }

    public int getHoriBearingY() {
        return FreeType.FT_Glyph_Metrics_Get_horiBearingY(pointer);
    }

    public int getVertBearingX() {
        return FreeType.FT_Glyph_Metrics_Get_vertBearingX(pointer);
    }

    public int getVertBearingY() {
        return FreeType.FT_Glyph_Metrics_Get_vertBearingY(pointer);
    }
}