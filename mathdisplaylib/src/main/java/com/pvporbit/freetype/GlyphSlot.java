package com.pvporbit.freetype;

import com.pvporbit.freetype.FreeTypeConstants.FT_Render_Mode;
import com.pvporbit.freetype.Utils.Pointer;

public class GlyphSlot extends Pointer {

    public GlyphSlot(long pointer) {
        super(pointer);
    }

    public Bitmap getBitmap() {
        long bitmap = FreeType.FT_GlyphSlot_Get_bitmap(pointer);
        if (bitmap == 0)
            return null;
        return new Bitmap(bitmap);
    }

    public long getLinearHoriAdvance() {
        return FreeType.FT_GlyphSlot_Get_linearHoriAdvance(pointer);
    }

    public long getLinearVertAdvance() {
        return FreeType.FT_GlyphSlot_Get_linearVertAdvance(pointer);
    }

    public Advance getAdvance() {
        return FreeType.FT_GlyphSlot_Get_advance(pointer);
    }

    public int getFormat() {
        return FreeType.FT_GlyphSlot_Get_format(pointer);
    }

    public int getBitmapLeft() {
        return FreeType.FT_GlyphSlot_Get_bitmap_left(pointer);
    }

    public int getBitmapTop() {
        return FreeType.FT_GlyphSlot_Get_bitmap_top(pointer);
    }

    public GlyphMetrics getMetrics() {
        long metrics = FreeType.FT_GlyphSlot_Get_metrics(pointer);
        if (metrics == 0)
            return null;
        return new GlyphMetrics(metrics);
    }

    public boolean renderGlyph(FT_Render_Mode renderMode) {
        return FreeType.FT_Render_Glyph(pointer, renderMode.ordinal());
    }

    public static class Advance {

        private final long x, y;

        public Advance(long x, long y) {
            this.x = x;
            this.y = y;
        }

        public long getX() {
            return x;
        }

        public long getY() {
            return y;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }
}