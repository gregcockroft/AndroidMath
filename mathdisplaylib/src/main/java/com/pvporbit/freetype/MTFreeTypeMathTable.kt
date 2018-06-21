package com.pvporbit.freetype

import java.nio.ByteBuffer

/*
  greg@agog.com

  Read the math table from a opentype font and create tables ready for typesetting math.
  freetype doesn't supply a library call to parse this table.
  The whole table is retrieved as a bytearray and then parsed in kotlin


  Best reference I've found for math table format
  https://docs.microsoft.com/en-us/typography/opentype/spec/math


 */
private val constTable = arrayOf(
        "int16", "ScriptPercentScaleDown",
        "int16", "ScriptScriptPercentScaleDown",
        "uint16", "DelimitedSubFormulaMinHeight",
        "uint16", "DisplayOperatorMinHeight",
        "MathValueRecord", "MathLeading",
        "MathValueRecord", "AxisHeight",
        "MathValueRecord", "AccentBaseHeight",
        "MathValueRecord", "FlattenedAccentBaseHeight",
        "MathValueRecord", "SubscriptShiftDown",
        "MathValueRecord", "SubscriptTopMax",
        "MathValueRecord", "SubscriptBaselineDropMin",
        "MathValueRecord", "SuperscriptShiftUp",
        "MathValueRecord", "SuperscriptShiftUpCramped",
        "MathValueRecord", "SuperscriptBottomMin",
        "MathValueRecord", "SuperscriptBaselineDropMax",
        "MathValueRecord", "SubSuperscriptGapMin",
        "MathValueRecord", "SuperscriptBottomMaxWithSubscript",
        "MathValueRecord", "SpaceAfterScript",
        "MathValueRecord", "UpperLimitGapMin",
        "MathValueRecord", "UpperLimitBaselineRiseMin",
        "MathValueRecord", "LowerLimitGapMin",
        "MathValueRecord", "LowerLimitBaselineDropMin",
        "MathValueRecord", "StackTopShiftUp",
        "MathValueRecord", "StackTopDisplayStyleShiftUp",
        "MathValueRecord", "StackBottomShiftDown",
        "MathValueRecord", "StackBottomDisplayStyleShiftDown",
        "MathValueRecord", "StackGapMin",
        "MathValueRecord", "StackDisplayStyleGapMin",
        "MathValueRecord", "StretchStackTopShiftUp",
        "MathValueRecord", "StretchStackBottomShiftDown",
        "MathValueRecord", "StretchStackGapAboveMin",
        "MathValueRecord", "StretchStackGapBelowMin",
        "MathValueRecord", "FractionNumeratorShiftUp",
        "MathValueRecord", "FractionNumeratorDisplayStyleShiftUp",
        "MathValueRecord", "FractionDenominatorShiftDown",
        "MathValueRecord", "FractionDenominatorDisplayStyleShiftDown",
        "MathValueRecord", "FractionNumeratorGapMin",
        "MathValueRecord", "FractionNumDisplayStyleGapMin",
        "MathValueRecord", "FractionRuleThickness",
        "MathValueRecord", "FractionDenominatorGapMin",
        "MathValueRecord", "FractionDenomDisplayStyleGapMin",
        "MathValueRecord", "SkewedFractionHorizontalGap",
        "MathValueRecord", "SkewedFractionVerticalGap",
        "MathValueRecord", "OverbarVerticalGap",
        "MathValueRecord", "OverbarRuleThickness",
        "MathValueRecord", "OverbarExtraAscender",
        "MathValueRecord", "UnderbarVerticalGap",
        "MathValueRecord", "UnderbarRuleThickness",
        "MathValueRecord", "UnderbarExtraDescender",
        "MathValueRecord", "RadicalVerticalGap",
        "MathValueRecord", "RadicalDisplayStyleVerticalGap",
        "MathValueRecord", "RadicalRuleThickness",
        "MathValueRecord", "RadicalExtraAscender",
        "MathValueRecord", "RadicalKernBeforeDegree",
        "MathValueRecord", "RadicalKernAfterDegree",
        "uint16", "RadicalDegreeBottomRaisePercent"
)

