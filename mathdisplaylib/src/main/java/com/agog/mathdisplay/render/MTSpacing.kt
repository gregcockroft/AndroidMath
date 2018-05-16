package com.agog.mathdisplay.render

import com.agog.mathdisplay.parse.MTMathAtomType
import com.agog.mathdisplay.parse.MathDisplayException
import com.agog.mathdisplay.render.MTInterElementSpaceType.*

/**
 * Created by greg on 3/13/18.
 */

enum class MTInterElementSpaceType {
    kMTSpaceInvalid,
    kMTSpaceNone,
    kMTSpaceThin,
    kMTSpaceNSThin,    // Thin but not in script mode
    kMTSpaceNSMedium,
    kMTSpaceNSThick
}

val interElementSpaceArray: Array<Array<MTInterElementSpaceType>> = arrayOf(
        //   ordinary             operator             binary               relation            open                 close               punct               // fraction
        arrayOf(kMTSpaceNone, kMTSpaceThin, kMTSpaceNSMedium, kMTSpaceNSThick, kMTSpaceNone, kMTSpaceNone, kMTSpaceNone, kMTSpaceNSThin),    // ordinary
        arrayOf(kMTSpaceThin, kMTSpaceThin, kMTSpaceInvalid, kMTSpaceNSThick, kMTSpaceNone, kMTSpaceNone, kMTSpaceNone, kMTSpaceNSThin),    // operator
        arrayOf(kMTSpaceNSMedium, kMTSpaceNSMedium, kMTSpaceInvalid, kMTSpaceInvalid, kMTSpaceNSMedium, kMTSpaceInvalid, kMTSpaceInvalid, kMTSpaceNSMedium),  // binary
        arrayOf(kMTSpaceNSThick, kMTSpaceNSThick, kMTSpaceInvalid, kMTSpaceNone, kMTSpaceNSThick, kMTSpaceNone, kMTSpaceNone, kMTSpaceNSThick),   // relation
        arrayOf(kMTSpaceNone, kMTSpaceNone, kMTSpaceInvalid, kMTSpaceNone, kMTSpaceNone, kMTSpaceNone, kMTSpaceNone, kMTSpaceNone),      // open
        arrayOf(kMTSpaceNone, kMTSpaceThin, kMTSpaceNSMedium, kMTSpaceNSThick, kMTSpaceNone, kMTSpaceNone, kMTSpaceNone, kMTSpaceNSThin),    // close
        arrayOf(kMTSpaceNSThin, kMTSpaceNSThin, kMTSpaceInvalid, kMTSpaceNSThin, kMTSpaceNSThin, kMTSpaceNSThin, kMTSpaceNSThin, kMTSpaceNSThin),    // punct
        arrayOf(kMTSpaceNSThin, kMTSpaceThin, kMTSpaceNSMedium, kMTSpaceNSThick, kMTSpaceNSThin, kMTSpaceNone, kMTSpaceNSThin, kMTSpaceNSThin),    // fraction
        arrayOf(kMTSpaceNSMedium, kMTSpaceNSThin, kMTSpaceNSMedium, kMTSpaceNSThick, kMTSpaceNone, kMTSpaceNone, kMTSpaceNone, kMTSpaceNSThin)  // radical
)


// Get's the index for the given type. If row is true, the index is for the row (i.e. left element) otherwise it is for the column (right element)
fun getInterElementSpaceArrayIndexForType(type: MTMathAtomType, row: Boolean): Int {
    when (type) {
    // A placeholder is treated as ordinary
        MTMathAtomType.KMTMathAtomColor, MTMathAtomType.KMTMathAtomOrdinary, MTMathAtomType.KMTMathAtomPlaceholder -> return 0
        MTMathAtomType.KMTMathAtomLargeOperator -> return 1
        MTMathAtomType.KMTMathAtomBinaryOperator -> return 2
        MTMathAtomType.KMTMathAtomRelation -> return 3
        MTMathAtomType.KMTMathAtomOpen -> return 4
        MTMathAtomType.KMTMathAtomClose -> return 5
        MTMathAtomType.KMTMathAtomPunctuation -> return 6
        MTMathAtomType.KMTMathAtomFraction, MTMathAtomType.KMTMathAtomInner -> return 7
        MTMathAtomType.KMTMathAtomRadical -> {
            if (row) {
                // Radicals have inter element spaces only when on the left side.
                // Note: This is a departure from latex but we don't want \sqrt{4}4 to look weird so we put a space in between.
                // They have the same spacing as ordinary except with ordinary.
                return 8
            } else {
                throw MathDisplayException("Interelement space undefined for radical on the right. Treat radical as ordinary.")
            }
        }

        else -> {
            throw MathDisplayException("Interelement space undefined for type $type")
        }
    }
}
