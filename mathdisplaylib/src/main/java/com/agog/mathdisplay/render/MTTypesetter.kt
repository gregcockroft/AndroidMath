package com.agog.mathdisplay.render

import android.graphics.Color
import com.agog.mathdisplay.parse.*
import com.agog.mathdisplay.parse.MTMathAtomType.*
import com.agog.mathdisplay.parse.MTLineStyle.*
import com.agog.mathdisplay.parse.MTColumnAlignment.*

import com.agog.mathdisplay.render.MTInterElementSpaceType.*



// Delimiter shortfall from plain.tex
const val kDelimiterFactor = 901
const val kDelimiterShortfallPoints = 5


const val kBaseLineSkipMultiplier = 1.2f  // default base line stretch is 12 pt for 10pt font.
const val kLineSkipMultiplier = 0.1f  // default is 1pt for 10pt font.
const val kLineSkipLimitMultiplier = 0.0f
const val kJotMultiplier = 0.3f // A jot is 3pt for a 10pt font.


class MTTypesetter(val font: MTFont, linestyle: MTLineStyle, var cramped: Boolean = false, var spaced: Boolean = false) {
    var displayAtoms: MutableList<MTDisplay> = mutableListOf()
    val currentPosition: CGPoint = CGPoint(0f, 0f)
    var currentLine: String = ""
    var currentAtoms: MutableList<MTMathAtom> = mutableListOf()    // List of atoms that make the line
    var currentLineIndexRange: NSRange = NSRange()
    var styleFont: MTFont = font

    var style: MTLineStyle = KMTLineStyleDisplay
        set(value) {
            field = value
            this.styleFont = this.font.copyFontWithSize(getStyleSize(value, font))
        }

    init {
        this.style = linestyle
    }

    companion object {

        fun createLineForMathList(mathList: MTMathList, font: MTFont, style: MTLineStyle): MTMathListDisplay {
            val finalizedList = mathList.finalized()
            // default is not cramped
            return createLineForMathList(finalizedList, font, style, false)
        }

        fun createLineForMathList(mathList: MTMathList, font: MTFont, style: MTLineStyle, cramped: Boolean): MTMathListDisplay {
            return createLineForMathList(mathList, font, style, cramped, false)
        }

        private fun createLineForMathList(mathList: MTMathList, font: MTFont, style: MTLineStyle, cramped: Boolean, spaced: Boolean): MTMathListDisplay {
            val preprocessedAtoms = preprocessMathList(mathList)
            val typesetter = MTTypesetter(font, style, cramped, spaced)
            typesetter.createDisplayAtoms(preprocessedAtoms)
            val lastAtom = mathList.atoms.lastOrNull()
            val maxrange = lastAtom?.indexRange?.maxrange ?: 0
            val line = MTMathListDisplay(typesetter.displayAtoms, NSRange(0, maxrange))
            return line
        }

        fun preprocessMathList(ml: MTMathList): MutableList<MTMathAtom> {
            // Note: Some of the preprocessing described by the TeX algorithm is done in the finalize method of MTMathList.
            // Specifically rules 5 & 6 in Appendix G are handled by finalize.
            // This function does not do a complete preprocessing as specified by TeX either. It removes any special atom types
            // that are not included in TeX and applies Rule 14 to merge ordinary characters.
            val preprocessed = mutableListOf<MTMathAtom>()
            var prevNode: MTMathAtom? = null
            for (atom in ml.atoms) {
                if (atom.type == KMTMathAtomVariable || atom.type == KMTMathAtomNumber) {
                    // These are not a TeX type nodes. TeX does this during parsing the input.
                    // switch to using the font specified in the atom
                    val newFont: String = changeFont(atom.nucleus, atom.fontStyle)
                    // We convert it to ordinary
                    atom.type = KMTMathAtomOrdinary
                    atom.nucleus = newFont
                } else if (atom.type == KMTMathAtomUnaryOperator) {
                    // TeX treats these as Ordinary. So will we.
                    atom.type = KMTMathAtomOrdinary
                }

                if (atom.type == KMTMathAtomOrdinary) {
                    // This is Rule 14 to merge ordinary characters.
                    // combine ordinary atoms together
                    if (prevNode != null && prevNode.type == KMTMathAtomOrdinary && prevNode.subScript == null && prevNode.superScript == null) {
                        prevNode.fuse(atom)
                        // skip the current node, we are done here.
                        continue
                    }
                }

                // greg leftover todo from iOS code
                // TODO: add italic correction here or in second pass?
                prevNode = atom
                preprocessed.add(atom)
            }
            return preprocessed
        }
    }

    // returns the size of the font in this style
    private fun getStyleSize(style: MTLineStyle, font: MTFont): Float {
        val original = font.fontSize
        return when (style) {
            KMTLineStyleDisplay, KMTLineStyleText -> original

            KMTLineStyleScript -> original * font.mathTable.scriptScaleDown

            KMTLineStyleScriptScript -> original * font.mathTable.scriptScriptScaleDown
        }
    }


    private fun addInterElementSpace(prevNode: MTMathAtom?, currentType: MTMathAtomType) {
        var interElementSpace = 0.0f
        if (prevNode != null) {
            interElementSpace = this.getInterElementSpace(prevNode.type, currentType)
        } else if (spaced) {
            // For the first atom of a spaced list, treat it as if it is preceded by an open.
            interElementSpace = this.getInterElementSpace(KMTMathAtomOpen, currentType)
        }
        currentPosition.x += interElementSpace
    }

