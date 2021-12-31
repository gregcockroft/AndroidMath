package com.pvporbit.freetype;

import java.nio.ByteBuffer;

import com.pvporbit.freetype.GlyphSlot.Advance;

public class FreeType {

    /* FreeType functions */

    public static native long FT_Init_FreeType();

    // ---- Library
    public static native boolean FT_Done_FreeType(long library);

    public static native LibraryVersion FT_Library_Version(long library); // [major, minor, patch]

    //	public static native long           FT_Open_Face(long library, FT_Open_Args args, long faceIndex); // Nope.
    //	public static native long           FT_New_Face(long library, String filepathname, long faceIndex); // Please use 'FT_New_Memory_Face' or preferable 'library.newFace(path)'
    public static native long FT_New_Memory_Face(long library, ByteBuffer data, int length, long faceIndex);

    // -- For getting math table
	/*
	  Uses FT_Load_Sfnt_Table with Tag fixed to MATH
	  data is preallocated and must be large enough to hold entire math table


     Example of stub generation
	 imac-3:mathdisplaylib greg$ javah -v -cp build/intermediates/classes/debug -d src/main/cpp/ com.pvporbit.freetype.FreeType


	 */
    public static native boolean FT_Load_Math_Table(long face, ByteBuffer data, int length);


    // ---- Face
    public static native int FT_Face_Get_ascender(long face);

    public static native int FT_Face_Get_descender(long face);

    public static native long FT_Face_Get_face_flags(long face);

    public static native int FT_Face_Get_face_index(long face);

    public static native String FT_Face_Get_family_name(long face);

    public static native int FT_Face_Get_heigth(long face);

    public static native int FT_Face_Get_max_advance_height(long face);

    public static native int FT_Face_Get_max_advance_width(long face);

    public static native int FT_Face_Get_num_faces(long face);

    public static native int FT_Face_Get_num_glyphs(long face);

    public static native long FT_Face_Get_style_flags(long face);

    public static native String FT_Face_Get_style_name(long face);

    public static native int FT_Face_Get_underline_position(long face);

    public static native int FT_Face_Get_underline_thickness(long face);

    public static native int FT_Face_Get_units_per_EM(long face);

    public static native long FT_Face_Get_glyph(long face); /* Pointer to FT_GlyphSlot */

    public static native long FT_Face_Get_size(long face); /* Pointer to FT_Size */

    public static native long FT_Get_Track_Kerning(long face, long point_size, int degree);

    public static native Kerning FT_Face_Get_Kerning(long face, char left, char right, int mode);

    public static native boolean FT_Done_Face(long face);

    public static native boolean FT_Reference_Face(long face);

    public static native boolean FT_HAS_KERNING(long face);

    public static native String FT_Get_Postscript_Name(long face);

    public static native boolean FT_Select_Charmap(long face, int encoding);

    public static native boolean FT_Set_Charmap(long face, long charmap);

    public static native boolean FT_Face_CheckTrueTypePatents(long face);

    public static native boolean FT_Face_SetUnpatentedHinting(long face, boolean value);

    public static native int[] FT_Get_First_Char(long face); // [charcode, glyphIndex]

    public static native int FT_Get_Next_Char(long face, long charcode);

    public static native int FT_Get_Char_Index(long face, int code);

    public static native int FT_Get_Name_Index(long face, String name);

    public static native String FT_Get_Glyph_Name(long face, int glyphIndex);

    public static native short FT_Get_FSType_Flags(long face);

    public static native boolean FT_Select_Size(long face, int strikeIndex);

    public static native boolean FT_Load_Char(long face, char c, int flags);

    //	public static native boolean FT_Attach_File              (long face, String filepathname); // Nope.
//	public static native boolean FT_Attach_Stream            (long face, FT_Open_Args parameters); // Nope.
//	public static native boolean FT_Set_Transform            (long face, FT_Matrix* matrix, FT_Vector* delta);
    public static native boolean FT_Request_Size(long face, SizeRequest sizeRequest);

    public static native boolean FT_Set_Pixel_Sizes(long face, float width, float height);

