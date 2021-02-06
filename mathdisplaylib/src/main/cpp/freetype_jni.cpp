#include "com_pvporbit_freetype_FreeType.h"
#include "com_pvporbit_freetype_Utils.h"
#include <sstream>
#include <string>
#include <ft2build.h>
#include FT_FREETYPE_H
#include FT_TRUETYPE_TAGS_H
#include FT_TRUETYPE_TABLES_H

/* Please compile with Release Multithreaded */
/* And do not compile with 32 bits after year 2038 ;) */

/* --- Helper functions --- */
/* fuck up */

JNIEXPORT jobject JNICALL
Java_com_pvporbit_freetype_Utils_newBuffer(JNIEnv *env, jclass obj, jint size) {
    return env->NewDirectByteBuffer((char *) malloc(size), size);
}

JNIEXPORT void JNICALL
Java_com_pvporbit_freetype_Utils_fillBuffer(JNIEnv *env, jclass obj, jbyteArray bytes,
                                            jobject buffer, jint length) {
    unsigned char *dst = (unsigned char *) (buffer ? env->GetDirectBufferAddress(buffer) : 0);
    char *src = (char *) env->GetPrimitiveArrayCritical(bytes, 0);

    memcpy(dst, src, length);

    env->ReleasePrimitiveArrayCritical(bytes, src, 0);
}

JNIEXPORT void JNICALL
Java_com_pvporbit_freetype_Utils_deleteBuffer(JNIEnv *env, jclass obj, jobject buffer) {
    char *b = (char *) (buffer ? env->GetDirectBufferAddress(buffer) : 0);
    free(b);
}


/* --- FreeType functions --- */
JNIEXPORT jobject JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Library_1Version(JNIEnv *env, jclass obj, jlong library) {
    int major, minor, patch;
    FT_Library_Version((FT_Library) library, &major, &minor, &patch);

    jclass cls = env->FindClass("com/pvporbit/freetype/LibraryVersion");
    jmethodID methodID = env->GetMethodID(cls, "<init>", "(III)V");
    jobject a = env->NewObject(cls, methodID, major, minor, patch);
    return a;
}

JNIEXPORT jlong JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Init_1FreeType(JNIEnv *env, jclass obj) {
    FT_Library lib = NULL;
    if (FT_Init_FreeType(&lib))
        return 0;
    return (jlong) lib;
}

JNIEXPORT jboolean JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Done_1FreeType(JNIEnv *env, jclass obj, jlong lib) {
    return FT_Done_FreeType((FT_Library) lib);
}

JNIEXPORT jlong JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1New_1Memory_1Face(JNIEnv *env, jclass obj, jlong lib,
                                                          jobject buffer, jint length,
                                                          jlong faceIndex) {
    char *data = (char *) (buffer ? env->GetDirectBufferAddress(buffer) : 0);
    FT_Face face = NULL;
    if (FT_New_Memory_Face((FT_Library) lib, (const FT_Byte *) data, length, faceIndex, &face))
        return 0;
    else return (jlong) face;
}

JNIEXPORT jboolean JNICALL Java_com_pvporbit_freetype_FreeType_FT_1Load_1Math_1Table
        (JNIEnv *env, jclass obj, jlong face, jobject buffer, jint length) {
    char *data = (char *) (buffer ? env->GetDirectBufferAddress(buffer) : 0);
    FT_ULong t_length = length;
    return (FT_Load_Sfnt_Table((FT_Face) face, TTAG_MATH, 0, (FT_Byte *) data, &t_length));
}


JNIEXPORT jboolean JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Set_1Pixel_1Sizes(JNIEnv *env, jclass obj, jlong face,
                                                          jfloat width, jfloat heigth) {
    return FT_Set_Pixel_Sizes((FT_Face) face, width, heigth);
}

JNIEXPORT jboolean JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Load_1Char(JNIEnv *env, jclass obj, jlong face, jchar c,
                                                   jint flags) {
    return FT_Load_Char((FT_Face) face, c, flags);
}