    private fun createDisplayAtoms(preprocessed: List<MTMathAtom>) {
        // items should contain all the nodes that need to be layed out.
        // convert to a list of MTDisplayAtoms
        var prevNode: MTMathAtom? = null
        var lastType: MTMathAtomType = KMTMathAtomNone
        outerloop@ for (atom in preprocessed) {
            when (atom.type) {
                KMTMathAtomNone -> {
                }
                KMTMathAtomNumber, KMTMathAtomVariable, KMTMathAtomUnaryOperator -> throw MathDisplayException("These types should never show here as they are removed by preprocessing")

                KMTMathAtomBoundary -> throw MathDisplayException("A boundary atom should never be inside a mathlist.")

                KMTMathAtomSpace -> {
                    // stash the existing layout
                    if (currentLine.isNotEmpty()) {
                        this.addDisplayLine()
                    }
                    val space = atom as MTMathSpace
                    // add the desired space
                    currentPosition.x += space.space * styleFont.mathTable.muUnit()
                    // Since this is extra space, the desired interelement space between the prevAtom
                    // and the next node is still preserved. To avoid resetting the prevAtom and lastType
                    // we skip to the next node.
                    continue@outerloop
                }

                KMTMathAtomStyle -> {
                    // stash the existing layout
                    if (currentLine.isNotEmpty()) {
                        this.addDisplayLine()
                    }
                    val style = atom as MTMathStyle
                    this.style = style.style
                    // We need to preserve the prevNode for any interelement space changes.
                    // so we skip to the next node.
                    continue@outerloop
                }

                KMTMathAtomColor -> {
                    // stash the existing layout
                    if (currentLine.isNotEmpty()) {
                        this.addDisplayLine()
                    }
                    val colorAtom = atom as MTMathColor
                    if (colorAtom.innerList != null) {
                        val display = createLineForMathList(colorAtom.innerList!!, font, style)
                        display.localTextColor = Color.parseColor(colorAtom.colorString)
                        display.position = currentPosition
                        currentPosition.x += display.width
                        displayAtoms.add(display)
                    }
                }

                KMTMathAtomRadical -> {
                    // stash the existing layout
                    if (currentLine.isNotEmpty()) {
                        this.addDisplayLine()
                    }
                    val rad = atom as MTRadical
                    // Radicals are considered as Ord in rule 16.
                    this.addInterElementSpace(prevNode, KMTMathAtomOrdinary)
                    val displayRad: MTRadicalDisplay = this.makeRadical(rad.radicand!!, rad.indexRange)
                    if (rad.degree != null) {
                        // add the degree to the radical
                        val degree = createLineForMathList(rad.degree!!, this.font, KMTLineStyleScriptScript)
                        displayRad.setDegree(degree, this.styleFont.mathTable)
                    }
                    this.displayAtoms.add(displayRad)
                    currentPosition.x += displayRad.width

                    // add super scripts || subscripts
                    if (atom.subScript != null || atom.superScript != null) {
                        this.makeScripts(atom, displayRad, rad.indexRange.location, 0.0f)
                    }
                    // change type to ordinary  greg this was commented out in ios code
                    //atom.type = KMTMathAtomOrdinary
                }

                KMTMathAtomFraction -> {
                    // stash the existing layout
                    if (currentLine.isNotEmpty()) {
                        this.addDisplayLine()
                    }
                    val frac = atom as MTFraction
                    this.addInterElementSpace(prevNode, atom.type)
                    val displayFrac = this.makeFraction(frac)
                    displayAtoms.add(displayFrac)
                    currentPosition.x += displayFrac.width
                    // add super scripts || subscripts
                    if (atom.subScript != null || atom.superScript != null) {
                        this.makeScripts(atom, displayFrac, frac.indexRange.location, 0.0f)
                    }
                }

                KMTMathAtomLargeOperator -> {
                    // stash the existing layout
                    if (currentLine.isNotEmpty()) {
                        this.addDisplayLine()
                    }
                    this.addInterElementSpace(prevNode, atom.type)
                    val op = atom as MTLargeOperator
                    val displayOp = this.makeLargeOp(op)
                    displayAtoms.add(displayOp)
                }

                KMTMathAtomInner -> {
                    // stash the existing layout
                    if (currentLine.isNotEmpty()) {
                        this.addDisplayLine()
                    }
                    this.addInterElementSpace(prevNode, atom.type)
                    val inner = atom as MTInner
                    var displayInner: MTDisplay? = null
                    if (inner.leftBoundary != null || inner.rightBoundary != null) {
                        displayInner = this.makeLeftRight(inner)
                    } else {
                        if (inner.innerList != null) {
                            displayInner = createLineForMathList(inner.innerList!!, font, style, cramped)
                        }
                    }
                    if (displayInner != null) {
                        displayInner.position = currentPosition
                        currentPosition.x += displayInner.width
                        displayAtoms.add(displayInner)
                        // add super scripts || subscripts
                        if (atom.subScript != null || atom.superScript != null) {
                            this.makeScripts(atom, displayInner, inner.indexRange.location, 0.0f)
                        }
                    }
                }

                KMTMathAtomUnderline -> {
                    // stash the existing layout
                    if (currentLine.isNotEmpty()) {
                        this.addDisplayLine()
                    }
                    // Underline is considered as Ord in rule 16.
                    this.addInterElementSpace(prevNode, KMTMathAtomOrdinary)
                    atom.type = KMTMathAtomOrdinary

                    val under = atom as MTUnderLine
                    val displayUnder = this.makeUnderline(under)
                    if (displayUnder != null) {
                        displayAtoms.add(displayUnder)
                        currentPosition.x += displayUnder.width
                        // add super scripts || subscripts
                        if (atom.subScript != null || atom.superScript != null) {
                            this.makeScripts(atom, displayUnder, under.indexRange.location, 0.0f)
                        }
                    }
                }

                KMTMathAtomOverline -> {
                    // stash the existing layout
                    if (currentLine.isNotEmpty()) {
                        this.addDisplayLine()
                    }
                    // Overline is considered as Ord in rule 16.
                    this.addInterElementSpace(prevNode, KMTMathAtomOrdinary)
                    atom.type = KMTMathAtomOrdinary

                    val over = atom as MTOverLine
                    val displayOver = this.makeOverline(over)
                    if (displayOver != null) {
                        displayAtoms.add(displayOver)
                        currentPosition.x += displayOver.width
                        // add super scripts || subscripts
                        if (atom.subScript != null || atom.superScript != null) {
                            this.makeScripts(atom, displayOver, over.indexRange.location, 0.0f)
                        }
                    }
                }

                KMTMathAtomAccent -> {
                    // stash the existing layout
                    if (currentLine.isNotEmpty()) {
                        this.addDisplayLine()
                    }
                    // Accent is considered as Ord in rule 16.
                    this.addInterElementSpace(prevNode, KMTMathAtomOrdinary)
                    atom.type = KMTMathAtomOrdinary

                    val accent = atom as MTAccent
                    val displayAccent = this.makeAccent(accent)
                    if (displayAccent != null) {
                        displayAtoms.add(displayAccent)
                        currentPosition.x += displayAccent.width

                        // add super scripts || subscripts
                        if (atom.subScript != null || atom.superScript != null) {
                            this.makeScripts(atom, displayAccent, accent.indexRange.location, 0.0f)
                        }
                    }
                }

                KMTMathAtomTable -> {
                    // stash the existing layout
                    if (currentLine.isNotEmpty()) {
                        this.addDisplayLine()
                    }
                    // We will consider tables as inner
                    this.addInterElementSpace(prevNode, KMTMathAtomInner)
                    atom.type = KMTMathAtomInner

                    val table = atom as MTMathTable
                    val displayTable = this.makeTable(table)
                    displayAtoms.add(displayTable)
                    currentPosition.x += displayTable.width
                    // A table doesn't have subscripts or superscripts
                }

                KMTMathAtomOrdinary, KMTMathAtomBinaryOperator, KMTMathAtomRelation, KMTMathAtomOpen, KMTMathAtomClose,
                KMTMathAtomPlaceholder, KMTMathAtomPunctuation -> {
                    // stash the existing layout
                    if (currentLine.isNotEmpty()) {
                        this.addDisplayLine()
                    }
                    // the rendering for all the rest is pretty similar
                    // All we need is render the character and set the interelement space.
                    if (prevNode != null) {
                        val interElementSpace = this.getInterElementSpace(prevNode.type, atom.type)
                        if (currentLine.isNotEmpty()) {
                            if (interElementSpace > 0) {
                                //throw MathDisplayException("Kerning not handled")
                                // add a kerning of that space to the previous character
                                /*
                                [_currentLine addAttribute:(NSString*) kCTKernAttributeName
                                        value:[NSNumber numberWithFloat:interElementSpace]
                                range:[_currentLine.string rangeOfComposedCharacterSequenceAtIndex:_currentLine.length - 1]]
                                */
                                // We are drawing a char at a time on Android. So same as single char case
                                currentPosition.x += interElementSpace
                            }
                        } else {
                            // increase the space
                            currentPosition.x += interElementSpace
                        }
                    }
                    val current: String = atom.nucleus
                    currentLine += current
                    // add the atom to the current range
                    if (currentLineIndexRange.location == NSNotFound) {
                        currentLineIndexRange.location = atom.indexRange.location
                        currentLineIndexRange.length = atom.indexRange.length
                    } else {
                        currentLineIndexRange.length += atom.indexRange.length
                    }

                    // add the fused atoms
                    if (atom.fusedAtoms.count() > 0) {
                        this.currentAtoms.addAll(atom.fusedAtoms)
                    } else {
                        this.currentAtoms.add(atom)
                    }

                    // add super scripts || subscripts
                    // see a change
                    if (atom.subScript != null || atom.superScript != null) {
                        // stash the existing line
                        // We don't check _currentLine.length here since we want to allow empty lines with super/sub scripts.
                        val line = this.addDisplayLine()
                        var delta = 0.0f
                        if (atom.nucleus.isNotEmpty()) {
                            // Use the italic correction of the last character.
                            val glyph = styleFont.findGlyphForCharacterAtIndex(0, atom.nucleus)
                            delta = styleFont.mathTable.getItalicCorrection(glyph.gid)
                        }
                        if (delta > 0 && atom.subScript == null) {
                            // Add a kern of delta
                            currentPosition.x += delta
                        }
                        this.makeScripts(atom, line, atom.indexRange.maxrange - 1, delta)
                    }
                }
            }
            lastType = atom.type
            prevNode = atom
        }
        if (currentLine.isNotEmpty()) {
            this.addDisplayLine()
        }
        if (spaced && lastType != KMTMathAtomNone) {
            // If _spaced then add an interelement space between the last type and close
            val display = displayAtoms.last()
            val interElementSpace = this.getInterElementSpace(lastType, KMTMathAtomClose)
            display.width += interElementSpace
        }
    }


