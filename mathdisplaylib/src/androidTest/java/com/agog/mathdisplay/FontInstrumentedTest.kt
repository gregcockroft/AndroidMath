package com.agog.mathdisplay;

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.agog.mathdisplay.render.CGGlyph
import com.agog.mathdisplay.render.BoundingBox

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before;
import org.junit.Assert.*


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4::class)
public class FontInstrumentedTest {

    private var context: Context? = null

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getContext();

    }


    /*
     Load the default font and test multiple methods that require parsing and handling of the font including MATH table
     */
    @Test
    fun testFontRead() {
        // Context of the app under test.
        assertNotNull(context)
        MTFontManager.setContext(context!!)

        var font = MTFontManager.defaultFont()
        assertNotNull("testFontRead", font)

        val mathtable = font!!.mathTable
        assertNotNull("font mathTable", mathtable)

        var srcname = "circumflexcmb"
        var gid = font!!.getGlyphWithName(srcname)
        println("gid $gid")
        assertEquals("getGlyphWithName", 2270, gid)

        var name = font!!.getGlyphName(gid)

        println("name " + name)
        assertEquals("getGlyphName", srcname, name)

        gid = 1319 //italic x
        val itcorrect = mathtable.getItalicCorrection(1319)
        println("itcorrect $itcorrect")
        assertEquals("getItalicCorrection", 0.0f, itcorrect, 0.05f)

        val scriptdown = mathtable.scriptScaleDown
        println("scriptdown $scriptdown")
        assertEquals("scriptScaleDown", 0.7f, scriptdown, 0.05f)

        val supbasedrop = mathtable.superscriptBaselineDropMax
        println("supbasedrop $supbasedrop")
        assertEquals("supbasedrop", 5.0f, supbasedrop, 0.05f)

        val lg = mathtable.getLargerGlyph(3060) // summation
        name = font!!.getGlyphName(lg)
        println("lg $lg name $name")
        assertEquals("getLargerGlyph", 3074, lg)
        assertEquals("getLargerGlyph", "summation.v1", name)

        val varray = listOf(9, 2367, 2389, 2411, 2433, 2455, 2477, 2499)

        val variants = mathtable.getVerticalVariantsForGlyph(CGGlyph(9))
        assertEquals("getVerticalVariantsForGlyph", variants, varray)


        val parts = mathtable.getVerticalGlyphAssemblyForGlyph(3077) // radical
        assertNotNull("getVerticalGlyphAssemblyForGlyph", parts)
        name = font!!.getGlyphName(3077)
        println("parts $parts name $name")

        assertEquals("getVerticalGlyphAssemblyForGlyph", 3, parts!!.size)
        assertEquals("getVerticalGlyphAssemblyForGlyph", 3078, parts[0].glyph)
        assertEquals("getVerticalGlyphAssemblyForGlyph", 36.4f, parts[0].fullAdvance, 0.05f)
        assertEquals("getVerticalGlyphAssemblyForGlyph", 0.0f, parts[0].startConnectorLength, 0.05f)
        assertEquals("getVerticalGlyphAssemblyForGlyph", 6.4f, parts[0].endConnectorLength, 0.05f)
        assertEquals("getVerticalGlyphAssemblyForGlyph", false, parts[0].isExtender)
        assertEquals("getVerticalGlyphAssemblyForGlyph", 3079, parts[1].glyph)
        assertEquals("getVerticalGlyphAssemblyForGlyph", true, parts[1].isExtender)
        assertEquals("getVerticalGlyphAssemblyForGlyph", 3080, parts[2].glyph)

        val accentarray = listOf(2270, 2280, 2290, 2300, 2310, 2320, 2330, 2340, 41)
        val srcadvances = arrayOf(0.0f, 12.88f, 15.36f, 18.38f, 22.0f, 26.4f, 31.62f, 37.92f, 15.0f)
        val advances = Array<Float>(9, { i -> 0.0f })
        font!!.mathTable!!.getAdvancesForGlyphs(accentarray, advances, 9)
        println("advances " + advances)
        for ((index, value) in srcadvances.withIndex()) {
            assertEquals("getAdvancesForGlyphs", value, advances[index], 0.02f)
        }


        val bboxes = Array<BoundingBox?>(9, { BoundingBox() })


        val contain = font!!.mathTable.getBoundingRectsForGlyphs(accentarray, bboxes, 9)
        assertNotNull("getBoundingRectsForGlyphs", bboxes[0])
        assertNotNull("getBoundingRectsForGlyphs", bboxes[1])

        assertEquals("getBoundingRectsForGlyphs", -8.92f, bboxes[0]!!.lowerLeftX, 0.02f)
        assertEquals("getBoundingRectsForGlyphs", 11.74f, bboxes[0]!!.lowerLeftY, 0.02f)
        assertEquals("getBoundingRectsForGlyphs", -1.64f, bboxes[0]!!.upperRightX, 0.02f)
        assertEquals("getBoundingRectsForGlyphs", 14.68f, bboxes[0]!!.upperRightY, 0.02f)

        assertEquals("getBoundingRectsForGlyphs", 0.0f, bboxes[1]!!.lowerLeftX, 0.02f)
        assertEquals("getBoundingRectsForGlyphs", 11.44f, bboxes[1]!!.lowerLeftY, 0.02f)
        assertEquals("getBoundingRectsForGlyphs", 12.88f, bboxes[1]!!.upperRightX, 0.02f)
        assertEquals("getBoundingRectsForGlyphs", 14.92f, bboxes[1]!!.upperRightY, 0.02f)

        assertEquals("getBoundingRectsForGlyphs", -8.92f, contain.lowerLeftX, 0.02f)
        assertEquals("getBoundingRectsForGlyphs", 0.0f, contain.lowerLeftY, 0.02f)
        assertEquals("getBoundingRectsForGlyphs", 37.92f, contain.upperRightX, 0.02f)
        assertEquals("getBoundingRectsForGlyphs", 14.98f, contain.upperRightY, 0.02f)

        var f = mathtable?.constantFromTable("FractionRuleThickness")
        assertEquals("default font FractionRuleThickness", 0.8f, f)

        f = mathtable?.percentFromTable("SpaceAfterScript")
        assertEquals("default font SpaceAfterScript", 0.56f, f)

        f = mathtable?.fractionNumeratorGapMin
        assertEquals("default font fractionNumeratorGapMin", 0.8f, f)

        println("done")


    }


}