JNIEXPORT jboolean JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Done_1Face(JNIEnv *env, jclass obj, jlong face) {
    return FT_Done_Face((FT_Face) face);
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1ascender(JNIEnv *env, jclass obj, jlong face) {
    return ((FT_Face) face)->ascender;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1descender(JNIEnv *env, jclass obj, jlong face) {
    return ((FT_Face) face)->descender;
}

JNIEXPORT jlong JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1face_1flags(JNIEnv *env, jclass obj,
                                                               jlong face) {
    return ((FT_Face) face)->face_flags;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1face_1index(JNIEnv *env, jclass obj,
                                                               jlong face) {
    return ((FT_Face) face)->face_index;
}

JNIEXPORT jstring JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1family_1name(JNIEnv *env, jclass obj,
                                                                jlong face) {
    return env->NewStringUTF(((FT_Face) face)->family_name);
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1heigth(JNIEnv *env, jclass obj, jlong face) {
    return ((FT_Face) face)->height;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1max_1advance_1height(JNIEnv *env, jclass obj,
                                                                        jlong face) {
    return ((FT_Face) face)->max_advance_height;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1max_1advance_1width(JNIEnv *env, jclass obj,
                                                                       jlong face) {
    return ((FT_Face) face)->max_advance_width;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1num_1faces(JNIEnv *env, jclass obj, jlong face) {
    return ((FT_Face) face)->num_faces;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1num_1glyphs(JNIEnv *env, jclass obj,
                                                               jlong face) {
    return ((FT_Face) face)->num_glyphs;
}

JNIEXPORT jlong JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1style_1flags(JNIEnv *env, jclass obj,
                                                                jlong face) {
    return ((FT_Face) face)->style_flags;
}

JNIEXPORT jstring JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1style_1name(JNIEnv *env, jclass obj,
                                                               jlong face) {
    return env->NewStringUTF(((FT_Face) face)->style_name);
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1underline_1position(JNIEnv *env, jclass obj,
                                                                       jlong face) {
    return ((FT_Face) face)->underline_position;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1underline_1thickness(JNIEnv *env, jclass obj,
                                                                        jlong face) {
    return ((FT_Face) face)->underline_thickness;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1units_1per_1EM(JNIEnv *env, jclass obj,
                                                                  jlong face) {
    return ((FT_Face) face)->units_per_EM;
}

JNIEXPORT jlong JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1size(JNIEnv *env, jclass obj, jlong face) {
    return (jlong) (((FT_Face) face)->size);
}

JNIEXPORT jlong JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Size_1Get_1metrics(JNIEnv *env, jclass obj, jlong size) {
    return (jlong) &(((FT_Size) size)->metrics);
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Size_1Metrics_1Get_1ascender(JNIEnv *env, jclass obj,
                                                                     jlong sizeMetrics) {
    return ((FT_Size_Metrics *) sizeMetrics)->ascender;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Size_1Metrics_1Get_1descender(JNIEnv *env, jclass obj,
                                                                      jlong sizeMetrics) {
    return ((FT_Size_Metrics *) sizeMetrics)->descender;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Size_1Metrics_1Get_1height(JNIEnv *env, jclass obj,
                                                                   jlong sizeMetrics) {
    return ((FT_Size_Metrics *) sizeMetrics)->height;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Size_1Metrics_1Get_1max_1advance(JNIEnv *env, jclass obj,
                                                                         jlong sizeMetrics) {
    return ((FT_Size_Metrics *) sizeMetrics)->max_advance;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Size_1Metrics_1Get_1x_1ppem(JNIEnv *env, jclass obj,
                                                                    jlong sizeMetrics) {
    return ((FT_Size_Metrics *) sizeMetrics)->x_ppem;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Size_1Metrics_1Get_1x_1scale(JNIEnv *env, jclass obj,
                                                                     jlong sizeMetrics) {
    return ((FT_Size_Metrics *) sizeMetrics)->x_scale;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Size_1Metrics_1Get_1y_1ppem(JNIEnv *env, jclass obj,
                                                                    jlong sizeMetrics) {
    return ((FT_Size_Metrics *) sizeMetrics)->y_ppem;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Size_1Metrics_1Get_1y_1scale(JNIEnv *env, jclass obj,
                                                                     jlong sizeMetrics) {
    return ((FT_Size_Metrics *) sizeMetrics)->y_scale;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Get_1Char_1Index(JNIEnv *env, jclass obj, jlong face,
                                                         jint code) {
    return FT_Get_Char_Index((FT_Face) face, code);
}

JNIEXPORT jboolean JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1HAS_1KERNING(JNIEnv *env, jclass obj, jlong face) {
    return FT_HAS_KERNING(((FT_Face) face));
}

JNIEXPORT jboolean JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Select_1Size(JNIEnv *env, jclass obj, jlong face,
                                                     jint strikeIndex) {
    return FT_Select_Size((FT_Face) face, strikeIndex);
}

JNIEXPORT jboolean JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Set_1Char_1Size(JNIEnv *env, jclass obj, jlong face,
                                                        jint char_width, jint char_height,
                                                        jint horz_resolution,
                                                        jint vert_resolution) {
    return FT_Set_Char_Size((FT_Face) face, char_width, char_height, horz_resolution,
                            vert_resolution);
}

JNIEXPORT jboolean JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Load_1Glyph(JNIEnv *env, jclass obj, jlong face,
                                                    jint glyphIndex, jint loadFlags) {
    return FT_Load_Glyph((FT_Face) face, glyphIndex, loadFlags);
}
/*
JNIEXPORT jlongArray JNICALL Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1Kerning(JNIEnv *env, jclass org, jlong face, jchar left, jchar right, jint mode) {
	FT_Vector vector;
	if (FT_Get_Kerning((FT_Face)face, left, right, mode, &vector))
		return 0;

	jlongArray result = env->NewLongArray(2);
	if (result == NULL) // Out of memory
		return NULL;
	jlong fill[2];
	fill[0] = vector.x;
	fill[1] = vector.y;
	env->SetLongArrayRegion(result, 0, 2, fill);

	return result;
}
JNIEXPORT jlong JNICALL Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1KerningX(JNIEnv *env, jclass org, jlong face, jchar left, jchar right, jint mode) {
	FT_Vector vector;
	if (FT_Get_Kerning((FT_Face)face, left, right, mode, &vector))
		return 0;
	return vector.x;
}
JNIEXPORT jlong JNICALL Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1KerningY(JNIEnv *env, jclass org, jlong face, jchar left, jchar right, jint mode) {
	FT_Vector vector;
	if (FT_Get_Kerning((FT_Face)face, left, right, mode, &vector))
		return 0;
	return vector.y;
}
*/
JNIEXPORT jlong JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1glyph(JNIEnv *env, jclass obj, jlong face) {
    return (jlong) ((FT_Face) face)->glyph;
}

JNIEXPORT jlong JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1GlyphSlot_1Get_1linearHoriAdvance(JNIEnv *env, jclass obj,
                                                                          jlong glyph) {
    return ((FT_GlyphSlot) glyph)->linearHoriAdvance;
}

JNIEXPORT jlong JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1GlyphSlot_1Get_1linearVertAdvance(JNIEnv *env, jclass obj,
                                                                          jlong glyph) {
    return ((FT_GlyphSlot) glyph)->linearVertAdvance;
}

JNIEXPORT jobject JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1GlyphSlot_1Get_1advance(JNIEnv *env, jclass obj,
                                                                jlong glyph) {
    FT_Vector vector = ((FT_GlyphSlot) glyph)->advance;

    jclass cls = env->FindClass("com/pvporbit/freetype/GlyphSlot$Advance");
    jmethodID methodID = env->GetMethodID(cls, "<init>", "(JJ)V");
    jobject a = env->NewObject(cls, methodID, (jlong) vector.x, (jlong) vector.y);
    return a;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1GlyphSlot_1Get_1format(JNIEnv *env, jclass obj,
                                                               jlong glyph) {
    return ((FT_GlyphSlot) glyph)->format;
}

JNIEXPORT jlong JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1GlyphSlot_1Get_1bitmap(JNIEnv *env, jclass obj,
                                                               jlong glyph) {
    return (jlong) (&((FT_GlyphSlot) glyph)->bitmap);
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1GlyphSlot_1Get_1bitmap_1left(JNIEnv *env, jclass obj,
                                                                     jlong glyph) {
    return ((FT_GlyphSlot) glyph)->bitmap_left;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1GlyphSlot_1Get_1bitmap_1top(JNIEnv *env, jclass obj,
                                                                    jlong glyph) {
    return ((FT_GlyphSlot) glyph)->bitmap_top;
}

JNIEXPORT jboolean JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Render_1Glyph(JNIEnv *env, jclass obj, jlong glyph,
                                                      jint renderMode) {
    return FT_Render_Glyph((FT_GlyphSlot) glyph, (FT_Render_Mode) renderMode);
}

JNIEXPORT jlong JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1GlyphSlot_1Get_1metrics(JNIEnv *env, jclass obj,
                                                                jlong glyph) {
    return (jlong) &((FT_GlyphSlot) glyph)->metrics;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Bitmap_1Get_1width(JNIEnv *env, jclass obj, jlong bitmap) {
    return ((FT_Bitmap *) bitmap)->width;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Bitmap_1Get_1rows(JNIEnv *env, jclass obj, jlong bitmap) {
    return ((FT_Bitmap *) bitmap)->rows;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Bitmap_1Get_1pitch(JNIEnv *env, jclass obj, jlong bitmap) {
    return ((FT_Bitmap *) bitmap)->pitch;
}

JNIEXPORT jshort JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Bitmap_1Get_1num_1grays(JNIEnv *env, jclass obj,
                                                                jlong bitmap) {
    return ((FT_Bitmap *) bitmap)->num_grays;
}

JNIEXPORT jchar JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Bitmap_1Get_1palette_1mode(JNIEnv *env, jclass obj,
                                                                   jlong bitmap) {
    return ((FT_Bitmap *) bitmap)->palette_mode;
}

JNIEXPORT jchar JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Bitmap_1Get_1pixel_1mode(JNIEnv *env, jclass obj,
                                                                 jlong bitmap) {
    return ((FT_Bitmap *) bitmap)->pixel_mode;
}

JNIEXPORT jobject JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Bitmap_1Get_1buffer(JNIEnv *env, jclass obj, jlong bitmap) {
    FT_Bitmap *bmp = (FT_Bitmap *) bitmap;
    return env->NewDirectByteBuffer((void *) bmp->buffer, bmp->rows * bmp->width * abs(bmp->pitch));
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Glyph_1Metrics_1Get_1width(JNIEnv *env, jclass obj,
                                                                   jlong glyphMetrics) {
    return ((FT_Glyph_Metrics *) glyphMetrics)->width;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Glyph_1Metrics_1Get_1height(JNIEnv *env, jclass obj,
                                                                    jlong glyphMetrics) {
    return ((FT_Glyph_Metrics *) glyphMetrics)->height;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Glyph_1Metrics_1Get_1horiAdvance(JNIEnv *env, jclass obj,
                                                                         jlong glyphMetrics) {
    return ((FT_Glyph_Metrics *) glyphMetrics)->horiAdvance;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Glyph_1Metrics_1Get_1vertAdvance(JNIEnv *env, jclass obj,
                                                                         jlong glyphMetrics) {
    return ((FT_Glyph_Metrics *) glyphMetrics)->vertAdvance;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Glyph_1Metrics_1Get_1horiBearingX(JNIEnv *env, jclass obj,
                                                                          jlong glyphMetrics) {
    return ((FT_Glyph_Metrics *) glyphMetrics)->horiBearingX;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Glyph_1Metrics_1Get_1horiBearingY(JNIEnv *env, jclass obj,
                                                                          jlong glyphMetrics) {
    return ((FT_Glyph_Metrics *) glyphMetrics)->horiBearingY;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Glyph_1Metrics_1Get_1vertBearingX(JNIEnv *env, jclass obj,
                                                                          jlong glyphMetrics) {
    return ((FT_Glyph_Metrics *) glyphMetrics)->vertBearingX;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Glyph_1Metrics_1Get_1vertBearingY(JNIEnv *env, jclass obj,
                                                                          jlong glyphMetrics) {
    return ((FT_Glyph_Metrics *) glyphMetrics)->vertBearingY;
}

JNIEXPORT jboolean JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1CheckTrueTypePatents(JNIEnv *env, jclass obj,
                                                                   jlong face) {
    return FT_Face_CheckTrueTypePatents((FT_Face) face);
}

JNIEXPORT jboolean JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1SetUnpatentedHinting(JNIEnv *env, jclass obj,
                                                                   jlong face, jboolean value) {
    return FT_Face_SetUnpatentedHinting((FT_Face) face, value);
}

JNIEXPORT jboolean JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Reference_1Face(JNIEnv *env, jclass obj, jlong face) {
    return FT_Reference_Face((FT_Face) face);
}

JNIEXPORT jboolean JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Request_1Size(JNIEnv *env, jclass obj, jlong face,
                                                      jobject sizeRequest) {
    jclass sizeRequestClass = env->GetObjectClass(sizeRequest);

    FT_Size_RequestRec req = {};
    req.height = env->GetLongField(sizeRequest, env->GetFieldID(sizeRequestClass, "height", "J"));
    req.width = env->GetLongField(sizeRequest, env->GetFieldID(sizeRequestClass, "width", "J"));
    req.horiResolution = env->GetIntField(sizeRequest,
                                          env->GetFieldID(sizeRequestClass, "horiResolution", "I"));
    req.vertResolution = env->GetIntField(sizeRequest,
                                          env->GetFieldID(sizeRequestClass, "vertResolution", "I"));
    req.type = (FT_Size_Request_Type) (env->GetIntField(sizeRequest,
                                                        env->GetFieldID(sizeRequestClass, "type",
                                                                        "I")));

    return FT_Request_Size((FT_Face) face, (FT_Size_Request) &req);
}

JNIEXPORT jintArray JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Get_1First_1Char(JNIEnv *env, jclass obj, jlong face) {
    FT_ULong charcode;
    FT_UInt gindex;
    charcode = FT_Get_First_Char((FT_Face) face, &gindex);

    jintArray result = env->NewIntArray(2);
    if (result == NULL) // Out of memory
        return NULL;
    jint fill[2];
    fill[0] = charcode;
    fill[1] = gindex;
    env->SetIntArrayRegion(result, 0, 2, fill);

    return result;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Get_1Next_1Char(JNIEnv *env, jclass obj, jlong face,
                                                        jlong charcode) {
    FT_UInt gindex;
    FT_Get_Next_Char((FT_Face) face, charcode, &gindex);
    return gindex;
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Get_1Name_1Index(JNIEnv *env, jclass obj, jlong face,
                                                         jstring name) {
    const char *nameNative = env->GetStringUTFChars(name, 0);
    jint glyphIndex = FT_Get_Name_Index((FT_Face) face, (char *) nameNative);
    env->ReleaseStringUTFChars(name, nameNative);
    return glyphIndex;
}

JNIEXPORT jlong JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Get_1Track_1Kerning(JNIEnv *env, jclass obj, jlong face,
                                                            jlong point_size, jint degree) {
    long kern;
    if (FT_Get_Track_Kerning((FT_Face) face, point_size, degree, &kern) != 0)
        return 0;
    return kern;
}

JNIEXPORT jstring JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Get_1Glyph_1Name(JNIEnv *env, jclass obj, jlong face,
                                                         jint glyphIndex) {
    char *name[100];
    FT_Get_Glyph_Name((FT_Face) face, glyphIndex, name, 100);
    return env->NewStringUTF((const char *) name);
}

JNIEXPORT jstring JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Get_1Postscript_1Name(JNIEnv *env, jclass obj, jlong face) {
    return env->NewStringUTF(FT_Get_Postscript_Name((FT_Face) face));
}

JNIEXPORT jboolean JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Select_1Charmap(JNIEnv *env, jclass obj, jlong face,
                                                        jint encoding) {
    return FT_Select_Charmap((FT_Face) face, (FT_Encoding) encoding);
}

JNIEXPORT jboolean JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Set_1Charmap(JNIEnv *env, jclass obj, jlong face,
                                                     jlong charmap) {
    return FT_Set_Charmap((FT_Face) face, (FT_CharMap) charmap);
}

JNIEXPORT jint JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Get_1Charmap_1Index(JNIEnv *env, jclass obj,
                                                            jlong charmap) {
    return FT_Get_Charmap_Index((FT_CharMap) charmap);
}

JNIEXPORT jshort JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Get_1FSType_1Flags(JNIEnv *env, jclass obj, jlong face) {
    return FT_Get_FSType_Flags((FT_Face) face);
}

JNIEXPORT jobject JNICALL
Java_com_pvporbit_freetype_FreeType_FT_1Face_1Get_1Kerning(JNIEnv *env, jclass obj, jlong face,
                                                           jchar left, jchar right, jint mode) {
    FT_Vector vector;
    int x = 0;
    int y = 0;
    if (!FT_Get_Kerning((FT_Face) face, left, right, mode, &vector)) {
        x = vector.x;
        y = vector.y;
    }

    jclass cls = env->FindClass("com/pvporbit/freetype/Kerning");
    jmethodID methodID = env->GetMethodID(cls, "<init>", "(II)V");
    jobject a = env->NewObject(cls, methodID, x, y);
    return a;
}