    private fun addDisplayLine(): MTCTLineDisplay {
        // add the font
        // [_currentLine addAttribute:(NSString *)kCTFontAttributeName value:(__bridge id)(_styleFont.ctFont) range:NSMakeRange(0, _currentLine.length)]
        /*NSAssert(_currentLineIndexRange.length == numCodePoints(_currentLine.string),
         @"The length of the current line: %@ does not match the length of the range (%d, %d)",
         _currentLine, _currentLineIndexRange.location, _currentLineIndexRange.length)*/

        val displayAtom = MTCTLineDisplay(currentLine, currentLineIndexRange, styleFont, currentAtoms)
        displayAtom.position = currentPosition
        displayAtoms.add(displayAtom)
        // update the position
        currentPosition.x += displayAtom.width
        // clear the string and the range
        currentLine = ""
        currentAtoms = mutableListOf()
        currentLineIndexRange = NSRange()
        return displayAtom
    }


    // Spacing

    // Returned in units of mu = 1/18 em.
    private fun getSpacingInMu(type: MTInterElementSpaceType): Int {
        return when (type) {
            KMTSpaceInvalid -> -1
            KMTSpaceNone -> 0
            KMTSpaceThin -> 3
            KMTSpaceNSThin -> if (style < KMTLineStyleScript) 3 else 0
            KMTSpaceNSMedium -> if (style < KMTLineStyleScript) 4 else 0
            KMTSpaceNSThick -> if (style < KMTLineStyleScript) 5 else 0
        }
    }


    private fun getInterElementSpace(left: MTMathAtomType, right: MTMathAtomType): Float {
        val leftIndex = getInterElementSpaceArrayIndexForType(left, true)
        val rightIndex = getInterElementSpaceArrayIndexForType(right, false)
        val spaceArray = interElementSpaceArray[leftIndex]
        val spaceType = spaceArray[rightIndex]
        if (spaceType == KMTSpaceInvalid) throw MathDisplayException("Invalid space between $left and $right")

        val spaceMultipler = this.getSpacingInMu(spaceType)
        if (spaceMultipler > 0) {
            // 1 em = size of font in pt. space multipler is in multiples mu or 1/18 em
            return spaceMultipler * styleFont.mathTable.muUnit()
        }
        return 0.0f
    }


    // Subscript/Superscript

    private fun scriptStyle(): MTLineStyle {
        when (this.style) {
            KMTLineStyleDisplay, KMTLineStyleText -> return KMTLineStyleScript
            KMTLineStyleScript -> return KMTLineStyleScriptScript
            KMTLineStyleScriptScript -> return KMTLineStyleScriptScript
        }
    }


    // subscript is always cramped
    private fun subScriptCramped(): Boolean {
        return true
    }

    // superscript is cramped only if the current style is cramped
    private fun superScriptCramped(): Boolean {
        return cramped
    }

    private fun superScriptShiftUp(): Float {
        return if (cramped) {
            styleFont.mathTable.superscriptShiftUpCramped
        } else {
            styleFont.mathTable.superscriptShiftUp
        }
    }

    // make scripts for the last atom
// index is the index of the element which is getting the sub/super scripts.
    private fun makeScripts(atom: MTMathAtom, display: MTDisplay, index: Int, delta: Float) {
        val subScriptList = atom.subScript
        val superScriptList = atom.superScript

        assert(subScriptList != null || superScriptList != null)

        display.hasScript = true

        // get the font in script style
        val scriptFontSize = this.getStyleSize(this.scriptStyle(), this.font)
        val scriptFont = this.font.copyFontWithSize(scriptFontSize)
        val scriptFontMetrics = scriptFont.mathTable

        // if it is not a simple line then
        var superScriptShiftUp = display.ascent - scriptFontMetrics.superscriptBaselineDropMax
        var subscriptShiftDown = display.descent + scriptFontMetrics.subscriptBaselineDropMin

        if (superScriptList == null && subScriptList != null) {
            val subscript = createLineForMathList(subScriptList, this.font, this.scriptStyle(), this.subScriptCramped())
            subscript.type = MTLinePosition.KMTLinePositionSubscript
            subscript.index = index

            subscriptShiftDown = maxOf(subscriptShiftDown, styleFont.mathTable.subscriptShiftDown)
            subscriptShiftDown = maxOf(subscriptShiftDown, subscript.ascent - styleFont.mathTable.subscriptTopMax)
            // add the subscript
            subscript.position = CGPoint(currentPosition.x, currentPosition.y - subscriptShiftDown)
            displayAtoms.add(subscript)
            // update the position
            currentPosition.x += subscript.width + styleFont.mathTable.spaceAfterScript
            return
        }

        val superScript = createLineForMathList(superScriptList!!, this.font, this.scriptStyle(), superScriptCramped())
        superScript.type = MTLinePosition.KMTLinePositionSuperscript
        superScript.index = index
        superScriptShiftUp = maxOf(superScriptShiftUp, this.superScriptShiftUp())
        superScriptShiftUp = maxOf(superScriptShiftUp, superScript.descent + styleFont.mathTable.superscriptBottomMin)

        if (subScriptList == null) {
            superScript.position = CGPoint(currentPosition.x, currentPosition.y + superScriptShiftUp)
            displayAtoms.add(superScript)
            // update the position
            currentPosition.x += superScript.width + styleFont.mathTable.spaceAfterScript
            return
        }
        val subScript = createLineForMathList(subScriptList, this.font, this.scriptStyle(), subScriptCramped())
        subScript.type = MTLinePosition.KMTLinePositionSubscript
        subScript.index = index
        subscriptShiftDown = maxOf(subscriptShiftDown, styleFont.mathTable.subscriptShiftDown)

        // joint positioning of subscript & superscript
        val subSuperScriptGap: Float = (superScriptShiftUp - superScript.descent) + (subscriptShiftDown - subScript.ascent)
        if (subSuperScriptGap < styleFont.mathTable.subSuperscriptGapMin) {
            // Set the gap to atleast as much
            subscriptShiftDown += styleFont.mathTable.subSuperscriptGapMin - subSuperScriptGap
            val superscriptBottomDelta: Float = styleFont.mathTable.superscriptBottomMaxWithSubscript - (superScriptShiftUp - superScript.descent)
            if (superscriptBottomDelta > 0) {
                // superscript is lower than the max allowed by the font with a subscript.
                superScriptShiftUp += superscriptBottomDelta
                subscriptShiftDown -= superscriptBottomDelta
            }
        }
        // The delta is the italic correction above that shift superscript position
        superScript.position = CGPoint(currentPosition.x + delta, currentPosition.y + superScriptShiftUp)
        displayAtoms.add(superScript)
        subScript.position = CGPoint(currentPosition.x, currentPosition.y - subscriptShiftDown)
        displayAtoms.add(subScript)
        currentPosition.x += maxOf(superScript.width + delta, subScript.width) + styleFont.mathTable.spaceAfterScript
    }

// Fractions

