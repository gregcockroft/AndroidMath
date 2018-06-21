package com.agog.mathdisplay.render

import com.agog.mathdisplay.parse.MTFontStyle
import com.agog.mathdisplay.parse.MathDisplayException

/**
 * Created by greg on 3/13/18.
 */

/*
   A string is a sequence of characters that could be 1 or 2 in length to represent a unicode charater.
   Given a string return the number of characters compensating
 */
fun numberOfGlyphs(s: String): Int {
    return (s.codePointCount(0, s.length))
}

data class CGGlyph(var gid: Int = 0, var glyphAscent: Float = 0.0f, var glyphDescent: Float = 0.0f, var glyphWidth: Float = 0.0f) {
    val isValid: Boolean
        get() = gid != 0

}

const val kMTUnicodeGreekLowerStart: Char = '\u03B1'
const val kMTUnicodeGreekLowerEnd = '\u03C9'
const val kMTUnicodeGreekCapitalStart = '\u0391'
const val kMTUnicodeGreekCapitalEnd = '\u03A9'

// Note this is not equivalent to ch.isLowerCase() delta is a test case
fun isLowerEn(ch: Char): Boolean {
    return (ch) >= 'a' && (ch) <= 'z'
}

fun isUpperEn(ch: Char): Boolean {
    return (ch) >= 'A' && (ch) <= 'Z'
}

fun isNumber(ch: Char): Boolean {
    return (ch) >= '0' && (ch) <= '9'
}

fun isLowerGreek(ch: Char): Boolean {
    return (ch) >= kMTUnicodeGreekLowerStart && (ch) <= kMTUnicodeGreekLowerEnd
}

fun isCapitalGreek(ch: Char): Boolean {
    return (ch) >= kMTUnicodeGreekCapitalStart && (ch) <= kMTUnicodeGreekCapitalEnd
}


fun greekSymbolOrder(ch: Char): Int {
    // These greek symbols that always appear in unicode in this particular order after the alphabet
    // The symbols are epsilon, vartheta, varkappa, phi, varrho, varpi.
    val greekSymbols: Array<Int> = arrayOf(0x03F5, 0x03D1, 0x03F0, 0x03D5, 0x03F1, 0x03D6)
    return greekSymbols.indexOf(ch.toInt())
}

fun isGREEKSYMBOL(ch: Char): Boolean {
    return (greekSymbolOrder(ch) != -1)
}

class MTCodepointChar(val codepoint: Int) {

    fun toUnicodeString(): String {
        val cs = Character.toChars(codepoint)
        val sb = StringBuffer()
        sb.append(cs)
        val sbs = sb.toString()
        return (sbs)
    }

}


// mathit
const val kMTUnicodePlanksConstant = 0x210e
const val kMTUnicodeMathCapitalItalicStart = 0x1D434
const val kMTUnicodeMathLowerItalicStart = 0x1D44E
const val kMTUnicodeGreekCapitalItalicStart = 0x1D6E2
const val kMTUnicodeGreekLowerItalicStart = 0x1D6FC
const val kMTUnicodeGreekSymbolItalicStart = 0x1D716

fun getItalicized(ch: Char): MTCodepointChar {
    // Special cases for italics
    when {
        ch == 'h' -> {  // italic h (plank's constant)
            return MTCodepointChar(kMTUnicodePlanksConstant)
        }
        isUpperEn(ch) -> {
            return MTCodepointChar(kMTUnicodeMathCapitalItalicStart + (ch - 'A'))
        }
        isLowerEn(ch) -> {
            return MTCodepointChar(kMTUnicodeMathLowerItalicStart + (ch - 'a'))
        }
        isCapitalGreek(ch) -> {
            // Capital Greek characters
            return MTCodepointChar(kMTUnicodeGreekCapitalItalicStart + (ch - kMTUnicodeGreekCapitalStart))
        }
        isLowerGreek(ch) -> {
            // Greek characters
            return MTCodepointChar(kMTUnicodeGreekLowerItalicStart + (ch - kMTUnicodeGreekLowerStart))
        }
        isGREEKSYMBOL(ch) -> {
            return MTCodepointChar(kMTUnicodeGreekSymbolItalicStart + greekSymbolOrder(ch))
        }
    }
    // Note there are no italicized numbers in unicode so we don't support italicizing numbers.
    return MTCodepointChar(ch.toInt())
}

