package com.pvporbit.freetype;

import java.nio.ByteBuffer;

import com.pvporbit.freetype.FreeTypeConstants.FT_Kerning_Mode;
import com.pvporbit.freetype.Utils.Pointer;

/**
 * A handle to a given typographic face object. A face object models a given typeface, in a given style.
 */
public class Face extends Pointer {

    private ByteBuffer data; // Save to delete later

    public Face(long pointer) {
        super(pointer);
    }

    public Face(long pointer, ByteBuffer data) {
        super(pointer);
        this.data = data;
    }

    public boolean delete() {
        if (data != null)
            Utils.deleteBuffer(data);
        return FreeType.FT_Done_Face(pointer);
    }

    public MTFreeTypeMathTable loadMathTable() {
        // Temporary buffer size of font.
        ByteBuffer buffer = Utils.newBuffer(data.remaining());
        MTFreeTypeMathTable fm = new MTFreeTypeMathTable(pointer, buffer);
        Utils.deleteBuffer(buffer);
        return fm;
    }


    public int getAscender() {
        return FreeType.FT_Face_Get_ascender(pointer);
    }

    public int getDescender() {
        return FreeType.FT_Face_Get_descender(pointer);
    }

    public long getFaceFlags() {
        return FreeType.FT_Face_Get_face_flags(pointer);
    }

    public int getFaceIndex() {
        return FreeType.FT_Face_Get_face_index(pointer);
    }

    public String getFamilyName() {
        return FreeType.FT_Face_Get_family_name(pointer);
    }

    public int getHeight() {
        return FreeType.FT_Face_Get_heigth(pointer);
    }

    public int getMaxAdvanceHeight() {
        return FreeType.FT_Face_Get_max_advance_height(pointer);
    }

    public int getMaxAdvanceWidth() {
        return FreeType.FT_Face_Get_max_advance_width(pointer);
    }

    public int getNumFaces() {
        return FreeType.FT_Face_Get_num_faces(pointer);
    }

    public int getNumGlyphs() {
        return FreeType.FT_Face_Get_num_glyphs(pointer);
    }

    public long getStyleFlags() {
        return FreeType.FT_Face_Get_style_flags(pointer);
    }

    public String getStyleName() {
        return FreeType.FT_Face_Get_style_name(pointer);
    }

    public int getUnderlinePosition() {
        return FreeType.FT_Face_Get_underline_position(pointer);
    }

    public int getUnderlineThickness() {
        return FreeType.FT_Face_Get_underline_thickness(pointer);
    }

    public int getUnitsPerEM() {
        return FreeType.FT_Face_Get_units_per_EM(pointer);
    }

    public int getCharIndex(int code) {
        return FreeType.FT_Get_Char_Index(pointer, code);
    }

    public boolean hasKerning() {
        return FreeType.FT_HAS_KERNING(pointer);
    }

    public boolean selectSize(int strikeIndex) {
        return FreeType.FT_Select_Size(pointer, strikeIndex);
    }

    public boolean setCharSize(int char_width, int char_height, int horz_resolution, int vert_resolution) {
        return FreeType.FT_Set_Char_Size(pointer, char_width, char_height, horz_resolution, vert_resolution);
    }

    public boolean loadGlyph(int glyphIndex, int flags) {
        return FreeType.FT_Load_Glyph(pointer, glyphIndex, flags);
    }

    public boolean loadChar(char c, int flags) {
        return FreeType.FT_Load_Char(pointer, c, flags);
    }

    public Kerning getKerning(char left, char right) {
        return getKerning(left, right, FT_Kerning_Mode.FT_KERNING_DEFAULT);
    }

    public Kerning getKerning(char left, char right, FT_Kerning_Mode mode) {
        return FreeType.FT_Face_Get_Kerning(pointer, left, right, mode.ordinal());
    }

    public boolean setPixelSizes(float width, float height) {
        return FreeType.FT_Set_Pixel_Sizes(pointer, width, height);
    }

    public GlyphSlot getGlyphSlot() {
        long glyph = FreeType.FT_Face_Get_glyph(pointer);
        if (glyph == 0)
            return null;
        return new GlyphSlot(glyph);
    }

    public Size getSize() {
        long size = FreeType.FT_Face_Get_size(pointer);
        if (size == 0)
            return null;
        return new Size(size);
    }

    public boolean checkTrueTypePatents() {
        return FreeType.FT_Face_CheckTrueTypePatents(pointer);
    }

    public boolean setUnpatentedHinting(boolean newValue) {
        return FreeType.FT_Face_SetUnpatentedHinting(pointer, newValue);
    }

    public boolean referenceFace() {
        return FreeType.FT_Reference_Face(pointer);
    }

    public boolean requestSize(SizeRequest sr) {
        return FreeType.FT_Request_Size(pointer, sr);
    }

    public int[] getFirstChar() {
        return FreeType.FT_Get_First_Char(pointer);
    }

    public int getFirstCharAsCharcode() {
        return getFirstChar()[0];
    }

    public int getFirstCharAsGlyphIndex() {
        return getFirstChar()[1];
    }

    public int getNextChar(long charcode) { // I will not create getNextCharAsCharcode to do charcode++.
        return FreeType.FT_Get_Next_Char(pointer, charcode);
    }

    public int getGlyphIndexByName(String name) {
        return FreeType.FT_Get_Name_Index(pointer, name);
    }

    public long getTrackKerning(int point_size, int degree) {
        return FreeType.FT_Get_Track_Kerning(pointer, point_size, degree);
    }

    public String getGlyphName(int glyphIndex) {
        return FreeType.FT_Get_Glyph_Name(pointer, glyphIndex);
    }

    public String getPostscriptName() {
        return FreeType.FT_Get_Postscript_Name(pointer);
    }

    public boolean selectCharmap(int encoding) {
        return FreeType.FT_Select_Charmap(pointer, encoding);
    }

    public boolean setCharmap(CharMap charmap) {
        return FreeType.FT_Set_Charmap(pointer, charmap.getPointer());
    }

    public short getFSTypeFlags() {
        return FreeType.FT_Get_FSType_Flags(pointer);
    }
}