    fun numeratorShiftUp(hasRule: Boolean): Float {
        if (hasRule) {
            if (this.style == KMTLineStyleDisplay) {
                return this.styleFont.mathTable.fractionNumeratorDisplayStyleShiftUp
            } else {
                return this.styleFont.mathTable.fractionNumeratorShiftUp
            }
        } else {
            if (this.style == KMTLineStyleDisplay) {
                return this.styleFont.mathTable.stackTopDisplayStyleShiftUp
            } else {
                return this.styleFont.mathTable.stackTopShiftUp
            }
        }
    }

    fun numeratorGapMin(): Float {
        if (this.style == KMTLineStyleDisplay) {
            return this.styleFont.mathTable.fractionNumeratorDisplayStyleGapMin
        } else {
            return this.styleFont.mathTable.fractionNumeratorGapMin
        }
    }

    fun denominatorShiftDown(hasRule: Boolean): Float {
        if (hasRule) {
            if (this.style == KMTLineStyleDisplay) {
                return this.styleFont.mathTable.fractionDenominatorDisplayStyleShiftDown
            } else {
                return this.styleFont.mathTable.fractionDenominatorShiftDown
            }
        } else {
            if (this.style == KMTLineStyleDisplay) {
                return this.styleFont.mathTable.stackBottomDisplayStyleShiftDown
            } else {
                return this.styleFont.mathTable.stackBottomShiftDown
            }
        }
    }

    fun denominatorGapMin(): Float {
        if (this.style == KMTLineStyleDisplay) {
            return this.styleFont.mathTable.fractionDenominatorDisplayStyleGapMin
        } else {
            return this.styleFont.mathTable.fractionDenominatorGapMin
        }
    }

    fun stackGapMin(): Float {
        if (this.style == KMTLineStyleDisplay) {
            return this.styleFont.mathTable.stackDisplayStyleGapMin
        } else {
            return this.styleFont.mathTable.stackGapMin
        }
    }

    fun fractionDelimiterHeight(): Float {
        if (this.style == KMTLineStyleDisplay) {
            return this.styleFont.mathTable.fractionDelimiterDisplayStyleSize
        } else {
            return this.styleFont.mathTable.fractionDelimiterSize
        }
    }


    private fun fractionStyle(): MTLineStyle {
        return when (this.style) {
            KMTLineStyleDisplay -> KMTLineStyleText
            KMTLineStyleText -> KMTLineStyleScript
            KMTLineStyleScript, KMTLineStyleScriptScript -> KMTLineStyleScriptScript
        }
    }


    private fun makeFraction(frac: MTFraction): MTDisplay {
        // lay out the parts of the fraction
        val fractionStyle = this.fractionStyle()
        val numeratorDisplay = createLineForMathList(frac.numerator!!, this.font, fractionStyle, false)
        val denominatorDisplay = createLineForMathList(frac.denominator!!, this.font, fractionStyle, true)

        // determine the location of the numerator
        var numeratorShiftUp: Float = numeratorShiftUp(frac.hasRule)
        var denominatorShiftDown: Float = denominatorShiftDown(frac.hasRule)
        val barLocation: Float = styleFont.mathTable.axisHeight
        val barThickness: Float = if (frac.hasRule) styleFont.mathTable.fractionRuleThickness else 0.0f

        if (frac.hasRule) {
            // This is the difference between the lowest edge of the numerator and the top edge of the fraction bar
            val distanceFromNumeratorToBar = (numeratorShiftUp - numeratorDisplay.descent) - (barLocation + barThickness / 2)
            // The distance should at least be displayGap
            val minNumeratorGap = this.numeratorGapMin()
            if (distanceFromNumeratorToBar < minNumeratorGap) {
                // This makes the distance between the bottom of the numerator and the top edge of the fraction bar
                // at least minNumeratorGap.
                numeratorShiftUp += (minNumeratorGap - distanceFromNumeratorToBar)
            }

            // Do the same for the denominator
            // This is the difference between the top edge of the denominator and the bottom edge of the fraction bar
            val distanceFromDenominatorToBar = (barLocation - barThickness / 2) - (denominatorDisplay.ascent - denominatorShiftDown)
            // The distance should at least be denominator gap
            val minDenominatorGap = this.denominatorGapMin()
            if (distanceFromDenominatorToBar < minDenominatorGap) {
                // This makes the distance between the top of the denominator and the bottom of the fraction bar to be exactly
                // minDenominatorGap
                denominatorShiftDown += (minDenominatorGap - distanceFromDenominatorToBar)
            }
        } else {
            // This is the distance between the numerator and the denominator
            val clearance = (numeratorShiftUp - numeratorDisplay.descent) - (denominatorDisplay.ascent - denominatorShiftDown)
            // This is the minimum clearance between the numerator and denominator.
            val minGap = this.stackGapMin()
            if (clearance < minGap) {
                numeratorShiftUp += (minGap - clearance) / 2
                denominatorShiftDown += (minGap - clearance) / 2
            }
        }

        val displayFraction = MTFractionDisplay(numeratorDisplay, denominatorDisplay, frac.indexRange)
        displayFraction.position = currentPosition
        displayFraction.numeratorUp = numeratorShiftUp
        displayFraction.denominatorDown = denominatorShiftDown
        displayFraction.lineThickness = barThickness
        displayFraction.linePosition = barLocation
        if (frac.leftDelimiter == null && frac.rightDelimiter == null) {
            return displayFraction
        } else {
            return this.addDelimitersToFractionDisplay(displayFraction, frac)
        }
    }