class MTFreeTypeMathTable(val pointer: Long, val data: ByteBuffer) {
    private val constants: HashMap<String, Int> = hashMapOf()
    private val italicscorrectioninfo: HashMap<Int, Int> = hashMapOf()
    private val topaccentattachment: HashMap<Int, Int> = hashMapOf()
    private val vertglyphconstruction: HashMap<Int, MathGlyphConstruction> = hashMapOf()
    private val horizglyphconstruction: HashMap<Int, MathGlyphConstruction> = hashMapOf()
    var minConnectorOverlap: Int = 0

    init {
        val i = data.remaining()
        val success = FreeType.FT_Load_Math_Table(pointer, data, data.remaining())

        if (success) {
            val version = data.int
            if (version == 0x00010000) {
                val mathConstantsOffset = getDataSInt()
                val mathGlyphInfoOffset = getDataSInt()
                val mathVariantsOffset = getDataSInt()
                //println("MathConstants $MathConstants MathGlyphInfo $MathGlyphInfo MathVariants $MathVariants")
                readConstants(mathConstantsOffset)

                // Glyph Info Tabe
                data.position(mathGlyphInfoOffset)
                val mathItalicsCorrectionInfo = getDataSInt()
                val mathTopAccentAttachment = getDataSInt()
                //val extendedShapeCoverage = getDataSInt()

                // This is unused
                //val mathKernInfo = getDataSInt()

                readmatchedtable(mathGlyphInfoOffset + mathItalicsCorrectionInfo, italicscorrectioninfo)
                readmatchedtable(mathGlyphInfoOffset + mathTopAccentAttachment, topaccentattachment)

                readvariants(mathVariantsOffset)
            }
        }


    }

    private fun getDataSInt(): Int {
        val v = data.short
        return v.toInt()
    }

    fun getConstant(name: String): Int {
        return constants[name]!!
    }

    fun getitalicCorrection(gid: Int): Int {
        return if (italicscorrectioninfo[gid] != null) italicscorrectioninfo[gid]!! else 0
    }

    fun gettopAccentAttachment(gid: Int): Int? {
        return topaccentattachment[gid]
    }

    private fun getVariantsForGlyph(construction: HashMap<Int, MathGlyphConstruction>, gid: Int): List<Int> {
        val v = construction[gid]
        if (v == null || v.variants.isEmpty()) return (listOf(gid))
        val vl = mutableListOf<Int>()
        for (variant in v.variants) {
            vl.add(variant.variantGlyph)
        }
        return vl
    }

    fun getVerticalVariantsForGlyph(gid: Int): List<Int> {
        return getVariantsForGlyph(vertglyphconstruction, gid)
    }

    fun getHorizontalVariantsForGlyph(gid: Int): List<Int> {
        return getVariantsForGlyph(horizglyphconstruction, gid)
    }

    fun getVerticalGlyphAssemblyForGlyph(gid: Int): Array<GlyphPartRecord>? {
        val v = vertglyphconstruction[gid]
        if (v?.assembly == null) return (null)

        return (v.assembly.partRecords)
    }


    private fun getDataRecord(): Int {
        val value = getDataSInt()
        @Suppress("UNUSED_VARIABLE")
        val deviceTable = getDataSInt()
        return value
    }


    // Read either a correction or offset table that has a table of glyphs covered that correspond
    // to an array of MathRecords of the values
    private fun readmatchedtable(foffset: Int, table: HashMap<Int, Int>) {
        data.position(foffset)
        val coverageoffset = getDataSInt()

        val coverage = readCoverageTable(foffset + coverageoffset)

        val count = getDataSInt()
        for (i in 0 until count) {
            // indexed by glyphid
            table[coverage[i]] = getDataRecord()
        }

    }


    private fun readConstants(foffset: Int) {
        data.position(foffset)

        var i = 0
        while (i < constTable.size) {
            val recordtype = constTable[i]
            val recordname = constTable[i + 1]
            when (recordtype) {
                "uint16", "int16" -> {
                    val value: Int = getDataSInt()
                    constants[recordname] = value
                }
                else -> {
                    val value: Int = getDataSInt()
                    @Suppress("UNUSED_VARIABLE")
                    val offset: Int = getDataSInt()
                    constants[recordname] = value
                }
            }
            i += 2
        }
    }

