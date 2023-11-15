package com.agog.mathdisplay.render

import com.agog.mathdisplay.parse.MTMathAtomType
import com.agog.mathdisplay.parse.MathDisplayException
import com.agog.mathdisplay.render.MTInterElementSpaceType.*

/**
 * Created by greg on 3/13/18.
 */

enum class MTInterElementSpaceType {
    KMTSpaceInvalid,
    KMTSpaceNone,
    KMTSpaceThin,
    KMTSpaceNSThin,    // Thin but not in script mode
    KMTSpaceNSMedium,
    KMTSpaceNSThick
}

val interElementSpaceArray: Array<Array<MTInterElementSpaceType>> = arrayOf(
        //   ordinary             operator             binary               relation            open                 close               punct               // fraction
        arrayOf(KMTSpaceNone, KMTSpaceThin, KMTSpaceNSMedium, KMTSpaceNSThick, KMTSpaceNone, KMTSpaceNone, KMTSpaceNone, KMTSpaceNSThin),    // ordinary
        arrayOf(KMTSpaceThin, KMTSpaceThin, KMTSpaceInvalid, KMTSpaceNSThick, KMTSpaceNone, KMTSpaceNone, KMTSpaceNone, KMTSpaceNSThin),    // operator
        arrayOf(KMTSpaceNSMedium, KMTSpaceNSMedium, KMTSpaceInvalid, KMTSpaceInvalid, KMTSpaceNSMedium, KMTSpaceInvalid, KMTSpaceInvalid, KMTSpaceNSMedium),  // binary
        arrayOf(KMTSpaceNSThick, KMTSpaceNSThick, KMTSpaceInvalid, KMTSpaceNone, KMTSpaceNSThick, KMTSpaceNone, KMTSpaceNone, KMTSpaceNSThick),   // relation
        arrayOf(KMTSpaceNone, KMTSpaceNone, KMTSpaceInvalid, KMTSpaceNone, KMTSpaceNone, KMTSpaceNone, KMTSpaceNone, KMTSpaceNone),      // open
        arrayOf(KMTSpaceNone, KMTSpaceThin, KMTSpaceNSMedium, KMTSpaceNSThick, KMTSpaceNone, KMTSpaceNone, KMTSpaceNone, KMTSpaceNSThin),    // close
        arrayOf(KMTSpaceNSThin, KMTSpaceNSThin, KMTSpaceInvalid, KMTSpaceNSThin, KMTSpaceNSThin, KMTSpaceNSThin, KMTSpaceNSThin, KMTSpaceNSThin),    // punct
        arrayOf(KMTSpaceNSThin, KMTSpaceThin, KMTSpaceNSMedium, KMTSpaceNSThick, KMTSpaceNSThin, KMTSpaceNone, KMTSpaceNSThin, KMTSpaceNSThin),    // fraction
        arrayOf(KMTSpaceNSMedium, KMTSpaceNSThin, KMTSpaceNSMedium, KMTSpaceNSThick, KMTSpaceNone, KMTSpaceNone, KMTSpaceNone, KMTSpaceNSThin)  // radical
)


// Get's the index for the given type. If row is true, the index is for the row (i.e. left element) otherwise it is for the column (right element)
fun getInterElementSpaceArrayIndexForType(type: MTMathAtomType, row: Boolean): Int {
    when (type) {
    // A placeholder is treated as ordinary
        MTMathAtomType.KMTMathAtomColor, MTMathAtomType.KMTMathAtomTextColor, MTMathAtomType.KMTMathAtomOrdinary, MTMathAtomType.KMTMathAtomPlaceholder -> return 0
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