    private fun addDelimitersToFractionDisplay(display: MTFractionDisplay, frac: MTFraction): MTDisplay {
        assert(frac.leftDelimiter != null || frac.rightDelimiter != null)

        val innerElements = MutableList(0, { MTDisplay() })
        val glyphHeight = this.fractionDelimiterHeight()
        val position = CGPoint()

        val ld = frac.leftDelimiter
        if (ld != null) {
            if (ld.isNotEmpty()) {
                val leftGlyph = this.findGlyphForBoundary(ld, glyphHeight)
                leftGlyph.position = position.copy()
                position.x += leftGlyph.width
                innerElements.add(leftGlyph)
            }
        }

        display.position = position.copy()
        position.x += display.width
        innerElements.add(display)

        val rd = frac.rightDelimiter
        if (rd != null && rd.isNotEmpty()) {
            val rightGlyph = this.findGlyphForBoundary(rd, glyphHeight)
            rightGlyph.position = position.copy()
            position.x += rightGlyph.width
            innerElements.add(rightGlyph)
        }

        val innerDisplay = MTMathListDisplay(innerElements, frac.indexRange)
        innerDisplay.position = currentPosition
        return innerDisplay
    }


    // Radicals

    private fun radicalVerticalGap(): Float {
        if (style == KMTLineStyleDisplay) {
            return styleFont.mathTable.radicalDisplayStyleVerticalGap
        } else {
            return styleFont.mathTable.radicalVerticalGap
        }
    }

    private fun getRadicalGlyphWithHeight(radicalHeight: Float): MTDisplay {

        val radicalGlyph = styleFont.findGlyphForCharacterAtIndex(0, "\u221A")
        val glyph = this.findGlyph(radicalGlyph, radicalHeight)

        var glyphDisplay: MTDisplay? = null
        if (glyph.glyphAscent + glyph.glyphDescent < radicalHeight) {
            // the glyphs is not as large as required. A glyph needs to be constructed using the extenders.
            glyphDisplay = this.constructGlyph(radicalGlyph, radicalHeight)
        }

        if (glyphDisplay == null) {
            // No constructed display so use the glyph we got.
            glyphDisplay = MTGlyphDisplay(glyph, NSRange(-1, 0), styleFont)
            glyphDisplay.ascent = glyph.glyphAscent
            glyphDisplay.descent = glyph.glyphDescent
            glyphDisplay.width = glyph.glyphWidth
        }
        return glyphDisplay
    }


    private fun makeRadical(radicand: MTMathList, range: NSRange): MTRadicalDisplay {
        val innerDisplay = createLineForMathList(radicand, font, style, true)
        var clearance = this.radicalVerticalGap()
        val radicalRuleThickness = styleFont.mathTable.radicalRuleThickness
        val radicalHeight = innerDisplay.ascent + innerDisplay.descent + clearance + radicalRuleThickness

        val glyph = this.getRadicalGlyphWithHeight(radicalHeight)


        // Note this is a departure from Latex. Latex assumes that glyphAscent == thickness.
        // Open type math makes no such assumption, and ascent and descent are independent of the thickness.
        // Latex computes delta as descent - (h(inner) + d(inner) + clearance)
        // but since we may not have ascent == thickness, we modify the delta calculation slightly.
        // If the font designer followes Latex conventions, it will be identical.
        val delta = (glyph.descent + glyph.ascent) - (innerDisplay.ascent + innerDisplay.descent + clearance + radicalRuleThickness)
        if (delta > 0) {
            clearance += delta / 2  // increase the clearance to center the radicand inside the sign.
        }

        // we need to shift the radical glyph up, to coincide with the baseline of inner.
        // The new ascent of the radical glyph should be thickness + adjusted clearance + h(inner)
        val radicalAscent = radicalRuleThickness + clearance + innerDisplay.ascent
        val shiftUp = radicalAscent - glyph.ascent  // Note: if the font designer followed latex conventions, this is the same as glyphAscent == thickness.
        glyph.shiftDown = -shiftUp

        val radicalDisplay = MTRadicalDisplay(innerDisplay, glyph, range)
        radicalDisplay.position = currentPosition
        radicalDisplay.ascent = radicalAscent + styleFont.mathTable.radicalExtraAscender
        radicalDisplay.topKern = styleFont.mathTable.radicalExtraAscender
        radicalDisplay.lineThickness = radicalRuleThickness
        // Note: Until we have radical construction from parts, it is possible that glyphAscent+glyphDescent is less
        // than the requested height of the glyph (i.e. radicalHeight, so in the case the innerDisplay has a larger
        // descent we use the innerDisplay's descent.
        radicalDisplay.descent = maxOf(glyph.ascent + glyph.descent - radicalAscent, innerDisplay.descent)
        radicalDisplay.width = glyph.width + innerDisplay.width
        return radicalDisplay
    }


    // Glyphs

    private fun findGlyph(glyph: CGGlyph, height: Float): CGGlyph {
        val variants = styleFont.mathTable.getVerticalVariantsForGlyph(glyph)
        val numVariants = variants.count()


        val bboxes: Array<BoundingBox?> = arrayOfNulls(numVariants)
        val advances: Array<Float> = Array(numVariants, { 0.0f })
        // Get the bounds for these glyphs
        styleFont.mathTable.getBoundingRectsForGlyphs(variants, bboxes, numVariants)
        styleFont.mathTable.getAdvancesForGlyphs(variants, advances, numVariants)
        var ascent = 0.0f
        var descent = 0.0f
        var width = 0.0f

        for (i in 0 until numVariants) {
            val bounds = bboxes[i]
            width = advances[i]
            ascent = getBboxDetailsAscent(bounds)
            descent = getBboxDetailsDescent(bounds)

            if (ascent + descent >= height) {
                return CGGlyph(variants[i], ascent, descent, width)
            }
        }
        return CGGlyph(variants[numVariants - 1], ascent, descent, width)

    }

    private fun constructGlyph(glyph: CGGlyph, glyphHeight: Float): MTGlyphConstructionDisplay? {
        val parts = styleFont.mathTable.getVerticalGlyphAssemblyForGlyph(glyph.gid)
        if (parts == null || parts.count() == 0) {
            return null
        }

        val glyphs: MutableList<Int> = MutableList(0, { 0 })
        val offsets: MutableList<Float> = MutableList(0, { 0.0f })

        val height = constructGlyphWithParts(parts, glyphHeight, glyphs, offsets)
        val advances = arrayOf(0.0f)

        styleFont.mathTable.getAdvancesForGlyphs(glyphs.toList(), advances, 1)
        val display = MTGlyphConstructionDisplay(glyphs, offsets, styleFont)
        display.width = advances[0] // width of first glyph
        display.ascent = height
        display.descent = 0.0f   // it's upto the rendering to adjust the display up or down.
        return display
    }