// mathbf
const val kMTUnicodeMathCapitalBoldStart = 0x1D400
const val kMTUnicodeMathLowerBoldStart = 0x1D41A
const val kMTUnicodeGreekCapitalBoldStart = 0x1D6A8
const val kMTUnicodeGreekLowerBoldStart = 0x1D6C2
const val kMTUnicodeGreekSymbolBoldStart = 0x1D6DC
const val kMTUnicodeNumberBoldStart = 0x1D7CE

fun getBold(ch: Char): MTCodepointChar {
    when {
        isUpperEn(ch) -> {
            return MTCodepointChar(kMTUnicodeMathCapitalBoldStart + (ch - 'A'))
        }
        isLowerEn(ch) -> {
            return MTCodepointChar(kMTUnicodeMathLowerBoldStart + (ch - 'a'))
        }
        isCapitalGreek(ch) -> {
            // Capital Greek characters
            return MTCodepointChar(kMTUnicodeGreekCapitalBoldStart + (ch - kMTUnicodeGreekCapitalStart))
        }
        isLowerGreek(ch) -> {
            // Greek characters
            return MTCodepointChar(kMTUnicodeGreekLowerBoldStart + (ch - kMTUnicodeGreekLowerStart))
        }
        isGREEKSYMBOL(ch) -> {
            return MTCodepointChar(kMTUnicodeGreekSymbolBoldStart + greekSymbolOrder(ch))
        }
        isNumber(ch) -> {
            return MTCodepointChar(kMTUnicodeNumberBoldStart + (ch - '0'))
        }
    }
    return MTCodepointChar(ch.toInt())
}

// mathbfit
const val kMTUnicodeMathCapitalBoldItalicStart = 0x1D468
const val kMTUnicodeMathLowerBoldItalicStart = 0x1D482
const val kMTUnicodeGreekCapitalBoldItalicStart = 0x1D71C
const val kMTUnicodeGreekLowerBoldItalicStart = 0x1D736
const val kMTUnicodeGreekSymbolBoldItalicStart = 0x1D750

fun getBoldItalic(ch: Char): MTCodepointChar {
    when {
        isUpperEn(ch) -> {
            return MTCodepointChar(kMTUnicodeMathCapitalBoldItalicStart + (ch - 'A'))
        }
        isLowerEn(ch) -> {
            return MTCodepointChar(kMTUnicodeMathLowerBoldItalicStart + (ch - 'a'))
        }
        isCapitalGreek(ch) -> {
            // Capital Greek characters
            return MTCodepointChar(kMTUnicodeGreekCapitalBoldItalicStart + (ch - kMTUnicodeGreekCapitalStart))
        }
        isLowerGreek(ch) -> {
            // Greek characters
            return MTCodepointChar(kMTUnicodeGreekLowerBoldItalicStart + (ch - kMTUnicodeGreekLowerStart))
        }
        isGREEKSYMBOL(ch) -> {
            return MTCodepointChar(kMTUnicodeGreekSymbolBoldItalicStart + greekSymbolOrder(ch))
        }
        isNumber(ch) -> {
            // No bold italic for numbers so we just bold them.
            return getBold(ch)
        }
    }
    return MTCodepointChar(ch.toInt())
}

// LaTeX default
fun getDefaultStyle(ch: Char): MTCodepointChar {
    when {
        isLowerEn(ch) || isUpperEn(ch) || isLowerGreek(ch) || isGREEKSYMBOL(ch) -> {
            return getItalicized(ch)
        }
        isNumber(ch) || isCapitalGreek(ch) -> {
            return MTCodepointChar(ch.toInt())
        }
        ch == '.' -> {
            // . is treated as a number in our code, but it doesn't change fonts.
            return MTCodepointChar(ch.toInt())
        }
    }
    throw MathDisplayException("Unknown character $ch for default style.")
}

