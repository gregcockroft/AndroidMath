package com.agog.mathdisplay.render

import com.agog.mathdisplay.parse.MathDisplayException
import com.pvporbit.freetype.*
import com.pvporbit.freetype.FreeTypeConstants.FT_LOAD_NO_SCALE
import java.io.IOException
import java.io.InputStream

data class MTGlyphPart(
        var glyph: Int = 0,
        var fullAdvance: Float = 0f,
        var startConnectorLength: Float = 0f,
        var endConnectorLength: Float = 0f,
        var isExtender: Boolean = false
)


class BoundingBox() {
    var lowerLeftX: Float = 0.0f
    var lowerLeftY: Float = 0.0f
    var upperRightX: Float = 0.0f
    var upperRightY: Float = 0.0f

    val width: Float
        get() = this.upperRightX - this.lowerLeftX

    val height: Float
        get() = this.upperRightY - this.lowerLeftY


    constructor(minX: Float, minY: Float, maxX: Float, maxY: Float) : this() {
        this.lowerLeftX = minX
        this.lowerLeftY = minY
        this.upperRightX = maxX
        this.upperRightY = maxY
    }

    constructor(numbers: List<Number>) : this() {
        this.lowerLeftX = numbers[0].toFloat()
        this.lowerLeftY = numbers[1].toFloat()
        this.upperRightX = numbers[2].toFloat()
        this.upperRightY = numbers[3].toFloat()
    }

    fun contains(x: Float, y: Float): Boolean {
        return x >= this.lowerLeftX && x <= this.upperRightX && y >= this.lowerLeftY && y <= this.upperRightY
    }

    override fun toString(): String {
        return "[" + this.lowerLeftX + "," + this.lowerLeftY + "," + this.upperRightX + "," + this.upperRightY + "]"
    }
}

class MTFontMathTable(val font: MTFont, var istreamotf: InputStream?) {
    var unitsPerEm: Int = 1
    var fontSize: Float = 0f
    lateinit var freeface: Face
    lateinit var freeTypeMathTable: MTFreeTypeMathTable

    /*
    lateinit var kConstantsTable: SortedMap<String, NSObject>
    lateinit var kVertVariantsTable: SortedMap<String, NSObject>
    lateinit var kHorizVariantsTable: SortedMap<String, NSObject>
    lateinit var kItalicTable: SortedMap<String, NSObject>
    lateinit var kAccentsTable: SortedMap<String, NSObject>
    lateinit var kVertAssemblyTable: SortedMap<String, NSObject>
    */