    private fun constructGlyphWithParts(parts: List<MTGlyphPart>, glyphHeight: Float, glyphs: MutableList<Int>, offsets: MutableList<Float>): Float {

        var numExtenders = 0
        while (true) {
            var prev: MTGlyphPart? = null
            val minDistance = styleFont.mathTable.minConnectorOverlap
            var minOffset = 0.0f
            var maxDelta = 1000000.0f // large flost
            glyphs.clear()
            offsets.clear()

            for (part in parts) {
                var repeats = 1
                if (part.isExtender) {
                    repeats = numExtenders
                }
                // add the extender num extender times
                for (i in 0 until repeats) {
                    glyphs.add(part.glyph)
                    if (prev != null) {
                        val maxOverlap = minOf(prev.endConnectorLength, part.startConnectorLength)
                        // the minimum amount we can add to the offset
                        val minOffsetDelta = prev.fullAdvance - maxOverlap
                        // The maximum amount we can add to the offset.
                        val maxOffsetDelta = prev.fullAdvance - minDistance
                        // we can increase the offsets by at most max - min.
                        maxDelta = minOf(maxDelta, maxOffsetDelta - minOffsetDelta)
                        minOffset += minOffsetDelta
                    }
                    offsets.add(minOffset)
                    prev = part
                }
            }

            if (prev == null) {
                numExtenders++
                continue   // maybe only extenders
            }
            val minHeight = minOffset + prev.fullAdvance
            val maxHeight = minHeight + maxDelta * (glyphs.count() - 1)
            if (minHeight >= glyphHeight) {
                // we are done
                return minHeight
            } else if (glyphHeight <= maxHeight) {
                // spread the delta equally between all the connectors
                val delta = glyphHeight - minHeight
                val deltaIncrease = delta / (glyphs.count() - 1)
                var lastOffset = 0.0f
                for (i in 0 until offsets.count()) {
                    val offset = offsets[i] + i * deltaIncrease
                    offsets[i] = offset
                    lastOffset = offset
                }
                // we are done
                return lastOffset + prev.fullAdvance
            }
            numExtenders++
        }
    }



    // Large Operators

    private fun makeLargeOp(op: MTLargeOperator): MTDisplay {
        val limits = (op.hasLimits && style == KMTLineStyleDisplay)
        val delta: Float

        if (op.nucleus.length == 1) {
            var glyph: CGGlyph = styleFont.findGlyphForCharacterAtIndex(0, op.nucleus)
            if (style == KMTLineStyleDisplay && glyph.isValid) {
                // Enlarge the character in display style.
                glyph = CGGlyph(styleFont.mathTable.getLargerGlyph(glyph.gid))
            }
            // This is be the italic correction of the character.
            delta = styleFont.mathTable.getItalicCorrection(glyph.gid)

            // vertically center
            val bboxes: Array<BoundingBox?> = arrayOfNulls(1)
            val advances: Array<Float> = Array(1, { 0.0f })
            val variants = listOf(glyph.gid)
            // Get the bounds for these glyphs
            styleFont.mathTable.getBoundingRectsForGlyphs(variants, bboxes, variants.count())
            styleFont.mathTable.getAdvancesForGlyphs(variants, advances, variants.count())

            val ascent = getBboxDetailsAscent(bboxes[0])
            val descent = getBboxDetailsDescent(bboxes[0])
            val shiftDown = 0.5f * (ascent - descent) - styleFont.mathTable.axisHeight
            val glyphDisplay = MTGlyphDisplay(glyph, op.indexRange, styleFont)
            glyphDisplay.ascent = ascent
            glyphDisplay.descent = descent
            glyphDisplay.width = advances[0]
            if (op.subScript != null && !limits) {
                // Remove italic correction from the width of the glyph if
                // there is a subscript and limits is not set.
                glyphDisplay.width -= delta
            }
            glyphDisplay.shiftDown = shiftDown
            glyphDisplay.position = currentPosition
            return this.addLimitsToDisplay(glyphDisplay, op, delta)
        } else {
            val atoms = mutableListOf<MTMathAtom>()
            atoms.add(op)
            val displayAtom = MTCTLineDisplay(op.nucleus, op.indexRange, styleFont, atoms)
            displayAtom.position = currentPosition
            return this.addLimitsToDisplay(displayAtom, op, 0.0f)
        }
    }


    private fun addLimitsToDisplay(display: MTDisplay, op: MTLargeOperator, delta: Float): MTDisplay {
        // If there is no subscript or superscript, just return the current display
        if (op.subScript == null && op.superScript == null) {
            currentPosition.x += display.width
            return display
        }
        if (op.hasLimits && style == KMTLineStyleDisplay) {
            // make limits
            var superScript: MTMathListDisplay? = null
            var subScript: MTMathListDisplay? = null

            if (op.superScript != null) {
                superScript = createLineForMathList(op.superScript!!, font, this.scriptStyle(), this.superScriptCramped())

            }
            if (op.subScript != null) {
                subScript = createLineForMathList(op.subScript!!, font, this.scriptStyle(), this.subScriptCramped())
            }
            assert(superScript != null || subScript != null) //  At least one of superscript or subscript should have been present.
            val opsDisplay = MTLargeOpLimitsDisplay(display, superScript, subScript, delta / 2, 0.0f)
            if (superScript != null) {
                val upperLimitGap = maxOf(styleFont.mathTable.upperLimitGapMin, styleFont.mathTable.upperLimitBaselineRiseMin - superScript.descent)
                opsDisplay.upperLimitGap = upperLimitGap
            }
            if (subScript != null) {
                val lowerLimitGap = maxOf(styleFont.mathTable.lowerLimitGapMin, styleFont.mathTable.lowerLimitBaselineDropMin - subScript.ascent)
                opsDisplay.lowerLimitGap = lowerLimitGap
            }
            opsDisplay.position = currentPosition
            opsDisplay.range = op.indexRange.copy()
            currentPosition.x += opsDisplay.width
            return opsDisplay
        } else {
            currentPosition.x += display.width
            this.makeScripts(op, display, op.indexRange.location, delta)
            return display
        }
    }


    // Large delimiters

    private fun makeLeftRight(inner: MTInner): MTDisplay {
        assert(inner.leftBoundary != null || inner.rightBoundary != null) // Inner should have a boundary to call this function

        val innerListDisplay = createLineForMathList(inner.innerList!!, font, style, cramped, true)
        val axisHeight = styleFont.mathTable.axisHeight
        // delta is the max distance from the axis
        val delta = maxOf(innerListDisplay.ascent - axisHeight, innerListDisplay.descent + axisHeight)
        val d1 = (delta / 500.0f) * kDelimiterFactor  // This represents atleast 90% of the formula
        val d2 = 2.0f * delta - kDelimiterShortfallPoints  // This represents a shortfall of 5pt
        // The size of the delimiter glyph should cover at least 90% of the formula or
        // be at most 5pt short.
        val glyphHeight = maxOf(d1, d2)

        val innerElements = mutableListOf<MTDisplay>()
        val position = CGPoint()
        val lb = inner.leftBoundary
        if (lb != null && lb.nucleus.isNotEmpty()) {
            val leftGlyph = this.findGlyphForBoundary(lb.nucleus, glyphHeight)
            leftGlyph.position = position.copy()
            position.x += leftGlyph.width
            innerElements.add(leftGlyph)
        }

        innerListDisplay.position = position.copy()
        position.x += innerListDisplay.width
        innerElements.add(innerListDisplay)

        val rb = inner.rightBoundary
        if (rb != null && rb.nucleus.isNotEmpty()) {
            val rightGlyph = this.findGlyphForBoundary(rb.nucleus, glyphHeight)
            rightGlyph.position = position.copy()
            position.x += rightGlyph.width
            innerElements.add(rightGlyph)
        }
        val innerDisplay = MTMathListDisplay(innerElements, inner.indexRange)
        return innerDisplay
    }