    public static native boolean FT_Load_Glyph(long face, int glyphIndex, int loadFlags);

    public static native boolean FT_Set_Char_Size(long face, int char_width, int char_height, int horz_resolution, int vert_resolution);

    // ---- Size
    public static native long FT_Size_Get_metrics(long size); /* Pointer to SizeMetrics */

    // ---- Size Metrics
    public static native int FT_Size_Metrics_Get_ascender(long sizeMetrics);

    public static native int FT_Size_Metrics_Get_descender(long sizeMetrics);

    public static native int FT_Size_Metrics_Get_height(long sizeMetrics);

    public static native int FT_Size_Metrics_Get_max_advance(long sizeMetrics);

    public static native int FT_Size_Metrics_Get_x_ppem(long sizeMetrics);

    public static native int FT_Size_Metrics_Get_x_scale(long sizeMetrics);

    public static native int FT_Size_Metrics_Get_y_ppem(long sizeMetrics);

    public static native int FT_Size_Metrics_Get_y_scale(long sizeMetrics);

    // ---- GlyphSlot
    public static native long FT_GlyphSlot_Get_linearHoriAdvance(long glyphSlot);

    public static native long FT_GlyphSlot_Get_linearVertAdvance(long glyphSlot);

    public static native Advance FT_GlyphSlot_Get_advance(long glyphSlot);

    public static native int FT_GlyphSlot_Get_format(long glyphSlot);

    public static native int FT_GlyphSlot_Get_bitmap_left(long glyphSlot);

    public static native int FT_GlyphSlot_Get_bitmap_top(long glyphSlot);

    public static native long FT_GlyphSlot_Get_bitmap(long glyphSlot); /* Pointer to Bitmap */

    public static native long FT_GlyphSlot_Get_metrics(long glyphSlot); /* Pointer to GlyphMetrics */

    //	public static native long     FT_Get_Glyph                      (long glyphSlot); /* Pointer to Glyph */
//	public static native SubGlyph FT_Get_SubGlyph_Info				(long glyphSlot, int subIndex);
    public static native boolean FT_Render_Glyph(long glyphSlot, int renderMode);

    // ---- GlyphMetrics
    public static native int FT_Glyph_Metrics_Get_width(long glyphMetrics);

    public static native int FT_Glyph_Metrics_Get_height(long glyphMetrics);

    public static native int FT_Glyph_Metrics_Get_horiAdvance(long glyphMetrics);

    public static native int FT_Glyph_Metrics_Get_vertAdvance(long glyphMetrics);

    public static native int FT_Glyph_Metrics_Get_horiBearingX(long glyphMetrics);

    public static native int FT_Glyph_Metrics_Get_horiBearingY(long glyphMetrics);

    public static native int FT_Glyph_Metrics_Get_vertBearingX(long glyphMetrics);

    public static native int FT_Glyph_Metrics_Get_vertBearingY(long glyphMetrics);

    // ---- Bitmap
    public static native int FT_Bitmap_Get_width(long bitmap);

    public static native int FT_Bitmap_Get_rows(long bitmap);

    public static native int FT_Bitmap_Get_pitch(long bitmap);

    public static native short FT_Bitmap_Get_num_grays(long bitmap);

    public static native char FT_Bitmap_Get_palette_mode(long bitmap);

    public static native char FT_Bitmap_Get_pixel_mode(long bitmap);

    public static native ByteBuffer FT_Bitmap_Get_buffer(long bitmap);

    // ---- Charmap
    public static native int FT_Get_Charmap_Index(long charmap);

    // ---- Glyph
    // TODO

    /* Java Object functions */

    public static Library newLibrary() {
        long library = FT_Init_FreeType();
        if (library == 0)
            return null;
        return new Library(library);
    }

    /* --------------------- */

    static { // Load library
        try {
            System.loadLibrary("main");
					/*
			if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
				int bits = 86;
				if (System.getProperty("os.arch").contains("64"))
					bits = 64;
				System.loadLibrary("freetype26MT_x" + bits);
			} else
				throw new Exception("Operating system not supported.");
				*/
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Can't find the native file for FreeType-jni.");
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}