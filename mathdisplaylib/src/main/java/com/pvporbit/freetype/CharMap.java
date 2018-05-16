package com.pvporbit.freetype;

import com.pvporbit.freetype.Utils.Pointer;

public class CharMap extends Pointer {

    public CharMap(long pointer) {
        super(pointer);
    }

    public static int getCharmapIndex(CharMap charmap) {
        return FreeType.FT_Get_Charmap_Index(charmap.getPointer());
    }
}