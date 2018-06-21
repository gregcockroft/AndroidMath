package com.agog.mathdisplay;

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.agog.mathdisplay.parse.*
import com.agog.mathdisplay.render.*
import com.agog.mathdisplay.render.MTTypesetter
import com.agog.mathdisplay.parse.MTFontStyle.*


import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before
import org.junit.Assert.*


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4::class)
class TypesetterInstrumentedTest {

    private var context: Context? = null
    private var font: MTFont? = null

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getContext()
        assertNotNull(context)
        MTFontManager.setContext(context!!)
        font = MTFontManager.defaultFont()


    }

    @Test
    public fun testSimpleVariable() {
        val mathList = MTMathList()
        val atom = MTMathAtom.atomForCharacter('x')
        assertNotNull("atomForCharacter", atom)
        if (atom != null) {
            mathList.addAtom(atom)
            val display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
            assertNotNull("createLineForMathList", display)
            assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
            assertTrue(display.position.equals(CGPoint()))
            assertTrue(display.range.equals(NSRange(0, 1)))
            assertFalse(display.hasScript)
            assertEquals(display.index, NSNotFound)

            val subdisplays = display.subDisplays
            assertEquals(subdisplays!!.count(), 1)

            val sub0: MTDisplay = subdisplays[0]

            assertTrue(sub0 is MTCTLineDisplay)
            val line = sub0 as MTCTLineDisplay
            assertEquals(line.atoms.count(), 1)
            // The x is italicized
            assertEquals("𝑥", line.str)
            assertTrue(line.position.equals(CGPoint()))
            assertTrue(line.range.equal(NSRange(0, 1)))
            assertFalse(line.hasScript)

            // dimensions
            assertEquals(display.ascent, line.ascent)
            assertEquals(display.descent, line.descent)
            assertEquals(display.width, line.width)

            assertEquals(8.834f, display.ascent, 0.05f)
            assertEquals(0.24f, display.descent, 0.05f)
            assertEquals(11.44f, display.width, 0.05f)
        }
    }

    @Test
    public fun testMultipleVariables() {
        val mathList = MTMathAtom.mathListForCharacters("xyzw")

        val display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("createLineForMathList", display)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 4)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertEquals(subdisplays!!.count(), 1)

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTCTLineDisplay)
        val line = sub0 as MTCTLineDisplay
        assertEquals(line.atoms.count(), 4)
        assertEquals("𝑥𝑦𝑧𝑤", line.str)
        assertTrue(line.position.equals(CGPoint()))
        assertTrue(line.range.equal(NSRange(0, 4)))
        assertFalse(line.hasScript)

        // dimensions
        assertEquals(display.ascent, line.ascent)
        assertEquals(display.descent, line.descent)
        assertEquals(display.width, line.width)

        assertEquals(8.834f, display.ascent, 0.05f)
        assertEquals(4.12f, display.descent, 0.05f)
        assertEquals(44.86f, display.width, 0.05f)
    }

    @Test
    public fun testVariablesAndNumbers() {
        val mathList = MTMathAtom.mathListForCharacters("xy2w")

        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("createLineForMathList", display)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 4)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertEquals(subdisplays!!.count(), 1)

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTCTLineDisplay)
        val line = sub0 as MTCTLineDisplay
        assertEquals(line.atoms.count(), 4)
        assertEquals("𝑥𝑦2𝑤", line.str)
        assertTrue(line.position.equals(CGPoint()))
        assertTrue(line.range.equal(NSRange(0, 4)))
        assertFalse(line.hasScript)

        // dimensions
        assertEquals(display.ascent, line.ascent)
        assertEquals(display.descent, line.descent)
        assertEquals(display.width, line.width)

        assertEquals(13.32f, display.ascent, 0.05f)
        assertEquals(4.12f, display.descent, 0.05f)
        assertEquals(45.56f, display.width, 0.05f)
    }

    @Test
    public fun testEquationWithOperatorsAndRelations() {
        val mathList = MTMathAtom.mathListForCharacters("2x+3=y")

        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("createLineForMathList", display)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 6)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertEquals(subdisplays!!.count(), 5)

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTCTLineDisplay)
        val line = sub0 as MTCTLineDisplay
        assertEquals(2, line.atoms.count())
        assertEquals("2𝑥", line.str)
        assertTrue(line.position.equals(CGPoint()))
        assertTrue(line.range.equal(NSRange(0, 2)))
        assertFalse(line.hasScript)

        // dimensions
        assertEquals(13.32f, display.ascent, 0.05f)
        assertEquals(4.12f, display.descent, 0.05f)
        assertEquals(92.36f, display.width, 0.05f)
    }

    fun assertEqualsCGPoint(p2: CGPoint, p1: CGPoint, acc: Float) {
        assertEquals(p1.x, p2.x, acc)
        assertEquals(p1.y, p2.y, acc)
    }


    @Test
    public fun testSuperscript() {
        val mathList = MTMathList()
        val x = MTMathAtom.atomForCharacter('x')
        assertNotNull("testSuperscript", x)

        val supersc = MTMathList()
        val s = MTMathAtom.atomForCharacter('2')
        assertNotNull("testSuperscript", s)
        supersc.addAtom(s!!)

        x!!.superScript = supersc
        mathList.addAtom(x)

        val display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("createLineForMathList", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertEquals(subdisplays!!.count(), 2)

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTCTLineDisplay)
        val line = sub0 as MTCTLineDisplay
        assertEquals(line.atoms.count(), 1)
        // The x is italicized
        assertEquals("𝑥", line.str)
        assertTrue(line.position.equals(CGPoint()))
        assertTrue(line.hasScript)

        val sub1: MTDisplay = subdisplays[1]
        assertTrue(sub1 is MTMathListDisplay)
        val display2 = sub1 as MTMathListDisplay
        assertEquals(display2.type, MTLinePosition.KMTLinePositionSuperscript)
        assertEqualsCGPoint(display2.position, CGPoint(11.44f, 7.26f), 0.05f)
        assertTrue(display2.range.equals(NSRange(0, 1)))
        assertFalse(display2.hasScript)
        assertEquals(display2.index, 0)
        assertNotNull("createLineForMathList", display2.subDisplays)
        assertEquals(display2.subDisplays!!.count(), 1)


        val sub1sub0 = display2.subDisplays!![0]
        assertTrue(sub1sub0 is MTCTLineDisplay)
        val line2 = sub1sub0 as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 1)
        assertEquals(line2.str, "2")
        assertTrue(line2.position.equals(CGPoint()))
        assertFalse(line2.hasScript)


        // dimensions
        assertEquals(16.584f, display.ascent, 0.05f)
        assertEquals(0.24f, display.descent, 0.05f)
        assertEquals(18.44f, display.width, 0.05f)
    }

    @Test
    fun testSubscript() {
        val mathList = MTMathList()
        val x = MTMathAtom.atomForCharacter('x')

        val subsc = MTMathList()
        val t = MTMathAtom.atomForCharacter('1')
        assertNotNull("testSuperscript", t)
        subsc.addAtom(t!!)
        x!!.subScript = subsc
        mathList.addAtom(x)

        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("createLineForMathList", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertEquals(subdisplays!!.count(), 2)

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTCTLineDisplay)
        val line = sub0 as MTCTLineDisplay
        assertEquals(line.atoms.count(), 1)
        // The x is italicized
        assertEquals("𝑥", line.str)
        assertTrue(line.position.equals(CGPoint()))
        assertTrue(line.hasScript)

        val sub1: MTDisplay = subdisplays[1]
        assertTrue(sub1 is MTMathListDisplay)
        val display2 = sub1 as MTMathListDisplay
        assertEquals(display2.type, MTLinePosition.KMTLinePositionSubscript)
        assertEqualsCGPoint(display2.position, CGPoint(11.44f, -4.94f), 0.05f)
        assertTrue(display2.range.equals(NSRange(0, 1)))
        assertFalse(display2.hasScript)
        assertEquals(display2.index, 0)
        assertNotNull("createLineForMathList", display2.subDisplays)
        assertEquals(display2.subDisplays!!.count(), 1)

        val sub1sub0 = display2.subDisplays!![0]
        assertTrue(sub1sub0 is MTCTLineDisplay)
        val line2 = sub1sub0 as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 1)
        assertEquals(line2.str, "1")
        assertTrue(line2.position.equals(CGPoint()))
        assertFalse(line2.hasScript)

        // dimensions
        assertEquals(8.834f, display.ascent, 0.05f)
        assertEquals(4.954f, display.descent, 0.05f)
        assertEquals(18.44f, display.width, 0.05f)
    }

    @Test
    public fun testSupersubscript() {
        val mathList = MTMathList()
        val x = MTMathAtom.atomForCharacter('x')
        assertNotNull("testSupersubscript", x)
        val supersc = MTMathList()
        val s = MTMathAtom.atomForCharacter('2')
        assertNotNull("testSupersubscript", s)
        supersc.addAtom(s!!)
        val subsc = MTMathList()
        val t = MTMathAtom.atomForCharacter('1')
        assertNotNull("testSupersubscript", t)
        subsc.addAtom(t!!)
        x!!.subScript = subsc
        x.superScript = supersc
        mathList.addAtom(x)

        val display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("createLineForMathList", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertEquals(subdisplays!!.count(), 3)

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTCTLineDisplay)
        val line = sub0 as MTCTLineDisplay
        assertEquals(line.atoms.count(), 1)
        // The x is italicized
        assertEquals("𝑥", line.str)
        assertTrue(line.position.equals(CGPoint()))
        assertTrue(line.hasScript)

        val sub1: MTDisplay = subdisplays[1]
        assertTrue(sub1 is MTMathListDisplay)
        val display2 = sub1 as MTMathListDisplay
        assertEquals(display2.type, MTLinePosition.KMTLinePositionSuperscript)
        assertEqualsCGPoint(display2.position, CGPoint(11.44f, 7.26f), 0.05f)
        assertTrue(display2.range.equals(NSRange(0, 1)))
        assertFalse(display2.hasScript)
        assertEquals(display2.index, 0)
        assertNotNull("createLineForMathList", display2.subDisplays)
        assertEquals(display2.subDisplays!!.count(), 1)

        val sub1sub0 = display2.subDisplays!![0]
        assertTrue(sub1sub0 is MTCTLineDisplay)
        val line2 = sub1sub0 as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 1)
        assertEquals(line2.str, "2")
        assertTrue(line2.position.equals(CGPoint()))
        assertFalse(line2.hasScript)

        val sub2: MTDisplay = subdisplays[2]
        assertTrue(sub2 is MTMathListDisplay)
        val display3 = sub2 as MTMathListDisplay
        assertEquals(display3.type, MTLinePosition.KMTLinePositionSubscript)
        assertEqualsCGPoint(display3.position, CGPoint(11.44f, -5.278f), 0.05f)
        assertTrue(display3.range.equals(NSRange(0, 1)))
        assertFalse(display3.hasScript)
        assertEquals(display3.index, 0)
        assertNotNull("createLineForMathList", display2.subDisplays)
        assertEquals(display3.subDisplays!!.count(), 1)

        val sub2sub0 = display3.subDisplays!![0]
        assertTrue(sub2sub0 is MTCTLineDisplay)
        val line3 = sub2sub0 as MTCTLineDisplay
        assertEquals(line3.atoms.count(), 1)
        assertEquals(line3.str, "1")
        assertTrue(line3.position.equals(CGPoint()))
        assertFalse(line3.hasScript)


        // dimensions
        assertEquals(16.584f, display.ascent, 0.05f)
        assertEquals(5.292f, display.descent, 0.05f)
        assertEquals(18.44f, display.width, 0.05f)
    }

    @Test
    public fun testRadical() {
        val mathList = MTMathList()
        val rad = MTRadical()
        val radicand = MTMathList()

        val t = MTMathAtom.atomForCharacter('1')
        assertNotNull("testRadical", t)
        radicand.addAtom(t!!)
        rad.radicand = radicand
        mathList.addAtom(rad)

        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("createLineForMathList", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testRadical", subdisplays)
        assertEquals(subdisplays!!.count(), 1)

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTRadicalDisplay)
        val radical = sub0 as MTRadicalDisplay
        assertTrue(radical.range.equals(NSRange(0, 1)))
        assertFalse(radical.hasScript)
        assertTrue(radical.position.equals(CGPoint()))
        assertNotNull("testRadical", radical.radicand)
        assertNull("testRadical", radical.degree)

        val display2 = radical.radicand
        assertEquals(display2.type, MTLinePosition.KMTLinePositionRegular)
        assertEqualsCGPoint(display2.position, CGPoint(16.66f, 0f), 0.05f)
        assertTrue(display2.range.equals(NSRange(0, 1)))
        assertFalse(display2.hasScript)
        assertEquals(display2.index, NSNotFound)
        val subDisplays = display2.subDisplays
        assertNotNull("testRadical", subDisplays)
        assertEquals(subDisplays!!.count(), 1)

        val subrad = subDisplays[0]
        assertTrue(subrad is MTCTLineDisplay)
        val line2 = subrad as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 1)
        assertEquals(line2.str, "1")
        assertTrue(line2.position.equals(CGPoint()))
        assertTrue(line2.range.equals(NSRange(0, 1)))
        assertFalse(line2.hasScript)

        // dimensions
        assertEquals(19.34f, display.ascent, 0.05f)
        assertEquals(1.48f, display.descent, 0.05f)
        assertEquals(26.66f, display.width, 0.05f)
    }


    @Test
    public fun testRadicalWithDegree() {
        val mathList = MTMathList()
        val rad = MTRadical()
        val radicand = MTMathList()

        var t = MTMathAtom.atomForCharacter('1')
        assertNotNull("testRadicalWithDegree", t)
        radicand.addAtom(t!!)
        val degree = MTMathList()
        t = MTMathAtom.atomForCharacter('3')
        assertNotNull("testRadicalWithDegree", t)
        degree.addAtom(t!!)

        rad.radicand = radicand
        rad.degree = degree
        mathList.addAtom(rad)


        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("createLineForMathList", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testRadicalWithDegree", subdisplays)
        assertEquals(subdisplays!!.count(), 1)

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTRadicalDisplay)
        val radical = sub0 as MTRadicalDisplay
        assertTrue(radical.range.equals(NSRange(0, 1)))
        assertFalse(radical.hasScript)
        assertTrue(radical.position.equals(CGPoint()))
        assertNotNull("testRadicalWithDegree", radical.radicand)


        val display2 = radical.radicand
        assertEquals(display2.type, MTLinePosition.KMTLinePositionRegular)
        assertEqualsCGPoint(display2.position, CGPoint(16.66f, 0f), 0.05f)
        assertTrue(display2.range.equals(NSRange(0, 1)))
        assertFalse(display2.hasScript)
        assertEquals(display2.index, NSNotFound)
        var subDisplays = display2.subDisplays
        assertNotNull("testRadicalWithDegree", subDisplays)
        assertEquals(subDisplays!!.count(), 1)

        val subrad = subDisplays[0]
        assertTrue(subrad is MTCTLineDisplay)
        val line2 = subrad as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 1)
        assertEquals(line2.str, "1")
        assertTrue(line2.position.equals(CGPoint()))
        assertTrue(line2.range.equals(NSRange(0, 1)))
        assertFalse(line2.hasScript)


        assertNotNull("testRadicalWithDegree", radical.degree)
        val display3 = radical.degree
        assertEquals(display3!!.type, MTLinePosition.KMTLinePositionRegular)
        assertEqualsCGPoint(display3.position, CGPoint(6.12f, 10.716f), 0.05f)
        assertTrue(display3.range.equals(NSRange(0, 1)))
        assertFalse(display3.hasScript)
        assertEquals(display3.index, NSNotFound)
        subDisplays = display3.subDisplays
        assertNotNull("testRadicalWithDegree", subDisplays)
        assertEquals(subDisplays!!.count(), 1)


        assertNotNull("testRadicalWithDegree", display3.subDisplays)
        val subdeg = display3.subDisplays!![0]
        assertTrue(subdeg is MTCTLineDisplay)
        val line3 = subdeg as MTCTLineDisplay
        assertEquals(line3.atoms.count(), 1)
        assertEquals(line3.str, "3")
        assertTrue(line3.position.equals(CGPoint()))
        assertTrue(line3.range.equals(NSRange(0, 1)))
        assertFalse(line3.hasScript)


        // dimensions
        assertEquals(19.34f, display.ascent, 0.05f)
        assertEquals(1.48f, display.descent, 0.05f)
        assertEquals(26.66f, display.width, 0.05f)
    }

    @Test
    public fun testFraction() {
        val mathList = MTMathList()
        val frac = MTFraction(true)
        val num = MTMathList()
        var t = MTMathAtom.atomForCharacter('1')
        assertNotNull("testFraction", t)
        num.addAtom(t!!)
        val denom = MTMathList()
        t = MTMathAtom.atomForCharacter('3')
        assertNotNull("testFraction", t)
        denom.addAtom(t!!)

        frac.numerator = num
        frac.denominator = denom
        mathList.addAtom(frac)

        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("createLineForMathList", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testFraction", subdisplays)
        assertEquals(subdisplays!!.count(), 1)

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTFractionDisplay)
        val fraction = sub0 as MTFractionDisplay
        assertTrue(fraction.range.equals(NSRange(0, 1)))
        assertFalse(fraction.hasScript)
        assertTrue(fraction.position.equals(CGPoint()))
        assertNotNull("testFraction", fraction.numerator)
        assertNotNull("testFraction", fraction.denominator)

        val display2 = fraction.numerator
        assertEquals(display2.type, MTLinePosition.KMTLinePositionRegular)
        assertEqualsCGPoint(display2.position, CGPoint(0f, 13.54f), 0.05f)
        assertTrue(display2.range.equals(NSRange(0, 1)))
        assertFalse(display2.hasScript)
        assertEquals(display2.index, NSNotFound)
        var subDisplays = display2.subDisplays
        assertNotNull("testFraction", subDisplays)
        assertEquals(subDisplays!!.count(), 1)

        val subnum = subDisplays[0]
        assertTrue(subnum is MTCTLineDisplay)
        val line2 = subnum as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 1)
        assertEquals(line2.str, "1")
        assertTrue(line2.position.equals(CGPoint()))
        assertTrue(line2.range.equals(NSRange(0, 1)))
        assertFalse(line2.hasScript)

        val display3 = fraction.denominator
        assertEquals(display3.type, MTLinePosition.KMTLinePositionRegular)
        assertEqualsCGPoint(display3.position, CGPoint(0f, -13.72f), 0.05f)
        assertTrue(display3.range.equals(NSRange(0, 1)))
        assertFalse(display3.hasScript)
        assertEquals(display3.index, NSNotFound)
        subDisplays = display3.subDisplays
        assertNotNull("testFraction", subDisplays)
        assertEquals(subDisplays!!.count(), 1)

        val subdenom = subDisplays[0]
        assertTrue(subdenom is MTCTLineDisplay)
        val line3 = subdenom as MTCTLineDisplay
        assertEquals(line3.atoms.count(), 1)
        assertEquals(line3.str, "3")
        assertTrue(line3.position.equals(CGPoint()))
        assertTrue(line3.range.equals(NSRange(0, 1)))
        assertFalse(line3.hasScript)


        // dimensions
        assertEquals(26.86f, display.ascent, 0.05f)
        assertEquals(14.18f, display.descent, 0.05f)
        assertEquals(10f, display.width, 0.05f)

    }


    @Test
    public fun testAtop() {
        val mathList = MTMathList()
        val frac = MTFraction(false)
        val num = MTMathList()
        var t = MTMathAtom.atomForCharacter('1')
        assertNotNull("testFraction", t)
        num.addAtom(t!!)
        val denom = MTMathList()
        t = MTMathAtom.atomForCharacter('3')
        assertNotNull("testFraction", t)
        denom.addAtom(t!!)

        frac.numerator = num
        frac.denominator = denom
        mathList.addAtom(frac)

        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("createLineForMathList", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testFraction", subdisplays)
        assertEquals(subdisplays!!.count(), 1)

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTFractionDisplay)
        val fraction = sub0 as MTFractionDisplay
        assertTrue(fraction.range.equals(NSRange(0, 1)))
        assertFalse(fraction.hasScript)
        assertTrue(fraction.position.equals(CGPoint()))
        assertNotNull("testFraction", fraction.numerator)
        assertNotNull("testFraction", fraction.denominator)

        val display2 = fraction.numerator
        assertEquals(display2.type, MTLinePosition.KMTLinePositionRegular)
        assertEqualsCGPoint(display2.position, CGPoint(0f, 13.54f), 0.05f)
        assertTrue(display2.range.equals(NSRange(0, 1)))
        assertFalse(display2.hasScript)
        assertEquals(display2.index, NSNotFound)
        var subDisplays = display2.subDisplays
        assertNotNull("testFraction", subDisplays)
        assertEquals(subDisplays!!.count(), 1)

        val subnum = subDisplays[0]
        assertTrue(subnum is MTCTLineDisplay)
        val line2 = subnum as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 1)
        assertEquals(line2.str, "1")
        assertTrue(line2.position.equals(CGPoint()))
        assertTrue(line2.range.equals(NSRange(0, 1)))
        assertFalse(line2.hasScript)

        val display3 = fraction.denominator
        assertEquals(display3.type, MTLinePosition.KMTLinePositionRegular)
        assertEqualsCGPoint(display3.position, CGPoint(0f, -13.72f), 0.05f)
        assertTrue(display3.range.equals(NSRange(0, 1)))
        assertFalse(display3.hasScript)
        assertEquals(display3.index, NSNotFound)
        subDisplays = display3.subDisplays
        assertNotNull("testFraction", subDisplays)
        assertEquals(subDisplays!!.count(), 1)

        val subdenom = subDisplays[0]
        assertTrue(subdenom is MTCTLineDisplay)
        val line3 = subdenom as MTCTLineDisplay
        assertEquals(line3.atoms.count(), 1)
        assertEquals(line3.str, "3")
        assertTrue(line3.position.equals(CGPoint()))
        assertTrue(line3.range.equals(NSRange(0, 1)))
        assertFalse(line3.hasScript)


        // dimensions
        assertEquals(26.86f, display.ascent, 0.05f)
        assertEquals(14.18f, display.descent, 0.05f)
        assertEquals(10f, display.width, 0.05f)

    }


    @Test
    public fun testBinomial() {
        val mathList = MTMathList()
        val frac = MTFraction(false)
        val num = MTMathList()
        var t = MTMathAtom.atomForCharacter('1')
        assertNotNull("testFraction", t)
        num.addAtom(t!!)
        val denom = MTMathList()
        t = MTMathAtom.atomForCharacter('3')
        assertNotNull("testFraction", t)
        denom.addAtom(t!!)

        frac.numerator = num
        frac.denominator = denom
        frac.leftDelimiter = "("
        frac.rightDelimiter = ")"
        mathList.addAtom(frac)

        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("createLineForMathList", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testBinomial", subdisplays)
        assertEquals(subdisplays!!.count(), 1)

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTMathListDisplay)
        val display0 = sub0 as MTMathListDisplay
        assertEquals(display0.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display0.position.equals(CGPoint()))
        assertTrue(display0.range.equals(NSRange(0, 1)))
        assertFalse(display0.hasScript)
        assertEquals(display0.index, NSNotFound)
        val sd = display0.subDisplays
        assertNotNull("testBinomial", sd)
        assertEquals(sd!!.count(), 3)

        val subLeft = sd[0]
        assertTrue(subLeft is MTGlyphDisplay)
        val glyph = subLeft as MTGlyphDisplay
        assertTrue(glyph.position.equals(CGPoint()))
        assertTrue(glyph.range.equals(NSRange()))
        assertFalse(glyph.hasScript)

        val subFrac = sd[1]
        assertTrue(subFrac is MTFractionDisplay)
        val fraction = subFrac as MTFractionDisplay
        assertTrue(fraction.range.equals(NSRange(0, 1)))
        assertFalse(fraction.hasScript)
        assertEqualsCGPoint(fraction.position, CGPoint(14.72f, 0.0f), 0.05f)
        assertNotNull("testBinomial", fraction.numerator)
        assertNotNull("testBinomial", fraction.denominator)

        val display2 = fraction.numerator
        assertEquals(display2.type, MTLinePosition.KMTLinePositionRegular)
        assertEqualsCGPoint(display2.position, CGPoint(14.72f, 13.54f), 0.05f)
        assertTrue(display2.range.equals(NSRange(0, 1)))
        assertFalse(display2.hasScript)
        assertEquals(display2.index, NSNotFound)
        var subDisplays = display2.subDisplays
        assertNotNull("testBinomial", subDisplays)
        assertEquals(subDisplays!!.count(), 1)

        val subnum = subDisplays[0]
        assertTrue(subnum is MTCTLineDisplay)
        val line2 = subnum as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 1)
        assertEquals(line2.str, "1")
        assertTrue(line2.position.equals(CGPoint()))
        assertTrue(line2.range.equals(NSRange(0, 1)))
        assertFalse(line2.hasScript)

        val display3 = fraction.denominator
        assertEquals(display3.type, MTLinePosition.KMTLinePositionRegular)
        assertEqualsCGPoint(display3.position, CGPoint(14.72f, -13.72f), 0.05f)
        assertTrue(display3.range.equals(NSRange(0, 1)))
        assertFalse(display3.hasScript)
        assertEquals(display3.index, NSNotFound)
        subDisplays = display3.subDisplays
        assertNotNull("testFraction", subDisplays)
        assertEquals(subDisplays!!.count(), 1)

        val subdenom = subDisplays[0]
        assertTrue(subdenom is MTCTLineDisplay)
        val line3 = subdenom as MTCTLineDisplay
        assertEquals(line3.atoms.count(), 1)
        assertEquals(line3.str, "3")
        assertTrue(line3.position.equals(CGPoint()))
        assertTrue(line3.range.equals(NSRange(0, 1)))
        assertFalse(line3.hasScript)

        val subRight = sd[2]
        assertTrue(subRight is MTGlyphDisplay)
        val glyph2 = subRight as MTGlyphDisplay
        assertEqualsCGPoint(glyph2.position, CGPoint(24.72f, 0f), 0.05f)
        assertTrue(glyph2.range.equals(NSRange()))
        assertFalse(glyph2.hasScript)

        // dimensions
        assertEquals(28.93f, display.ascent, 0.05f)
        assertEquals(18.93f, display.descent, 0.05f)
        assertEquals(39.44f, display.width, 0.05f)

    }

    @Test
    public fun testLargeOpNoLimitsText() {
        val mathList = MTMathList()
        var t = MTMathAtom.atomForLatexSymbolName("sin")
        assertNotNull("testLargeOpNoLimitsText", t)
        mathList.addAtom(t!!)
        t = MTMathAtom.atomForCharacter('x')
        assertNotNull("testLargeOpNoLimitsText", t)
        mathList.addAtom(t!!)


        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("testLargeOpNoLimitsText", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 2)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testLargeOpNoLimitsText", subdisplays)
        assertEquals(subdisplays!!.count(), 2)

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTCTLineDisplay)
        val line = sub0 as MTCTLineDisplay
        assertEquals(line.atoms.count(), 1)
        assertEquals(line.str, "sin")
        assertTrue(line.position.equals(CGPoint()))
        assertTrue(line.range.equals(NSRange(0, 1)))
        assertFalse(line.hasScript)

        val sub1: MTDisplay = subdisplays[1]
        assertTrue(sub1 is MTCTLineDisplay)
        val line2 = sub1 as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 1)
        // italic x
        assertEquals(line2.str, "𝑥")
        assertEqualsCGPoint(line2.position, CGPoint(27.893f, 0f), 0.05f)
        assertTrue(line2.range.equals(NSRange(1, 1)))
        assertFalse(line2.hasScript)

        // dimensions
        assertEquals(13.14f, display.ascent, 0.05f)
        assertEquals(0.24f, display.descent, 0.05f)
        assertEquals(39.33f, display.width, 0.05f)

    }

    @Test
    public fun testLargeOpNoLimitsSymbol() {
        val mathList = MTMathList()
        var t = MTMathAtom.atomForLatexSymbolName("int")
        assertNotNull("testLargeOpNoLimitsSymbol", t)
        mathList.addAtom(t!!)
        t = MTMathAtom.atomForCharacter('x')
        assertNotNull("testLargeOpNoLimitsSymbol", t)
        mathList.addAtom(t!!)


        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("testLargeOpNoLimitsText", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 2)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testLargeOpNoLimitsSymbol", subdisplays)
        assertEquals(subdisplays!!.count(), 2)

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTGlyphDisplay)
        val glyph = sub0 as MTGlyphDisplay
        assertTrue(glyph.position.equals(CGPoint()))
        assertTrue(glyph.range.equals(NSRange(0, 1)))
        assertFalse(glyph.hasScript)

        val sub1: MTDisplay = subdisplays[1]
        assertTrue(sub1 is MTCTLineDisplay)
        val line2 = sub1 as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 1)
        // italic x
        assertEquals(line2.str, "𝑥")
        assertEqualsCGPoint(line2.position, CGPoint(23.313f, 0f), 0.05f)
        assertTrue(line2.range.equals(NSRange(1, 1)))
        assertFalse(line2.hasScript)

        // dimensions
        assertEquals(27.23f, display.ascent, 0.05f)
        assertEquals(17.23f, display.descent, 0.05f)
        assertEquals(34.753f, display.width, 0.05f)

    }


    @Test
    public fun testLargeOpNoLimitsSymbolWithScripts() {
        val mathList = MTMathList()
        var op = MTMathAtom.atomForLatexSymbolName("int")
        assertNotNull("testLargeOpNoLimitsSymbol", op)
        val supersc = MTMathList()
        var t = MTMathAtom.atomForCharacter('1')
        supersc.addAtom(t!!)
        op!!.superScript = supersc
        val subsc = MTMathList()
        t = MTMathAtom.atomForCharacter('0')
        subsc.addAtom(t!!)
        op!!.subScript = subsc
        mathList.addAtom(op!!)
        t = MTMathAtom.atomForCharacter('x')
        mathList.addAtom(t!!)


        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("testLargeOpNoLimitsSymbolWithScripts", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 2)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testLargeOpNoLimitsSymbolWithScripts", subdisplays)
        assertEquals(subdisplays!!.count(), 4)


        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTMathListDisplay)
        val display0 = sub0 as MTMathListDisplay
        assertEquals(display0.type, MTLinePosition.KMTLinePositionSuperscript)
        assertEqualsCGPoint(display0.position, CGPoint(19.98f, 23.73f), 0.05f)
        assertTrue(display0.range.equals(NSRange(0, 1)))
        assertFalse(display0.hasScript)
        assertEquals(display0.index, 0)
        var sd = display0.subDisplays
        assertNotNull("testLargeOpNoLimitsSymbolWithScripts", sd)
        assertEquals(sd!!.count(), 1)

        val sub0sub0 = sd[0]
        assertTrue(sub0sub0 is MTCTLineDisplay)
        val line1 = sub0sub0 as MTCTLineDisplay
        assertEquals(line1.atoms.count(), 1)
        assertEquals(line1.str, "1")
        assertEqualsCGPoint(line1.position, CGPoint(), 0.05f)
        assertFalse(line1.hasScript)


        val sub1: MTDisplay = subdisplays[1]
        assertTrue(sub1 is MTMathListDisplay)
        val display1 = sub1 as MTMathListDisplay
        assertEquals(display1.type, MTLinePosition.KMTLinePositionSubscript)
        assertEqualsCGPoint(display1.position, CGPoint(8.16f, -20.03f), 0.05f)
        assertTrue(display1.range.equals(NSRange(0, 1)))
        assertFalse(display1.hasScript)
        assertEquals(display1.index, 0)
        sd = display1.subDisplays
        assertNotNull("testLargeOpNoLimitsSymbolWithScripts", sd)
        assertEquals(sd!!.count(), 1)

        val sub1sub0 = sd[0]
        assertTrue(sub1sub0 is MTCTLineDisplay)
        val line3 = sub1sub0 as MTCTLineDisplay
        assertEquals(line3.atoms.count(), 1)
        assertEquals(line3.str, "0")
        assertEqualsCGPoint(line3.position, CGPoint(), 0.05f)
        assertFalse(line3.hasScript)

        val sub2: MTDisplay = subdisplays[2]
        assertTrue(sub2 is MTGlyphDisplay)
        val glyph = sub2 as MTGlyphDisplay
        assertEqualsCGPoint(glyph.position, CGPoint(), 0.05f)
        assertTrue(glyph.range.equals(NSRange(0, 1)))
        assertTrue(glyph.hasScript)  // There are subscripts and superscripts

        val sub3 = subdisplays[3]
        assertTrue(sub3 is MTCTLineDisplay)
        val line2 = sub3 as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 1)
        assertEquals(line2.str, "𝑥")
        assertEqualsCGPoint(line2.position, CGPoint(31.433f, 0f), 0.05f)
        assertTrue(line2.range.equals(NSRange(1, 1)))
        assertFalse(line2.hasScript)

        // dimensions
        assertEquals(33.054f, display.ascent, 0.05f)
        assertEquals(20.352f, display.descent, 0.05f)
        assertEquals(42.873f, display.width, 0.05f)

    }

    @Test
    public fun testLargeOpWithLimitsTextWithScripts() {
        val mathList = MTMathList()
        var op = MTMathAtom.atomForLatexSymbolName("lim")
        assertNotNull("testLargeOpNoLimitsSymbol", op)
        val subsc = MTMathList()
        var t = MTMathAtom.atomForLatexSymbolName("infty")
        subsc.addAtom(t!!)
        op!!.subScript = subsc
        mathList.addAtom(op!!)
        t = MTMathAtom.atomForCharacter('x')
        mathList.addAtom(t!!)


        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("hestLargeOpWithLimitsTextWithScripts", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 2)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testLargeOpNoLimitsSymbolWithScripts", subdisplays)
        assertEquals(subdisplays!!.count(), 2)


        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTLargeOpLimitsDisplay)
        val largeOp = sub0 as MTLargeOpLimitsDisplay
        assertEqualsCGPoint(largeOp.position, CGPoint(), 0.05f)
        assertTrue(largeOp.range.equals(NSRange(0, 1)))
        assertFalse(largeOp.hasScript)
        assertNotNull("testLargeOpWithLimitsTextWithScripts", largeOp.lowerLimit)
        assertNull("testLargeOpWithLimitsTextWithScripts", largeOp.upperLimit)

        val display2: MTMathListDisplay = largeOp.lowerLimit!!
        assertEquals(display2.type, MTLinePosition.KMTLinePositionRegular)
        assertEqualsCGPoint(display2.position, CGPoint(6.89f, -12.02f), 0.05f)
        assertTrue(display2.range.equals(NSRange(0, 1)))
        assertFalse(display2.hasScript)
        assertEquals(display2.index, NSNotFound)

        val sd = display2.subDisplays
        assertNotNull("testLargeOpWithLimitsTextWithScripts", sd)
        val sub0sub0 = sd!![0]
        assertTrue(sub0sub0 is MTCTLineDisplay)
        val line1 = sub0sub0 as MTCTLineDisplay
        assertEquals(line1.atoms.count(), 1)
        assertEquals(line1.str, "∞")
        assertEqualsCGPoint(line1.position, CGPoint(), 0.05f)
        assertFalse(line1.hasScript)


        val sub3 = subdisplays[1]
        assertTrue(sub3 is MTCTLineDisplay)
        val line2 = sub3 as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 1)
        assertEquals(line2.str, "𝑥")
        assertEqualsCGPoint(line2.position, CGPoint(31.1133f, 0f), 0.05f)
        assertTrue(line2.range.equals(NSRange(1, 1)))
        assertFalse(line2.hasScript)

        // dimensions
        assertEquals(13.88f, display.ascent, 0.05f)
        assertEquals(12.188f, display.descent, 0.05f)
        assertEquals(42.553f, display.width, 0.05f)

    }

    @Test
    public fun testLargeOpWithLimitsSymbolWithScripts() {
        val mathList = MTMathList()
        var op = MTMathAtom.atomForLatexSymbolName("sum")
        assertNotNull("testLargeOpNoLimitsSymbol", op)
        val supersc = MTMathList()
        var t = MTMathAtom.atomForLatexSymbolName("infty")
        supersc.addAtom(t!!)
        op!!.superScript = supersc
        val subsc = MTMathList()
        t = MTMathAtom.atomForCharacter('0')
        subsc.addAtom(t!!)
        op!!.subScript = subsc
        mathList.addAtom(op!!)
        t = MTMathAtom.atomForCharacter('x')
        mathList.addAtom(t!!)


        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("testLargeOpWithLimitsSymbolWithScripts", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 2)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testLargeOpNoLimitsSymbolWithScripts", subdisplays)
        assertEquals(subdisplays!!.count(), 2)


        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTLargeOpLimitsDisplay)
        val largeOp = sub0 as MTLargeOpLimitsDisplay
        assertEqualsCGPoint(largeOp.position, CGPoint(), 0.05f)
        assertTrue(largeOp.range.equals(NSRange(0, 1)))
        assertFalse(largeOp.hasScript)
        assertNotNull("testLargeOpWithLimitsSymbolWithScripts", largeOp.lowerLimit)
        assertNotNull("testLargeOpWithLimitsSymbolWithScripts", largeOp.upperLimit)

        val display2: MTMathListDisplay = largeOp.lowerLimit!!
        assertEquals(display2.type, MTLinePosition.KMTLinePositionRegular)
        assertEqualsCGPoint(display2.position, CGPoint(10.94f, -21.674f), 0.05f)
        assertTrue(display2.range.equals(NSRange(0, 1)))
        assertFalse(display2.hasScript)
        assertEquals(display2.index, NSNotFound)
        var sd = display2.subDisplays
        assertNotNull("testLargeOpWithLimitsSymbolWithScripts", sd)
        assertEquals(sd!!.count(), 1)


        val sub0sub0 = sd!![0]
        assertTrue(sub0sub0 is MTCTLineDisplay)
        val line1 = sub0sub0 as MTCTLineDisplay
        assertEquals(line1.atoms.count(), 1)
        assertEquals(line1.str, "0")
        assertEqualsCGPoint(line1.position, CGPoint(), 0.05f)
        assertFalse(line1.hasScript)

        val displayU: MTMathListDisplay = largeOp.upperLimit!!
        assertEquals(displayU.type, MTLinePosition.KMTLinePositionRegular)
        assertEqualsCGPoint(displayU.position, CGPoint(7.44f, 23.178f), 0.05f)
        assertTrue(displayU.range.equals(NSRange(0, 1)))
        assertFalse(displayU.hasScript)
        assertEquals(displayU.index, NSNotFound)
        sd = displayU.subDisplays
        assertNotNull("testLargeOpWithLimitsSymbolWithScripts", sd)
        assertEquals(sd!!.count(), 1)

        val sub0subU = sd!![0]
        assertTrue(sub0subU is MTCTLineDisplay)
        val line3 = sub0subU as MTCTLineDisplay
        assertEquals(line3.atoms.count(), 1)
        assertEquals(line3.str, "∞")
        assertEqualsCGPoint(line3.position, CGPoint(), 0.05f)
        assertFalse(line3.hasScript)


        val sub3 = subdisplays[1]
        assertTrue(sub3 is MTCTLineDisplay)
        val line2 = sub3 as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 1)
        assertEquals(line2.str, "𝑥")
        assertEqualsCGPoint(line2.position, CGPoint(32.2133f, 0f), 0.05f)
        assertTrue(line2.range.equals(NSRange(1, 1)))
        assertFalse(line2.hasScript)

        // dimensions
        assertEquals(29.366f, display.ascent, 0.05f)
        assertEquals(21.996f, display.descent, 0.05f)
        assertEquals(43.653f, display.width, 0.05f)

    }


    @Test
    public fun testInner() {
        val innerList = MTMathList()
        var t = MTMathAtom.atomForCharacter('x')
        innerList.addAtom(t!!)
        val inner = MTInner()
        inner.innerList = innerList
        inner.leftBoundary = MTMathAtom.atomWithType(MTMathAtomType.KMTMathAtomBoundary, "(")
        inner.rightBoundary = MTMathAtom.atomWithType(MTMathAtomType.KMTMathAtomBoundary, ")")

        val mathList = MTMathList()
        mathList.addAtom(inner)

        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("testInner", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testInner", subdisplays)
        assertEquals(subdisplays!!.count(), 1)


        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTMathListDisplay)
        val display2 = sub0 as MTMathListDisplay
        assertEquals(display2.type, MTLinePosition.KMTLinePositionRegular)
        assertEqualsCGPoint(display2.position, CGPoint(), 0.05f)
        assertTrue(display2.range.equals(NSRange(0, 1)))
        assertFalse(display2.hasScript)
        assertEquals(display2.index, NSNotFound)
        var sd = display2.subDisplays
        assertNotNull("testInner", sd)
        assertEquals(sd!!.count(), 3)


        val subLeft = sd!![0]
        assertTrue(subLeft is MTGlyphDisplay)
        val glyph = subLeft as MTGlyphDisplay
        assertEqualsCGPoint(glyph.position, CGPoint(), 0.05f)
        assertTrue(glyph.range.equals(NSRange()))
        assertFalse(glyph.hasScript)

        val sub3 = sd!![1]
        assertTrue(sub3 is MTMathListDisplay)
        val display3 = sub3 as MTMathListDisplay
        assertEquals(display3.type, MTLinePosition.KMTLinePositionRegular)
        assertEqualsCGPoint(display3.position, CGPoint(7.78f, 0f), 0.05f)
        assertTrue(display3.range.equals(NSRange(0, 1)))
        assertFalse(display3.hasScript)
        assertEquals(display3.index, NSNotFound)
        sd = display3.subDisplays
        assertNotNull("testInner", sd)
        assertEquals(sd!!.count(), 1)

        val subsub3 = sd!![0]
        assertTrue(subsub3 is MTCTLineDisplay)
        val line1 = subsub3 as MTCTLineDisplay
        assertEquals(line1.atoms.count(), 1)
        // The x is italicized
        assertEquals(line1.str, "𝑥")
        assertEqualsCGPoint(line1.position, CGPoint(), 0.05f)
        assertFalse(line1.hasScript)

        sd = display2.subDisplays
        val subRight = sd!![2]
        assertTrue(subRight is MTGlyphDisplay)
        val glyph2 = subRight as MTGlyphDisplay
        assertEqualsCGPoint(glyph2.position, CGPoint(19.22f, 0f), 0.05f)
        assertTrue(glyph2.range.equals(NSRange()))
        assertFalse(glyph2.hasScript)

        // dimensions
        assertEquals(display.ascent, display2.ascent);
        assertEquals(display.descent, display2.descent);
        assertEquals(display.width, display2.width);

        assertEquals(14.97f, display.ascent, 0.05f)
        assertEquals(4.97f, display.descent, 0.05f)
        assertEquals(27f, display.width, 0.05f)

    }


    @Test
    public fun testOverline() {
        val mathList = MTMathList()
        val over = MTOverLine()
        val inner = MTMathList()
        val t = MTMathAtom.atomForCharacter('1')
        inner.addAtom(t!!)
        over.innerList = inner
        mathList.addAtom(over)

        val display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("testInner", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testOverline", subdisplays)
        assertEquals(subdisplays!!.count(), 1)


        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTLineDisplay)
        val overline = sub0 as MTLineDisplay
        assertEqualsCGPoint(overline.position, CGPoint(), 0.05f)
        assertTrue(overline.range.equals(NSRange(0, 1)))
        assertFalse(overline.hasScript)
        assertNotNull("testOverline", overline.inner)

        val display2 = overline.inner
        assertEquals(display2.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display2.position.equals(CGPoint()))
        assertTrue(display2.range.equals(NSRange(0, 1)))
        assertFalse(display2.hasScript)
        assertEquals(display2.index, NSNotFound)
        val sd = display2.subDisplays
        assertNotNull("testOverline", subdisplays)
        assertEquals(sd!!.count(), 1)

        val subover = sd[0]
        assertTrue(subover is MTCTLineDisplay)
        val line2 = subover as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 1)
        assertEquals(line2.str, "1")
        assertEqualsCGPoint(line2.position, CGPoint(), 0.05f)
        assertTrue(line2.range.equals(NSRange(0, 1)))
        assertFalse(line2.hasScript)


        // dimensions
        assertEquals(17.32f, display.ascent, 0.05f)
        assertEquals(0.02f, display.descent, 0.05f)
        assertEquals(10f, display.width, 0.05f)

    }

    @Test
    public fun testUnderline() {
        val mathList = MTMathList()
        val under = MTUnderLine()
        val inner = MTMathList()
        val t = MTMathAtom.atomForCharacter('1')
        inner.addAtom(t!!)
        under.innerList = inner
        mathList.addAtom(under)

        val display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("testUnderline", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testOverline", subdisplays)
        assertEquals(subdisplays!!.count(), 1)


        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTLineDisplay)
        val underline = sub0 as MTLineDisplay
        assertEqualsCGPoint(underline.position, CGPoint(), 0.05f)
        assertTrue(underline.range.equals(NSRange(0, 1)))
        assertFalse(underline.hasScript)
        assertNotNull("testUnderline", underline.inner)

        val display2 = underline.inner
        assertEquals(display2.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display2.position.equals(CGPoint()))
        assertTrue(display2.range.equals(NSRange(0, 1)))
        assertFalse(display2.hasScript)
        assertEquals(display2.index, NSNotFound)
        val sd = display2.subDisplays
        assertNotNull("testUnderline", subdisplays)
        assertEquals(sd!!.count(), 1)

        val subover = sd[0]
        assertTrue(subover is MTCTLineDisplay)
        val line2 = subover as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 1)
        assertEquals(line2.str, "1")
        assertEqualsCGPoint(line2.position, CGPoint(), 0.05f)
        assertTrue(line2.range.equals(NSRange(0, 1)))
        assertFalse(line2.hasScript)


        // dimensions
        assertEquals(13.32f, display.ascent, 0.05f)
        assertEquals(4.02f, display.descent, 0.05f)
        assertEquals(10f, display.width, 0.05f)

    }


    @Test
    public fun testSpacing() {
        val mathList = MTMathList()
        mathList.addAtom(MTMathAtom.atomForCharacter('x')!!)
        mathList.addAtom(MTMathSpace(9.0f)!!)
        mathList.addAtom(MTMathAtom.atomForCharacter('y')!!)

        val display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("testSpacing", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 3)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testSpacing", subdisplays)
        assertEquals(2, subdisplays!!.count())

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTCTLineDisplay)
        val line = sub0 as MTCTLineDisplay
        assertEquals(1, line.atoms.count())
        assertEquals(line.str, "𝑥") // The x is italicized
        assertEqualsCGPoint(line.position, CGPoint(), 0.05f)
        assertTrue(line.range.equals(NSRange(0, 1)))
        assertFalse(line.hasScript)

        val sub1: MTDisplay = subdisplays[1]
        assertTrue(sub1 is MTCTLineDisplay)
        val line2 = sub1 as MTCTLineDisplay
        assertEquals(1, line2.atoms.count())
        assertEquals(line2.str, "𝑦") // The y is italicized
        assertEqualsCGPoint(line2.position, CGPoint(21.44f, 0f), 0.05f)
        assertTrue(line2.range.equals(NSRange(2, 1)))
        assertFalse(line2.hasScript)

        val noSpace = MTMathList()
        noSpace.addAtom(MTMathAtom.atomForCharacter('x')!!)
        noSpace.addAtom(MTMathAtom.atomForCharacter('y')!!)

        val noSpaceDisplay = MTTypesetter.createLineForMathList(noSpace, font!!, MTLineStyle.KMTLineStyleDisplay)

        // dimensions
        assertEquals(noSpaceDisplay.ascent, display.ascent, 0.05f)
        assertEquals(noSpaceDisplay.descent, display.descent, 0.05f)
        assertEquals(noSpaceDisplay.width + 10, display.width, 0.05f)

    }

    // For issue: https://github.com/kostub/iosMath/issues/5
    @Test
    public fun testLargeRadicalDescent() {
        val mathList = MTMathListBuilder.buildFromString("\\sqrt{\\frac{\\sqrt{\\frac{1}{2}} + 3}{\\sqrt{5}^x}}")
        assertNotNull("testLargeRadicalDescent", mathList)
        val display = MTTypesetter.createLineForMathList(mathList!!, font!!, MTLineStyle.KMTLineStyleDisplay)

        // dimensions
        assertEquals(49.18f, display.ascent, 0.05f)
        assertEquals(21.308f, display.descent, 0.05f)
        assertEquals(82.569f, display.width, 0.05f)

    }


    @Test
    public fun testMathTable() {
        val c00 = MTMathAtom.mathListForCharacters("1")
        val c01 = MTMathAtom.mathListForCharacters("y+z")
        val c02 = MTMathAtom.mathListForCharacters("y")

        val c11 = MTMathList()
        c11.addAtom(MTMathAtom.fractionWithNumerator("1", "2x")!!)
        val c12 = MTMathAtom.mathListForCharacters("x-y")

        val c20 = MTMathAtom.mathListForCharacters("x+5")
        val c22 = MTMathAtom.mathListForCharacters("12")


        val table = MTMathTable(null)
        table.setCell(c00, 0, 0)
        table.setCell(c01, 0, 1)
        table.setCell(c02, 0, 2)
        table.setCell(c11, 1, 1)
        table.setCell(c12, 1, 2)
        table.setCell(c20, 2, 0)
        table.setCell(c22, 2, 2)

        // alignments
        table.setAlignment(MTColumnAlignment.KMTColumnAlignmentRight, 0)
        table.setAlignment(MTColumnAlignment.KMTColumnAlignmentLeft, 2)

        table.interColumnSpacing = 18f; // 1 quad

        val mathList = MTMathList()
        mathList.addAtom(table)

        val display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("testMathTable", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testMathTable", subdisplays)
        assertEquals(subdisplays!!.count(), 1)

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTMathListDisplay)
        val display2 = sub0 as MTMathListDisplay
        assertEquals(display2.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display2.position.equals(CGPoint()))
        assertTrue(display2.range.equals(NSRange(0, 1)))
        assertFalse(display2.hasScript)
        assertEquals(display2.index, NSNotFound)
        val sd = display2.subDisplays
        assertNotNull("testMathTable", sd)
        assertEquals(sd!!.count(), 3)

        val rowPos = arrayOf(30.31f, -2.67f, -31.95f)
        // alignment is right, center, left.
        val cellPos = arrayOf(arrayOf(35.89f, 65.89f, 129.438f), arrayOf(45.89f, 76.94f, 129.438f), arrayOf(0f, 87.66f, 129.438f))
        // check the 3 rows of the matrix
        for (i in 0 until 3) {
            val sub0i = sd[i];
            assertTrue(sub0i is MTMathListDisplay)

            val row = sub0i as MTMathListDisplay
            assertEquals(row.type, MTLinePosition.KMTLinePositionRegular);
            assertEqualsCGPoint(row.position, CGPoint(0f, rowPos[i]), 0.05f)
            assertTrue(row.range.equals(NSRange(0, 3)))
            assertFalse(row.hasScript);
            assertEquals(row.index, NSNotFound);
            val rsd = row.subDisplays
            assertNotNull("testMathTable", rsd)
            assertEquals(rsd!!.count(), 3);

            for (j in 0 until 3) {
                val sub0ij = rsd[j]
                assertTrue(sub0ij is MTMathListDisplay)
                val col = sub0ij as MTMathListDisplay
                assertEquals(col.type, MTLinePosition.KMTLinePositionRegular);
                assertEqualsCGPoint(col.position, CGPoint(cellPos[i][j], 0f), 0.05f)
                assertFalse(col.hasScript);
                assertEquals(col.index, NSNotFound);
            }
        }
    }

    @Test
    public fun testLatexSymbols() {
        // Test all latex symbols
        val allSymbols = MTMathAtom.supportedLatexSymbolNames()
        //val allSymbols = arrayOf("delta")
        for (symName in allSymbols) {
            val atom = MTMathAtom.atomForLatexSymbolName(symName)
            assertNotNull("testLatexSymbols", atom)

            if (atom!!.type >= MTMathAtomType.KMTMathAtomBoundary) {
                // Skip these types as they aren't symbols.
                continue;
            }

            val list = MTMathList()
            list.addAtom(atom)

            val display = MTTypesetter.createLineForMathList(list, font!!, MTLineStyle.KMTLineStyleDisplay)
            assertNotNull("Symbol $symName", display)
            assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
            assertTrue(display.position.equals(CGPoint()))
            assertTrue(display.range.equals(NSRange(0, 1)))
            assertFalse(display.hasScript)
            assertEquals(display.index, NSNotFound)
            val subdisplays = display.subDisplays
            assertNotNull("Symbol $symName", subdisplays)
            assertEquals(subdisplays!!.count(), 1)

            val sub0: MTDisplay = subdisplays[0]
            if (atom.type == MTMathAtomType.KMTMathAtomLargeOperator && atom.nucleus.length == 1) {

                assertTrue(sub0 is MTGlyphDisplay)
                val glyph = sub0 as MTGlyphDisplay
                assertTrue(glyph.position.equals(CGPoint()))
                assertTrue(glyph.range.equals(NSRange(0, 1)))
                assertFalse(glyph.hasScript)
            } else {
                assertTrue(sub0 is MTCTLineDisplay)
                val line = sub0 as MTCTLineDisplay
                assertEquals(line.atoms.count(), 1)
                if (atom.type != MTMathAtomType.KMTMathAtomVariable) {
                    assertEquals(line.str, atom.nucleus)
                }
                assertEqualsCGPoint(line.position, CGPoint(), 0.05f)
                assertTrue(line.range.equals(NSRange(0, 1)))
                assertFalse(line.hasScript)
            }


            // dimensions
            assertEquals(display.ascent, sub0.ascent);
            assertEquals(display.descent, sub0.descent);
            assertEquals(display.width, sub0.width);

            // All chars will occupy some space.
            if (atom.nucleus != " ") {
                // all chars except space have height
                assertTrue(display.ascent + display.descent > 0);
            }
            // all chars have a width.
            assertTrue(display.width > 0);
        }
    }

    fun dumpstr(s: String) {
        val ca = s.toCharArray()
        val cp = Character.codePointAt(ca, 0)
        println("str $s codepoint $cp")
        for (c in ca) {
            println("c $c")
        }
    }

    fun testAtomWithAllFontStyles(atom: MTMathAtom) {
        val fontStyles = arrayOf(KMTFontStyleDefault, KMTFontStyleRoman, KMTFontStyleBold,
                KMTFontStyleCaligraphic, KMTFontStyleTypewriter, KMTFontStyleItalic,
                KMTFontStyleSansSerif, KMTFontStyleFraktur, KMTFontStyleBlackboard, KMTFontStyleBoldItalic)



        for (fontStyle in fontStyles) {
            val acopy = atom.copyDeep()
            acopy.fontStyle = fontStyle
            val list = MTMathList(acopy)

            val display = MTTypesetter.createLineForMathList(list, font!!, MTLineStyle.KMTLineStyleDisplay)
            assertNotNull("Symbol ${atom.nucleus}", display)
            assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
            assertTrue(display.position.equals(CGPoint()))
            assertTrue(display.range.equals(NSRange(0, 1)))
            assertFalse(display.hasScript)
            assertEquals(display.index, NSNotFound)
            val subdisplays = display.subDisplays
            assertNotNull("Symbol ${atom.nucleus}", subdisplays)
            assertEquals(subdisplays!!.count(), 1)

            val sub0: MTDisplay = subdisplays[0]
            assertTrue(sub0 is MTCTLineDisplay)
            val line = sub0 as MTCTLineDisplay
            assertEquals(line.atoms.count(), 1)
            if (atom.type != MTMathAtomType.KMTMathAtomVariable && atom.type != MTMathAtomType.KMTMathAtomNumber) {
                // numbers will get bolded and not matched
                if (line.str != atom.nucleus) {
                    dumpstr(line.str)
                    dumpstr(atom.nucleus)
                    println("$atom")
                }
                assertEquals(line.str, atom.nucleus)
            }
            assertEqualsCGPoint(line.position, CGPoint(), 0.05f)
            assertTrue(line.range.equals(NSRange(0, 1)))
            assertFalse(line.hasScript)


            // dimensions
            assertEquals(display.ascent, sub0.ascent);
            assertEquals(display.descent, sub0.descent);
            assertEquals(display.width, sub0.width);

            // All chars will occupy some space.
            /* Some of these passed in iOS tests but I believe this is only because they were substituted by placeholders
               KMTFontStyleCaligraphic
                   return MTCodepointChar(0x212F)   // Script e (Natural exponent)
               There isn't a glyph for this codepoint in the default font
               on iOS this get measured as 2512 decimal a box
             */
            if (atom.nucleus != " " && fontStyle != KMTFontStyleCaligraphic) {
                // all chars except space have height
                if ((display.ascent + display.descent) <= 0) {
                    println("Failed $atom")
                    dumpstr(atom.nucleus)
                }
                assertTrue((display.ascent + display.descent) > 0)
            }
            // all chars have a width.
            assertTrue(display.width > 0);
        }
    }

    @Test
    public fun testVariables() {
        // Test all latex symbols
        val allSymbols = MTMathAtom.supportedLatexSymbolNames()
        for (symName in allSymbols) {
            val atom = MTMathAtom.atomForLatexSymbolName(symName)
            assertNotNull("testLatexSymbols", atom)

            if (atom!!.type != MTMathAtomType.KMTMathAtomVariable) {
                // Skip these types as we are only interested in variables.
                continue
            }
            this.testAtomWithAllFontStyles(atom)
        }
        val alphaNum = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789."
        val mathList = MTMathAtom.mathListForCharacters(alphaNum)
        for (atom in mathList.atoms) {
            this.testAtomWithAllFontStyles(atom)
        }
    }

    @Test
    public fun testStyleChanges() {
        val frac = MTMathAtom.fractionWithNumerator("1", "2")
        val list = MTMathList(frac)
        val style = MTMathStyle(MTLineStyle.KMTLineStyleText)
        val textList = MTMathList(style, frac)

        // This should make the display same as text.
        val display = MTTypesetter.createLineForMathList(textList, font!!, MTLineStyle.KMTLineStyleDisplay)
        val textDisplay = MTTypesetter.createLineForMathList(list, font!!, MTLineStyle.KMTLineStyleText)
        val originalDisplay = MTTypesetter.createLineForMathList(list, font!!, MTLineStyle.KMTLineStyleDisplay)

        // Display should be the same as rendering the fraction in text style.
        assertEquals(display.ascent, textDisplay.ascent)
        assertEquals(display.descent, textDisplay.descent)
        assertEquals(display.width, textDisplay.width)

        // Original display should be larger than display since it is greater.
        assertTrue(originalDisplay.ascent > display.ascent)
        assertTrue(originalDisplay.descent > display.descent)
        assertTrue(originalDisplay.width > display.width)
    }

    @Test
    public fun testStyleMiddle() {
        val atom1 = MTMathAtom.atomForCharacter('x')
        val style1 = MTMathStyle(MTLineStyle.KMTLineStyleScript)
        val atom2 = MTMathAtom.atomForCharacter('y')
        val style2 = MTMathStyle(MTLineStyle.KMTLineStyleScriptScript)
        val atom3 = MTMathAtom.atomForCharacter('z')
        val list = MTMathList(atom1!!, style1, atom2!!, style2, atom3!!)

        val display = MTTypesetter.createLineForMathList(list, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("testStyleMiddle", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 5)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testStyleMiddle", subdisplays)
        assertEquals(subdisplays!!.count(), 3)

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTCTLineDisplay)
        val line = sub0 as MTCTLineDisplay
        assertEquals(line.atoms.count(), 1)
        assertEquals(line.str, "𝑥") // The x is italicized
        assertEqualsCGPoint(line.position, CGPoint(), 0.05f)
        assertTrue(line.range.equals(NSRange(0, 1)))
        assertFalse(line.hasScript)

        val sub1: MTDisplay = subdisplays[1]
        assertTrue(sub1 is MTCTLineDisplay)
        val line1 = sub1 as MTCTLineDisplay
        assertEquals(line1.atoms.count(), 1)
        assertEquals(line1.str, "𝑦") // The y is italicized
        assertTrue(line1.range.equals(NSRange(2, 1)))
        assertFalse(line1.hasScript)

        val sub2: MTDisplay = subdisplays[2]
        assertTrue(sub2 is MTCTLineDisplay)
        val line2 = sub2 as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 1)
        assertEquals(line2.str, "𝑧") // The z is italicized
        assertTrue(line2.range.equals(NSRange(4, 1)))
        assertFalse(line2.hasScript)
    }

    @Test
    public fun testAccent() {
        val mathList = MTMathList()
        val accent = MTMathAtom.accentWithName("hat")
        assertNotNull("testAccent", accent)
        val inner = MTMathList()
        inner.addAtom(MTMathAtom.atomForCharacter('x')!!)
        accent!!.innerList = inner;
        mathList.addAtom(accent as MTMathAtom)

        val display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("testAccent", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testAccent", subdisplays)
        assertEquals(subdisplays!!.count(), 1)

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTAccentDisplay)
        val accentDisp = sub0 as MTAccentDisplay
        assertTrue(accentDisp.range.equals(NSRange(0, 1)))
        assertFalse(accentDisp.hasScript)
        assertEqualsCGPoint(accentDisp.position, CGPoint(), 0.05f)

        assertNotNull(accentDisp.accentee)
        assertNotNull(accentDisp.accent)

        val display2 = accentDisp.accentee
        assertEquals(display2.type, MTLinePosition.KMTLinePositionRegular)
        assertEqualsCGPoint(display2.position, CGPoint(), 0.05f)
        assertTrue(display2.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val sd = display2.subDisplays
        assertNotNull(sd)
        assertEquals(sd!!.count(), 1)

        val subaccentee = sd[0]
        assertTrue(subaccentee is MTCTLineDisplay)
        val line2 = subaccentee as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 1)
        assertEquals(line2.str, "𝑥") // The x is italicized
        assertEqualsCGPoint(line2.position, CGPoint(), 0.05f)
        assertTrue(line2.range.equals(NSRange(0, 1)))
        assertFalse(line2.hasScript)


        val glyph = accentDisp.accent;
        assertEqualsCGPoint(glyph.position, CGPoint(11.86f, 0f), 0.05f)
        assertTrue(glyph.range.equals(NSRange(0, 1)))
        assertFalse(glyph.hasScript)

        // dimensions
        assertEquals(14.68f, display.ascent, 0.05f);
        assertEquals(0.24f, display.descent, 0.05f);
        assertEquals(11.44f, display.width, 0.05f)
    }

    @Test
    public fun testWideAccent() {
        val mathList = MTMathList()
        val accent = MTMathAtom.accentWithName("hat")
        assertNotNull("testAccent", accent)
        accent!!.innerList = MTMathAtom.mathListForCharacters("xyzw")
        mathList.addAtom(accent as MTMathAtom)

        val display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("testWideAccent", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val subdisplays = display.subDisplays
        assertNotNull("testAccent", subdisplays)
        assertEquals(subdisplays!!.count(), 1)

        val sub0: MTDisplay = subdisplays[0]
        assertTrue(sub0 is MTAccentDisplay)
        val accentDisp = sub0 as MTAccentDisplay
        assertTrue(accentDisp.range.equals(NSRange(0, 1)))
        assertFalse(accentDisp.hasScript)
        assertEqualsCGPoint(accentDisp.position, CGPoint(), 0.05f)

        assertNotNull(accentDisp.accentee)
        assertNotNull(accentDisp.accent)

        val display2 = accentDisp.accentee
        assertEquals(display2.type, MTLinePosition.KMTLinePositionRegular)
        assertEqualsCGPoint(display2.position, CGPoint(), 0.05f)
        assertTrue(display2.range.equals(NSRange(0, 4)))
        assertFalse(display.hasScript)
        assertEquals(display.index, NSNotFound)
        val sd = display2.subDisplays
        assertNotNull(sd)
        assertEquals(sd!!.count(), 1)

        val subaccentee = sd[0]
        assertTrue(subaccentee is MTCTLineDisplay)
        val line2 = subaccentee as MTCTLineDisplay
        assertEquals(line2.atoms.count(), 4)
        assertEquals(line2.str, "𝑥𝑦𝑧𝑤") // The string is italicized
        assertEqualsCGPoint(line2.position, CGPoint(), 0.05f)
        assertTrue(line2.range.equals(NSRange(0, 4)))
        assertFalse(line2.hasScript)


        val glyph = accentDisp.accent;
        assertEqualsCGPoint(glyph.position, CGPoint(3.47f, 0f), 0.05f)
        assertTrue(glyph.range.equals(NSRange(0, 1)))
        assertFalse(glyph.hasScript)

        // dimensions
        assertEquals(14.98f, display.ascent, 0.05f);
        assertEquals(4.12f, display.descent, 0.05f);
        assertEquals(44.86f, display.width, 0.05f)
    }
}