    private fun findGlyphForBoundary(delimiter: String, glyphHeight: Float): MTDisplay {
        val leftGlyph = styleFont.findGlyphForCharacterAtIndex(0, delimiter)
        val glyph = this.findGlyph(leftGlyph, glyphHeight)

        var glyphDisplay: MTDisplay? = null
        if (glyph.glyphAscent + glyph.glyphDescent < glyphHeight) {
            // the glyphs is not as large as required. A glyph needs to be constructed using the extenders.
            glyphDisplay = this.constructGlyph(leftGlyph, glyphHeight)
        }

        if (glyphDisplay == null) {
            // No constructed display so use the glyph we got.
            glyphDisplay = MTGlyphDisplay(glyph, NSRange(-1, 0), styleFont)
            glyphDisplay.ascent = glyph.glyphAscent
            glyphDisplay.descent = glyph.glyphDescent
            glyphDisplay.width = glyph.glyphWidth
        }


        // Center the glyph on the axis
        val shiftDown = 0.5f * (glyphDisplay.ascent - glyphDisplay.descent) - styleFont.mathTable.axisHeight
        glyphDisplay.shiftDown = shiftDown
        return glyphDisplay
    }

    // Underline/Overline
    private fun makeUnderline(under: MTUnderLine): MTDisplay? {
        if (under.innerList != null) {
            val innerListDisplay = createLineForMathList(under.innerList!!, font, style, cramped)

            val underDisplay = MTLineDisplay(innerListDisplay, under.indexRange)
            // Move the line down by the vertical gap.
            underDisplay.lineShiftUp = -(innerListDisplay.descent + styleFont.mathTable.underbarVerticalGap)
            underDisplay.lineThickness = styleFont.mathTable.underbarRuleThickness
            underDisplay.ascent = innerListDisplay.ascent
            underDisplay.descent = innerListDisplay.descent + styleFont.mathTable.underbarVerticalGap + styleFont.mathTable.underbarRuleThickness + styleFont.mathTable.underbarExtraDescender
            underDisplay.width = innerListDisplay.width
            underDisplay.position = currentPosition
            return underDisplay
        }
        return null
    }

    private fun makeOverline(over: MTOverLine): MTDisplay? {
        if (over.innerList != null) {
            val innerListDisplay = createLineForMathList(over.innerList!!, font, style, cramped)
            val overDisplay = MTLineDisplay(innerListDisplay, over.indexRange)
            overDisplay.lineShiftUp = innerListDisplay.ascent + styleFont.mathTable.overbarVerticalGap
            overDisplay.lineThickness = styleFont.mathTable.underbarRuleThickness
            overDisplay.ascent = innerListDisplay.ascent + styleFont.mathTable.overbarVerticalGap + styleFont.mathTable.overbarRuleThickness + styleFont.mathTable.overbarExtraAscender
            overDisplay.descent = innerListDisplay.descent
            overDisplay.width = innerListDisplay.width
            overDisplay.position = currentPosition
            return overDisplay
        }
        return null
    }

    // Accents

    private fun isSingleCharAccentee(accent: MTAccent): Boolean {
        if (accent.innerList != null) {

            if (accent.innerList!!.atoms.count() != 1) {
                // Not a single char list.
                return false
            }
            val innerAtom = accent.innerList!!.atoms[0]
            if (numberOfGlyphs(innerAtom.nucleus) != 1) {
                // A complex atom, not a simple char.
                return false
            }
            if (innerAtom.subScript != null || innerAtom.superScript != null) {
                return false
            }
            return true
        }
        return false
    }

    // The distance the accent must be moved from the beginning.
    private fun getSkew(accent: MTAccent, accenteeWidth: Float, accentGlyph: CGGlyph): Float {
        if (accent.nucleus.isEmpty()) {
            // No accent
            return 0.0f
        }
        val accentAdjustment = styleFont.mathTable.getTopAccentAdjustment(accentGlyph.gid)
        var accenteeAdjustment = 0.0f
        if (!this.isSingleCharAccentee(accent)) {
            // use the center of the accentee
            accenteeAdjustment = accenteeWidth / 2
        } else if (accent.innerList != null) {
            val innerAtom = accent.innerList!!.atoms[0]
            val accenteeGlyph = styleFont.findGlyphForCharacterAtIndex(0, innerAtom.nucleus)
            accenteeAdjustment = styleFont.mathTable.getTopAccentAdjustment(accenteeGlyph.gid)
        }
        // The adjustments need to aligned, so skew is just the difference.
        return (accenteeAdjustment - accentAdjustment)
    }

    // Find the largest horizontal variant if exists, with width less than max width.
    private fun findVariantGlyph(glyph: CGGlyph, maxWidth: Float): CGGlyph {
        val variants = styleFont.mathTable.getHorizontalVariantsForGlyph(glyph)
        val numVariants = variants.count()
        assert(numVariants > 0) //  @"A glyph is always it's own variant, so number of variants should be > 0")

        val bboxes: Array<BoundingBox?> = arrayOfNulls(numVariants)

        val advances: Array<Float> = Array(numVariants, { 0.0f })
        // Get the bounds for these glyphs
        styleFont.mathTable.getBoundingRectsForGlyphs(variants, bboxes, numVariants)
        styleFont.mathTable.getAdvancesForGlyphs(variants, advances, numVariants)
        val retGlyph = CGGlyph()

        for (i in 0 until numVariants) {
            val bounds = bboxes[i]
            if (bounds != null) {
                val ascent = getBboxDetailsAscent(bounds)
                val descent = getBboxDetailsDescent(bounds)
                val width = maxOf(bounds.lowerLeftX, bounds.upperRightX)

                if (width > maxWidth) {
                    if (i == 0) {
                        // glyph dimensions are not yet set
                        retGlyph.glyphWidth = advances[i]
                        retGlyph.glyphAscent = ascent
                        retGlyph.glyphDescent = descent
                    }
                    return retGlyph
                } else {
                    retGlyph.gid = variants[i]
                    retGlyph.glyphWidth = advances[i]
                    retGlyph.glyphAscent = ascent
                    retGlyph.glyphDescent = descent
                }
            }
        }
        // We exhausted all the variants and none was larger than the width, so we return the largest
        return retGlyph
    }