const val kMTUnicodeMathCapitalScriptStart = 0x1D49C
// TODO(kostub): Unused in Latin Modern Math - if another font is used determine if
// this should be applicable.
// static const MTCodepointChar kMTUnicodeMathLowerScriptStart = 0x1D4B6;

// mathcal/mathscr (caligraphic or script)
fun getCaligraphic(ch: Char): MTCodepointChar {
    // Caligraphic has lots of exceptions:
    when (ch) {
        'B' ->
            return MTCodepointChar(0x212C)   // Script B (bernoulli)
        'E' ->
            return MTCodepointChar(0x2130)   // Script E (emf)
        'F' ->
            return MTCodepointChar(0x2131)   // Script F (fourier)
        'H' ->
            return MTCodepointChar(0x210B)   // Script H (hamiltonian)
        'I' ->
            return MTCodepointChar(0x2110)   // Script I
        'L' ->
            return MTCodepointChar(0x2112)   // Script L (laplace)
        'M' ->
            return MTCodepointChar(0x2133)   // Script M (M-matrix)
        'R' ->
            return MTCodepointChar(0x211B)   // Script R (Riemann integral)
        'e' ->
            return MTCodepointChar(0x212F)   // Script e (Natural exponent)
        'g' ->
            return MTCodepointChar(0x210A)   // Script g (real number)
        'o' ->
            return MTCodepointChar(0x2134)   // Script o (order)
    }
    when {
        isUpperEn(ch) -> {
            return MTCodepointChar(kMTUnicodeMathCapitalScriptStart + (ch - 'A'))
        }
        isLowerEn(ch) -> {
            // Latin Modern Math does not have lower case caligraphic characters, so we use
            // the default style instead of showing a ?
            return getDefaultStyle(ch)
        }
    }
    // Caligraphic characters don't exist for greek or numbers, we give them the
    // default treatment.
    return getDefaultStyle(ch)
}

const val kMTUnicodeMathCapitalTTStart = 0x1D670
const val kMTUnicodeMathLowerTTStart = 0x1D68A
const val kMTUnicodeNumberTTStart = 0x1D7F6

// mathtt (monospace)
fun getTypewriter(ch: Char): MTCodepointChar {
    when {
        isUpperEn(ch) -> {
            return MTCodepointChar(kMTUnicodeMathCapitalTTStart + (ch - 'A'))
        }
        isLowerEn(ch) -> {
            return MTCodepointChar(kMTUnicodeMathLowerTTStart + (ch - 'a'))
        }
        isNumber(ch) -> {
            return MTCodepointChar(kMTUnicodeNumberTTStart + (ch - '0'))
        }
        else -> {
            // Monospace characters don't exist for greek, we give them the
            // default treatment.
            return getDefaultStyle(ch)
        }
    }
}

const val kMTUnicodeMathCapitalSansSerifStart = 0x1D5A0
const val kMTUnicodeMathLowerSansSerifStart = 0x1D5BA
const val kMTUnicodeNumberSansSerifStart = 0x1D7E2

// mathsf
fun getSansSerif(ch: Char): MTCodepointChar {
    when {
        isUpperEn(ch) -> {
            return MTCodepointChar(kMTUnicodeMathCapitalSansSerifStart + (ch - 'A'))
        }
        isLowerEn(ch) -> {
            return MTCodepointChar(kMTUnicodeMathLowerSansSerifStart + (ch - 'a'))
        }
        isNumber(ch) -> {
            return MTCodepointChar(kMTUnicodeNumberSansSerifStart + (ch - '0'))
        }
        else -> {
            // Sans-serif characters don't exist for greek, we give them the
            // default treatment.
            return getDefaultStyle(ch)
        }
    }
}

const val kMTUnicodeMathCapitalFrakturStart = 0x1D504
const val kMTUnicodeMathLowerFrakturStart = 0x1D51E