    init {
        fontSize = font.fontSize
        var barray: ByteArray? = null

        if (istreamotf != null) {
            istreamotf.use {
                try {
                    barray = it!!.readBytes()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        it?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            /* --- Init FreeType --- */
            /* get singleton */
            val library = FreeType.newLibrary()
                    ?: throw  MathDisplayException("Error initializing FreeType.")

            freeface = library.newFace(barray, 0)
            checkFontSize()
            unitsPerEm = freeface.getUnitsPerEM()


            freeTypeMathTable = freeface.loadMathTable()


            /**
            val kConstants: String = "constants"
            val kVertVariants = "v_variants"
            val kHorizVariants = "h_variants"
            val kItalic = "italic"
            val kAccents = "accents"
            val kVertAssembly = "v_assembly"

            kConstantsTable = getMathTable(kConstants)
            kVertVariantsTable = getMathTable(kVertVariants)
            kHorizVariantsTable = getMathTable(kHorizVariants)
            kItalicTable = getMathTable(kItalic)
            kAccentsTable = getMathTable(kAccents)
            kVertAssemblyTable = getMathTable(kVertAssembly)
             ***/
        }

    }

    fun checkFontSize(): Face {
        freeface.setCharSize(0, (fontSize * 64).toInt(), 0, 0)
        return (freeface)
    }

    // Lightweight copy
    fun copyFontTableWithSize(size: Float): MTFontMathTable {
        val copyTable = MTFontMathTable(font, null)
        copyTable.fontSize = size
        copyTable.unitsPerEm = this.unitsPerEm
        copyTable.freeface = this.freeface
        copyTable.freeTypeMathTable = this.freeTypeMathTable

        return copyTable
    }

    fun getGlyphName(gid: Int): String {
        val g = this.freeface.getGlyphName(gid)
        return g
    }

    fun getGlyphWithName(glyphName: String): Int {
        val g = this.freeface.getGlyphIndexByName(glyphName)
        return g
    }

    fun getGlyphForCodepoint(codepoint: Int): Int {
        val g = this.freeface.getCharIndex(codepoint)
        return g
    }

    fun getAdvancesForGlyphs(glyphs: List<Int>, advances: Array<Float>, count: Int) {
        for (i in 0 until count) {
            if (!freeface.loadGlyph(glyphs[i], FT_LOAD_NO_SCALE)) {
                val gslot = freeface.getGlyphSlot()
                val a = gslot.advance
                if (a != null) {
                    advances[i] = fontUnitsToPt(a.x)
                }
            }
        }
    }

    fun unionBounds(u: BoundingBox, b: BoundingBox) {
        u.lowerLeftX = minOf(u.lowerLeftX, b.lowerLeftX)
        u.lowerLeftY = minOf(u.lowerLeftY, b.lowerLeftY)
        u.upperRightX = maxOf(u.upperRightX, b.upperRightX)
        u.upperRightY = maxOf(u.upperRightY, b.upperRightY)
    }

    //  Good description and picture
    // https://www.freetype.org/freetype2/docs/glyphs/glyphs-3.html

    fun getBoundingRectsForGlyphs(glyphs: List<Int>, boundingRects: Array<BoundingBox?>?, count: Int): BoundingBox {
        val enclosing = BoundingBox()

        for (i in 0 until count) {
            if (!freeface.loadGlyph(glyphs[i], FT_LOAD_NO_SCALE)) {
                val nb = BoundingBox()
                val gslot = freeface.getGlyphSlot()
                val m = gslot.metrics

                val w = fontUnitsToPt(m.getWidth())
                val h = fontUnitsToPt(m.getHeight())
                //val HoriAdvance = fontUnitsToPt(m.getHoriAdvance())
                //val VertAdvance = fontUnitsToPt(m.getVertAdvance())
                val horiBearingX = fontUnitsToPt(m.getHoriBearingX())
                val horiBearingY = fontUnitsToPt(m.getHoriBearingY())
                //val VertBearingX = fontUnitsToPt(m.getVertBearingX())
                //val VertBearingY = fontUnitsToPt(m.getVertBearingY())
                //println("$a $m $w $h $HoriAdvance $VertAdvance $horiBearingX $horiBearingY $VertBearingX $VertBearingY")
                nb.lowerLeftX = horiBearingX
                nb.lowerLeftY = horiBearingY - h
                nb.upperRightX = horiBearingX + w
                nb.upperRightY = horiBearingY
                //println("nb $nb")

                unionBounds(enclosing, nb)
                if (boundingRects != null) {
                    boundingRects[i] = nb
                }
            }

        }
        return enclosing
    }

    private fun fontUnitsToPt(fontUnits: Long): Float {
        return fontUnits * fontSize / unitsPerEm
    }

    private fun fontUnitsToPt(fontUnits: Int): Float {
        return fontUnits * fontSize / unitsPerEm
    }

    fun fontUnitsBox(b: BoundingBox): BoundingBox {
        val rb = BoundingBox()
        rb.lowerLeftX = fontUnitsToPt(b.lowerLeftX.toInt())
        rb.lowerLeftY = fontUnitsToPt(b.lowerLeftY.toInt())
        rb.upperRightX = fontUnitsToPt(b.upperRightX.toInt())
        rb.upperRightY = fontUnitsToPt(b.upperRightY.toInt())
        return rb
    }


    fun muUnit(): Float {
        return fontSize / 18
    }

    fun constantFromTable(constName: String): Float {
        return fontUnitsToPt(freeTypeMathTable.getConstant(constName))
    }


    fun percentFromTable(percentName: String): Float {
        return freeTypeMathTable.getConstant(percentName) / 100.0f
    }

    val fractionNumeratorDisplayStyleShiftUp: Float
        get() = constantFromTable("FractionNumeratorDisplayStyleShiftUp")


    val fractionNumeratorShiftUp: Float
        get() = constantFromTable("FractionNumeratorShiftUp")


    val fractionDenominatorDisplayStyleShiftDown: Float
        get() = constantFromTable("FractionDenominatorDisplayStyleShiftDown")

    val fractionDenominatorShiftDown: Float
        get() = constantFromTable("FractionDenominatorShiftDown")

    val fractionNumeratorDisplayStyleGapMin: Float
        get() = constantFromTable("FractionNumDisplayStyleGapMin")

    val fractionNumeratorGapMin: Float
        get() = constantFromTable("FractionNumeratorGapMin")

    val fractionDenominatorDisplayStyleGapMin: Float
        get() = constantFromTable("FractionDenomDisplayStyleGapMin")


    val fractionDenominatorGapMin: Float
        get() = constantFromTable("FractionDenominatorGapMin")


    val fractionRuleThickness: Float
        get() = constantFromTable("FractionRuleThickness")

    val skewedFractionHorizontalGap: Float
        get() = constantFromTable("SkewedFractionHorizontalGap")

    val skewedFractionVerticalGap: Float
        get() = constantFromTable("SkewedFractionVerticalGap")


    // FractionDelimiterSize and FractionDelimiterDisplayStyleSize are not constants
// specified in the OpenType Math specification. Rather these are proposed LuaTeX extensions
// for the TeX parameters \sigma_20 (delim1) and \sigma_21 (delim2). Since these do not
// exist in the fonts that we have, we use the same approach as LuaTeX and use the fontSize
// to determine these values. The constants used are the same as LuaTeX and KaTeX and match the
// metrics values of the original TeX fonts.
// Note: An alternative approach is to use DelimitedSubFormulaMinHeight for \sigma21 and use a factor
// of 2 to get \sigma 20 as proposed in Vieth paper.
// The XeTeX implementation sets \sigma21 = fontSize and \sigma20 = DelimitedSubFormulaMinHeight which
// will produce smaller delimiters.
// Of all the approaches we've implemented LuaTeX's approach since it mimics LaTeX most accurately.
    val fractionDelimiterSize: Float
        get() = 1.01f * fontSize


    val fractionDelimiterDisplayStyleSize: Float
    // Modified constant from 2.4 to 2.39, it matches KaTeX and looks better.
        get() = 2.39f * fontSize

    // Sub/Superscripts

    val superscriptShiftUp: Float
        get() = constantFromTable("SuperscriptShiftUp")

    val superscriptShiftUpCramped: Float
        get() = constantFromTable("SuperscriptShiftUpCramped")

    val subscriptShiftDown: Float
        get() = constantFromTable("SubscriptShiftDown")

    val superscriptBaselineDropMax: Float
        get() = constantFromTable("SuperscriptBaselineDropMax")

    val subscriptBaselineDropMin: Float
        get() = constantFromTable("SubscriptBaselineDropMin")

    val superscriptBottomMin: Float
        get() = constantFromTable("SuperscriptBottomMin")

    val subscriptTopMax: Float
        get() = constantFromTable("SubscriptTopMax")

    val subSuperscriptGapMin: Float
        get() = constantFromTable("SubSuperscriptGapMin")

    val superscriptBottomMaxWithSubscript: Float
        get() = constantFromTable("SuperscriptBottomMaxWithSubscript")

    val spaceAfterScript: Float
        get() = constantFromTable("SpaceAfterScript")

    val radicalRuleThickness: Float
        get() = constantFromTable("RadicalRuleThickness")

    val radicalExtraAscender: Float
        get() = constantFromTable("RadicalExtraAscender")

    val radicalVerticalGap: Float
        get() = constantFromTable("RadicalVerticalGap")

    val radicalDisplayStyleVerticalGap: Float
        get() = constantFromTable("RadicalDisplayStyleVerticalGap")

    val radicalKernBeforeDegree: Float
        get() = constantFromTable("RadicalKernBeforeDegree")

    val radicalKernAfterDegree: Float
        get() = constantFromTable("RadicalKernAfterDegree")

    val radicalDegreeBottomRaisePercent: Float
        get() = percentFromTable("RadicalDegreeBottomRaisePercent")

    // Limits

    val upperLimitGapMin: Float
        get() = constantFromTable("UpperLimitGapMin")

    val upperLimitBaselineRiseMin: Float
        get() = constantFromTable("UpperLimitBaselineRiseMin")

    val lowerLimitGapMin: Float
        get() = constantFromTable("LowerLimitGapMin")

    val lowerLimitBaselineDropMin: Float
        get() = constantFromTable("LowerLimitBaselineDropMin")

    // not present in OpenType fonts.
    val limitExtraAscenderDescender: Float
        get() = 0.0f

    // Constants

    val axisHeight: Float
        get() = constantFromTable("AxisHeight")

    val scriptScaleDown: Float
        get() = percentFromTable("ScriptPercentScaleDown")

    val scriptScriptScaleDown: Float
        get() = percentFromTable("ScriptScriptPercentScaleDown")

    val mathLeading: Float
        get() = constantFromTable("MathLeading")

    val delimitedSubFormulaMinHeight: Float
        get() = constantFromTable("DelimitedSubFormulaMinHeight")

    // Accents

    val accentBaseHeight: Float
        get() = constantFromTable("AccentBaseHeight")

    val flattenedAccentBaseHeight: Float
        get() = constantFromTable("FlattenedAccentBaseHeight")

    // Large Operators

    val displayOperatorMinHeight: Float
        get() = constantFromTable("DisplayOperatorMinHeight")

    // Over and Underbar

    val overbarExtraAscender: Float
        get() = constantFromTable("OverbarExtraAscender")

    val overbarRuleThickness: Float
        get() = constantFromTable("OverbarRuleThickness")

    val overbarVerticalGap: Float
        get() = constantFromTable("OverbarVerticalGap")

    val underbarExtraDescender: Float
        get() = constantFromTable("UnderbarExtraDescender")

    val underbarRuleThickness: Float
        get() = constantFromTable("UnderbarRuleThickness")

    val underbarVerticalGap: Float
        get() = constantFromTable("UnderbarVerticalGap")

    // Stacks

    val stackBottomDisplayStyleShiftDown: Float
        get() = constantFromTable("StackBottomDisplayStyleShiftDown")

    val stackBottomShiftDown: Float
        get() = constantFromTable("StackBottomShiftDown")

    val stackDisplayStyleGapMin: Float
        get() = constantFromTable("StackDisplayStyleGapMin")

    val stackGapMin: Float
        get() = constantFromTable("StackGapMin")

    val stackTopDisplayStyleShiftUp: Float
        get() = constantFromTable("StackTopDisplayStyleShiftUp")

    val stackTopShiftUp: Float
        get() = constantFromTable("StackTopShiftUp")

    val stretchStackBottomShiftDown: Float
        get() = constantFromTable("StretchStackBottomShiftDown")

    val stretchStackGapAboveMin: Float
        get() = constantFromTable("StretchStackGapAboveMin")

    val stretchStackGapBelowMin: Float
        get() = constantFromTable("StretchStackGapBelowMin")

    val stretchStackTopShiftUp: Float
        get() = constantFromTable("StretchStackTopShiftUp")

    // Variants

    fun getVerticalVariantsForGlyph(glyph: CGGlyph): List<Int> {
        return freeTypeMathTable.getVerticalVariantsForGlyph(glyph.gid)
    }

    fun getHorizontalVariantsForGlyph(glyph: CGGlyph): List<Int> {
        return freeTypeMathTable.getHorizontalVariantsForGlyph(glyph.gid)
    }

    fun getLargerGlyph(glyph: Int): Int {
        val glyphName = this.font.getGlyphName(glyph)
        // Find the first variant with a different name.
        val variantGlyphs = freeTypeMathTable.getVerticalVariantsForGlyph(glyph)
        for (vglyph in variantGlyphs) {
            val vname = this.font.getGlyphName(vglyph)
            if (vname != glyphName) {
                return font.getGlyphWithName(vname)
            }
        }
        // We did not find any variants of this glyph so return it.
        return glyph
    }

    // Italic Correction
    fun getItalicCorrection(gid: Int): Float {
        return fontUnitsToPt(freeTypeMathTable.getitalicCorrection(gid))
    }

    // Top Accent Adjustment
    fun getTopAccentAdjustment(glyph: Int): Float {
        val value = freeTypeMathTable.gettopAccentAttachment(glyph)
        return if (value != null) {
            fontUnitsToPt(value)
        } else {
            // testWideAccent test case covers this

            // If no top accent is defined then it is the center of the advance width.
            val glyphs = arrayOf(glyph)
            val advances = arrayOf(0.0f)

            this.getAdvancesForGlyphs(glyphs.toList(), advances, 1)
            advances[0] / 2
        }
    }

    // Glyph Assembly
    val minConnectorOverlap: Float
        get() = fontUnitsToPt(freeTypeMathTable.minConnectorOverlap)


    fun getVerticalGlyphAssemblyForGlyph(glyph: Int): List<MTGlyphPart>? {
        val assemblyInfo: Array<MTFreeTypeMathTable.GlyphPartRecord>? = freeTypeMathTable.getVerticalGlyphAssemblyForGlyph(glyph)

        if (assemblyInfo == null) {
            // No vertical assembly defined for glyph
            return null
        }

        val rv = mutableListOf<MTGlyphPart>()
        for (pi in assemblyInfo) {
            val part = MTGlyphPart()
            part.fullAdvance = fontUnitsToPt(pi.fullAdvance)
            part.endConnectorLength = fontUnitsToPt(pi.endConnectorLength)
            part.startConnectorLength = fontUnitsToPt(pi.startConnectorLength)
            part.isExtender = pi.partFlags == 1
            part.glyph = pi.glyph
            rv.add(part)
        }
        return rv
    }
}
