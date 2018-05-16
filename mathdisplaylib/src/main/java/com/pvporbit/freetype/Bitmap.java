package com.pvporbit.freetype;

import java.nio.ByteBuffer;

import com.pvporbit.freetype.Utils.Pointer;

public class Bitmap extends Pointer {

    public Bitmap(long pointer) {
        super(pointer);
    }

    public int getWidth() {
        return FreeType.FT_Bitmap_Get_width(pointer);
    }

    public int getRows() {
        return FreeType.FT_Bitmap_Get_rows(pointer);
    }

    public int getPitch() {
        return FreeType.FT_Bitmap_Get_pitch(pointer);
    }

    public short getNumGrays() {
        return FreeType.FT_Bitmap_Get_num_grays(pointer);
    }

    public char getPaletteMode() {
        return FreeType.FT_Bitmap_Get_palette_mode(pointer);
    }

    public char getPixelMode() {
        return FreeType.FT_Bitmap_Get_pixel_mode(pointer);
    }

    public ByteBuffer getBuffer() {
        return FreeType.FT_Bitmap_Get_buffer(pointer);
    }
}