// mathfrak
fun getFraktur(ch: Char): MTCodepointChar {
    // Fraktur has exceptions:
    when (ch) {
        'C' ->
            return MTCodepointChar(0x212D)   // C Fraktur
        'H' ->
            return MTCodepointChar(0x210C)   // Hilbert space
        'I' ->
            return MTCodepointChar(0x2111)   // Imaginary
        'R' ->
            return MTCodepointChar(0x211C)   // Real
        'Z' ->
            return MTCodepointChar(0x2128)   // Z Fraktur
    }
    if (isUpperEn(ch)) {
        return MTCodepointChar(kMTUnicodeMathCapitalFrakturStart + (ch - 'A'))
    } else if (isLowerEn(ch)) {
        return MTCodepointChar(kMTUnicodeMathLowerFrakturStart + (ch - 'a'))
    }
    // Fraktur characters don't exist for greek & numbers, we give them the
    // default treatment.
    return getDefaultStyle(ch)
}

const val kMTUnicodeMathCapitalBlackboardStart = 0x1D538
const val kMTUnicodeMathLowerBlackboardStart = 0x1D552
const val kMTUnicodeNumberBlackboardStart = 0x1D7D8

// mathbb (double struck)
fun getBlackboard(ch: Char): MTCodepointChar {
    // Blackboard has lots of exceptions:
    when (ch) {
        'C' ->
            return MTCodepointChar(0x2102)  // Complex numbers
        'H' ->
            return MTCodepointChar(0x210D)  // Quarternions
        'N' ->
            return MTCodepointChar(0x2115)   // Natural numbers
        'P' ->
            return MTCodepointChar(0x2119)   // Primes
        'Q' ->
            return MTCodepointChar(0x211A)   // Rationals
        'R' ->
            return MTCodepointChar(0x211D)   // Reals
        'Z' ->
            return MTCodepointChar(0x2124)  // Integers
    }
    when {
        isUpperEn(ch) -> {
            return MTCodepointChar(kMTUnicodeMathCapitalBlackboardStart + (ch - 'A'))
        }
        isLowerEn(ch) -> {
            return MTCodepointChar(kMTUnicodeMathLowerBlackboardStart + (ch - 'a'))
        }
        isNumber(ch) -> {
            return MTCodepointChar(kMTUnicodeNumberBlackboardStart + (ch - '0'))
        }
    }
    // Blackboard characters don't exist for greek, we give them the
    // default treatment.
    return getDefaultStyle(ch)
}

fun styleCharacter(ch: Char, fontStyle: MTFontStyle): MTCodepointChar {
    when (fontStyle) {
        MTFontStyle.KMTFontStyleDefault -> {
            return getDefaultStyle(ch)
        }
        MTFontStyle.KMTFontStyleRoman -> {
            return MTCodepointChar(ch.toInt())
        }
        MTFontStyle.KMTFontStyleBold -> {
            return getBold(ch)
        }
        MTFontStyle.KMTFontStyleItalic -> {
            return getItalicized(ch)
        }
        MTFontStyle.KMTFontStyleBoldItalic -> {
            return getBoldItalic(ch)
        }
        MTFontStyle.KMTFontStyleCaligraphic -> {
            return getCaligraphic(ch)
        }
        MTFontStyle.KMTFontStyleTypewriter -> {
            return getTypewriter(ch)
        }
        MTFontStyle.KMTFontStyleSansSerif -> {
            return getSansSerif(ch)
        }
        MTFontStyle.KMTFontStyleFraktur -> {
            return getFraktur(ch)
        }
        MTFontStyle.KMTFontStyleBlackboard -> {
            return getBlackboard(ch)
        }
        else -> {
            throw MathDisplayException("Unknown style $fontStyle for font.")

        }
    }
}

// This can only take single unicode character sequence as input.
// Should never be called with a codepoint that requires 2 escaped characters to represent
fun changeFont(str: String, fontStyle: MTFontStyle): String {
    val ret = StringBuffer()
    val ca = str.toCharArray()
    for (ch in ca) {
        val codepoint = styleCharacter(ch, fontStyle)
        ret.append(codepoint.toUnicodeString())
    }
    return ret.toString()
}