    private fun makeAccent(accent: MTAccent): MTDisplay? {
        if (accent.innerList != null) {

            var accentee = createLineForMathList(accent.innerList!!, font, style, true)
            if (accent.nucleus.isEmpty()) {
                // no accent!
                return accentee
            }
            var accentGlyph = styleFont.findGlyphForCharacterAtIndex(0, accent.nucleus)
            val accenteeWidth = accentee.width
            accentGlyph = this.findVariantGlyph(accentGlyph, accenteeWidth)
            val delta = minOf(accentee.ascent, styleFont.mathTable.accentBaseHeight)

            val skew = this.getSkew(accent, accenteeWidth, accentGlyph)
            val height = accentee.ascent - delta  // This is always positive since delta <= height.
            val accentPosition = CGPoint(skew, height)
            val accentGlyphDisplay = MTGlyphDisplay(accentGlyph, accent.indexRange, styleFont)
            accentGlyphDisplay.ascent = accentGlyph.glyphAscent
            accentGlyphDisplay.descent = accentGlyph.glyphDescent
            accentGlyphDisplay.width = accentGlyph.glyphWidth
            accentGlyphDisplay.position = accentPosition

            if (this.isSingleCharAccentee(accent) && (accent.subScript != null || accent.superScript != null)) {
                // Attach the super/subscripts to the accentee instead of the accent.
                val innerAtom = accent.innerList!!.atoms[0]
                innerAtom.superScript = accent.superScript
                innerAtom.subScript = accent.subScript
                accent.superScript = null
                accent.subScript = null
                // Remake the accentee (now with sub/superscripts)
                // Note: Latex adjusts the heights in case the height of the char is different in non-cramped mode. However this shouldn't be the case since cramping
                // only affects fractions and superscripts. We skip adjusting the heights.
                accentee = createLineForMathList(accent.innerList!!, font, style, cramped)
            }

            val display = MTAccentDisplay(accentGlyphDisplay, accentee, accent.indexRange)
            display.width = accentee.width
            display.descent = accentee.descent
            val ascent = accentee.ascent - delta + accentGlyph.glyphAscent
            display.ascent = maxOf(accentee.ascent, ascent)
            display.position = currentPosition

            return display
        } else {
            return null
        }
    }


    private fun makeTable(table: MTMathTable): MTDisplay {
        val numColumns = table.numColumns()
        if (numColumns == 0 || table.numRows() == 0) {
            // Empty table
            val emptylist = List(0, { MTDisplay() })
            return MTMathListDisplay(emptylist, table.indexRange)
        }

        val columnWidths = Array(numColumns, { 0.0f })
        val displays: Array<Array<MTDisplay>> = this.typesetCells(table, columnWidths)

        // Position all the columns in each row
        val rowDisplays = MutableList(0, { MTDisplay() })
        for (row in displays) {
            val rowDisplay: MTMathListDisplay = this.makeRowWithColumns(row, table, columnWidths)
            rowDisplays.add(rowDisplay)
        }

        // Position all the rows
        this.positionRows(rowDisplays, table)
        val tableDisplay = MTMathListDisplay(rowDisplays, table.indexRange)
        tableDisplay.position = currentPosition
        return tableDisplay
    }

    // Typeset every cell in the table. As a side-effect calculate the max column width of each column.
    private fun typesetCells(table: MTMathTable, columnWidths: Array<Float>): Array<Array<MTDisplay>> {
        val displays = Array(table.numRows(), { Array<MTDisplay>(0, { MTDisplay() }) })

        for (r in 0 until table.numRows()) {
            val row = table.cells[r]
            val colDisplays = Array<MTDisplay>(row.count(), { MTDisplay() })
            displays[r] = colDisplays
            for (i in 0 until row.count()) {
                val disp: MTDisplay? = createLineForMathList(row[i], font, style, false)
                columnWidths[i] = maxOf(disp!!.width, columnWidths[i])
                colDisplays[i] = disp
            }
        }
        return displays
    }

    private fun makeRowWithColumns(cols: Array<MTDisplay>, table: MTMathTable, columnWidths: Array<Float>): MTMathListDisplay {
        var columnStart = 0.0f
        var rowRange = NSRange()
        for (i in 0 until cols.count()) {
            val col = cols[i]
            val colWidth = columnWidths[i]
            val alignment = table.getAlignmentForColumn(i)

            var cellPos = columnStart
            when (alignment) {
                MTColumnAlignment.KMTColumnAlignmentRight -> {
                    cellPos += colWidth - col.width
                }

                KMTColumnAlignmentCenter -> {
                    cellPos += (colWidth - col.width) / 2
                }

                KMTColumnAlignmentLeft -> {
                    // No changes if left aligned
                }
            }
            if (rowRange.location != NSNotFound) {
                rowRange = rowRange.union(col.range)
            } else {
                rowRange = col.range.copy()
            }

            col.position = CGPoint(cellPos, 0.0f)
            columnStart += colWidth + table.interColumnSpacing * styleFont.mathTable.muUnit()
        }
        // Create a display for the row
        val rowDisplay = MTMathListDisplay(cols.toList(), rowRange)
        return rowDisplay
    }

    private fun positionRows(rows: MutableList<MTDisplay>, table: MTMathTable) {
        // Position the rows
        // We will first position the rows starting from 0 and then in the second pass center the whole table vertically.
        var currPos = 0.0f
        val openup = table.interRowAdditionalSpacing * kJotMultiplier * styleFont.fontSize
        val baselineSkip = openup + kBaseLineSkipMultiplier * styleFont.fontSize
        val lineSkip = openup + kLineSkipMultiplier * styleFont.fontSize
        val lineSkipLimit = openup + kLineSkipLimitMultiplier * styleFont.fontSize
        var prevRowDescent = 0.0f
        var ascent = 0.0f
        var first = true

        for (row in rows) {
            if (first) {
                row.position = CGPoint()
                ascent += row.ascent
                first = false
            } else {
                var skip = baselineSkip
                if (skip - (prevRowDescent + row.ascent) < lineSkipLimit) {
                    // rows are too close to each other. Space them apart further
                    skip = prevRowDescent + row.ascent + lineSkip
                }
                // We are going down so we decrease the y value.
                currPos -= skip
                row.position = CGPoint(0.0f, currPos)
            }
            prevRowDescent = row.descent
        }

        // Vertically center the whole structure around the axis
        // The descent of the structure is the position of the last row
        // plus the descent of the last row.
        val descent = -currPos + prevRowDescent
        val shiftDown = 0.5f * (ascent - descent) - styleFont.mathTable.axisHeight

        for (row in rows) {
            row.position = CGPoint(row.position.x, row.position.y - shiftDown)
        }
    }


    private fun getBboxDetailsDescent(bbox: BoundingBox?): Float {
        // Descent is how much the line goes below the origin. However if the line is all above the origin, then descent can't be negative.
        if (bbox == null) {
            return 0.0f
        } else {
            return maxOf(0.0f, 0.0f - minOf(bbox.upperRightY, bbox.lowerLeftY))
        }
    }


    private fun getBboxDetailsAscent(bbox: BoundingBox?): Float {
        if (bbox == null) {
            return 0.0f
        } else {
            return maxOf(0.0f, maxOf(bbox.upperRightY, bbox.lowerLeftY))
        }
    }

}