    // https://docs.microsoft.com/en-us/typography/opentype/spec/chapter2
    /*
        Read an array of glyph ids
     */
    private fun readCoverageTable(foffset: Int): Array<Int> {
        val currentposition = data.position()
        data.position(foffset)
        val format: Int = getDataSInt()
        val ra: Array<Int>?

        when (format) {
            1 -> {
                val glyphCount: Int = getDataSInt()
                ra = Array(glyphCount, { 0 })
                for (i in 0 until glyphCount) {
                    ra[i] = getDataSInt()
                }
            }
            2 -> {
                val rangeCount: Int = getDataSInt()
                val rr = mutableListOf<Int>()
                for (i in 0 until rangeCount) {
                    val startGlyphID = getDataSInt()
                    val endGlyphID = getDataSInt()
                    var startCoverageIndex = getDataSInt()
                    for (g in startGlyphID..endGlyphID) {
                        rr.add(startCoverageIndex++, g)
                    }
                }
                ra = rr.toTypedArray()
            }
            else -> {
                throw Exception("Invalid coverage format")
            }
        }

        data.position(currentposition)
        return ra
    }

    class MathGlyphConstruction(val assembly: GlyphAssembly?, val variants: Array<MathGlyphVariantRecord>)
    class MathGlyphVariantRecord(val variantGlyph: Int, @Suppress("unused") val advanceMeasurement: Int)
    class GlyphPartRecord(val glyph: Int, val startConnectorLength: Int, val endConnectorLength: Int, val fullAdvance: Int, val partFlags: Int)
    class GlyphAssembly(@Suppress("unused") val italicsCorrection: Int, val partRecords: Array<GlyphPartRecord>)

    private fun readconstruction(foffset: Int): MathGlyphConstruction {
        val currentposition = data.position()
        data.position(foffset)

        val glyphAssemblyOff = getDataSInt()
        val variantCount = getDataSInt()
        val variants = mutableListOf<MathGlyphVariantRecord>()
        for (v in 0 until variantCount) {
            val variantGlyph = getDataSInt()
            val advanceMeasurement = getDataSInt()
            variants.add(v, MathGlyphVariantRecord(variantGlyph, advanceMeasurement))
        }
        val assembly = if (glyphAssemblyOff == 0) null else readassembly(foffset + glyphAssemblyOff)
        val construction = MathGlyphConstruction(assembly, variants.toTypedArray())
        data.position(currentposition)
        return construction
    }

    private fun readassembly(foffset: Int): GlyphAssembly {
        val currentposition = data.position()
        data.position(foffset)

        val italicsCorrection = getDataRecord()
        val partCount = getDataSInt()
        val parts = mutableListOf<GlyphPartRecord>()

        for (p in 0 until partCount) {
            val glyph = getDataSInt()
            val startConnectorLength = getDataSInt()
            val endConnectorLength = getDataSInt()
            val fullAdvance = getDataSInt()
            val partFlags = getDataSInt()
            parts.add(p, GlyphPartRecord(glyph, startConnectorLength, endConnectorLength, fullAdvance, partFlags))
        }
        val assembly = GlyphAssembly(italicsCorrection, parts.toTypedArray())
        data.position(currentposition)
        return assembly
    }


    private fun readvariants(foffset: Int) {
        data.position(foffset)

        this.minConnectorOverlap = getDataSInt()
        val vertGlyphCoverage = getDataSInt()
        val horizGlyphCoverage = getDataSInt()
        val vertGlyphCount = getDataSInt()
        val horizGlyphCount = getDataSInt()

        val vertcoverage = readCoverageTable(foffset + vertGlyphCoverage)
        val horizcoverage = readCoverageTable(foffset + horizGlyphCoverage)

        for (g in 0 until vertGlyphCount) {
            val glyphConstruction = getDataSInt()
            vertglyphconstruction[vertcoverage[g]] = readconstruction(foffset + glyphConstruction)
        }

        for (g in 0 until horizGlyphCount) {
            val glyphConstruction = getDataSInt()
            horizglyphconstruction[horizcoverage[g]] = readconstruction(foffset + glyphConstruction)
        }
    }

}