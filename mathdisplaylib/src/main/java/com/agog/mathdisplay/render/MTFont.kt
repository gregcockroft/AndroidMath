package com.agog.mathdisplay.render

import android.content.Context
import android.content.res.AssetManager
import com.agog.mathdisplay.parse.MathDisplayException
import android.util.Log

public fun PackageWarning(str: String) {
    Log.w("com.agog.mathdisplay.render", str)
}


class MTFont(private val assets: AssetManager, val name: String, val fontSize: Float, isCopy: Boolean = false) {
    var mathTable: MTFontMathTable = MTFontMathTable(this, null)

    init {
        val fontpath = "fonts/$name.otf"


        if (!isCopy) {
            val istreamotf = assets.open(fontpath)
                    ?: throw MathDisplayException("Missing font asset for $name")
            mathTable = MTFontMathTable(this, istreamotf)
            istreamotf.close()
        }
    }


    fun findGlyphForCharacterAtIndex(index: Int, str: String): CGGlyph {
        // Do we need to check with our font to see if this glyph is in the font?
        val codepoint = Character.codePointAt(str.toCharArray(), index)
        val gid = mathTable.getGlyphForCodepoint(codepoint)
        return CGGlyph(gid)
    }

    fun getGidListForString(str: String): List<Int> {
        val ca = str.toCharArray()
        val ret = MutableList(0, { 0 })

        var i = 0
        while (i < ca.size) {
            val codepoint = Character.codePointAt(ca, i)
            i += Character.charCount(codepoint)
            val gid = mathTable.getGlyphForCodepoint(codepoint)
            if (gid == 0) {
                PackageWarning("getGidListForString codepoint $codepoint mapped to missing glyph")
            }
            ret.add(gid)
        }
        return ret
    }


    fun copyFontWithSize(size: Float): MTFont {
        val copyFont = MTFont(this.assets, this.name, size, true)
        copyFont.mathTable = this.mathTable.copyFontTableWithSize(size)
        return copyFont
    }


    fun getGlyphName(gid: Int): String {
        return mathTable.getGlyphName(gid)
    }

    fun getGlyphWithName(glyphName: String): Int {
        return mathTable.getGlyphWithName(glyphName)
    }

}