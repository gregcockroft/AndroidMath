package com.agog.mathdisplay;

import android.Manifest
import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.agog.mathdisplay.parse.*
import com.agog.mathdisplay.render.*
import com.agog.mathdisplay.render.MTTypesetter

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before;
import org.junit.Assert.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Environment
import java.io.FileOutputStream
import java.io.IOException
import android.support.test.rule.GrantPermissionRule
import com.agog.mathdisplay.render.MTCodepointChar
import org.junit.Rule


const val BITMAPWIDTH = 640
const val BITMAPHEIGHT = 480

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4::class)
public class DrawInstrumentedTest {

    private var context: Context? = null
    private var font: MTFont? = null
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null

    @Rule
    @JvmField
    var mRuntimePermissionWriteRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    @Rule
    @JvmField
    var mRuntimePermissionReadRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE)

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getContext();

        assertNotNull(context)
        MTFontManager.setContext(context!!)
        font = MTFontManager.defaultFont()

        bitmap = Bitmap.createBitmap(BITMAPWIDTH, BITMAPHEIGHT, Bitmap.Config.ARGB_8888)
        assertNotNull(bitmap)
        canvas = Canvas(bitmap!!)
        assertNotNull(canvas)
        canvas!!.translate(0.0f, BITMAPHEIGHT.toFloat())
        canvas!!.scale(1.0f, -1.0f)

        canvas!!.translate(100.0f, 100.0f) // We shift this to catch any coordinate system errors
        // Show coordinate system
        val strokePaint = Paint()
        strokePaint.setColor(Color.GREEN)
        canvas!!.drawLine(0.0f, 0.0f, 0.0f, 100.0f, strokePaint)
        canvas!!.drawLine(0.0f, 0.0f, 100.0f, 0f, strokePaint)


    }


    fun savebitmap(filename: String) {
        assertNotNull(context)
        assertNotNull(bitmap)

        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + filename

        FileOutputStream(path).use {
            try {
                bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, it) // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    if (it != null) {
                        it!!.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    @Test
    public fun testRandom() {
        val l = """\vec \bf V_1 \times \vec \bf V_2 =  \begin{vmatrix}
    \hat \imath &\hat \jmath &\hat k \\
    \frac{\partial X}{\partial u} &  \frac{\partial Y}{\partial u} & 0 \\
    \frac{\partial X}{\partial v} &  \frac{\partial Y}{\partial v} & 0
    \end{vmatrix}"""

        var cp = 0x1d715
        var g = MTCodepointChar(cp)
        var str = g.toUnicodeString()
        println("str $str")
        val e: MTParseError = MTParseError()

        //@"imath" : [MTMathAtom atomWithType:KMTMathAtomOrdinary value:@"\U0001D6A4"],
        //@"jmath" : [MTMathAtom atomWithType:KMTMathAtomOrdinary value:@"\U0001D6A5"],
        cp = 0x1D6A4
        g = MTCodepointChar(cp)
        str = g.toUnicodeString()
        println("str $str")

        cp = 0x1D6A5
        g = MTCodepointChar(cp)
        str = g.toUnicodeString()
        println("str $str")


        val list: MTMathList? = MTMathListBuilder.buildFromString(l, e)
        assertEquals(e.errorcode, MTParseErrors.ErrorNone)
        val desc = "Error for string:$l"


        val mathList = MTMathListBuilder.buildFromString(l)
        assertNotNull(mathList)
        // convert it back to latex
        if (mathList != null) {
            val latex: String = MTMathListBuilder.toLatexString(mathList)
            println("string converted back $latex")

            val display = MTTypesetter.createLineForMathList(mathList!!, font!!, MTLineStyle.KMTLineStyleDisplay)

            assertNotNull(display)

            display.draw(canvas!!)
            savebitmap("testRandom.png")
        }
    }


    @Test
    public fun testSimpleVariable() {
        var mathList = MTMathList()
        var atom = MTMathAtom.atomForCharacter('x')
        assertNotNull("atomForCharacter", atom)
        if (atom != null) {
            mathList.addAtom(atom)
            var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
            assertNotNull(display)
            assertEquals(display.type, MTLinePosition.KMTLinePositionRegular);
            assertTrue(display.position.equals(CGPoint()))
            assertTrue(display.range.equals(NSRange(0, 1)))
            assertFalse(display.hasScript);
            assertEquals(display.index, NSNotFound);

            assertNotNull(canvas)
            display.draw(canvas!!)
            savebitmap("testSimpleVariable.png")
        }
    }


    @Test
    public fun testMultipleVariables() {
        val mathList = MTMathAtom.mathListForCharacters("xyzw")

        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("createLineForMathList", display)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 4)))
        assertFalse(display.hasScript);
        assertEquals(display.index, NSNotFound);

        assertNotNull(canvas)
        display.draw(canvas!!)
        savebitmap("testMultipleVariables.png")

    }


    @Test
    public fun testEquationWithOperatorsAndRelations() {
        val mathList = MTMathAtom.mathListForCharacters("2x+3=y")

        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("createLineForMathList", display)
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 6)))
        assertFalse(display.hasScript);
        assertEquals(display.index, NSNotFound);

        assertNotNull(canvas)
        display.draw(canvas!!)
        savebitmap("testEquationWithOperatorsAndRelations.png")
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

        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("createLineForMathList", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular);
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript);
        assertEquals(display.index, NSNotFound);

        assertNotNull(canvas)
        display.draw(canvas!!)
        savebitmap("testSuperscript.png")
    }

    @Test
    public fun testSubscript() {
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
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular);
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript);
        assertEquals(display.index, NSNotFound);

        assertNotNull(canvas)
        display.draw(canvas!!)
        savebitmap("testSubscript.png")
    }

    @Test
    public fun testSupersubscript() {
        val mathList = MTMathList()
        val x = MTMathAtom.atomForCharacter('x')
        assertNotNull("testSuperscript", x)
        val supersc = MTMathList()
        val s = MTMathAtom.atomForCharacter('2')
        assertNotNull("testSuperscript", s)
        supersc.addAtom(s!!)
        val subsc = MTMathList()
        val t = MTMathAtom.atomForCharacter('1')
        assertNotNull("testSuperscript", t)
        subsc.addAtom(t!!)
        x!!.subScript = subsc
        x!!.superScript = supersc
        mathList.addAtom(x)

        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("createLineForMathList", display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular);
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript);
        assertEquals(display.index, NSNotFound);

        assertNotNull(canvas)
        display.draw(canvas!!)
        savebitmap("testSupersubscript.png")

    }

    @Test
    public fun testRadical() {
        val mathList = MTMathList()
        val rad = MTRadical()
        val radicand = MTMathList()

        val t = MTMathAtom.atomForCharacter('1')
        assertNotNull(t)
        radicand.addAtom(t!!)
        rad.radicand = radicand;
        mathList.addAtom(rad)


        var display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull(display)
        assertEquals(display.type, MTLinePosition.KMTLinePositionRegular);
        assertTrue(display.position.equals(CGPoint()))
        assertTrue(display.range.equals(NSRange(0, 1)))
        assertFalse(display.hasScript);
        assertEquals(display.index, NSNotFound);
        assertNotNull(canvas)
        display.draw(canvas!!)
        savebitmap("testRadical.png")

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
        assertNotNull(display)

        display.draw(canvas!!)
        savebitmap("testRadicalWithDegree.png")
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
        assertNotNull(display)

        display.draw(canvas!!)
        savebitmap("testFraction.png")
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
        display.draw(canvas!!)
        savebitmap("testAtop.png")
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
        display.draw(canvas!!)
        savebitmap("testBinomial.png")
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
        display.draw(canvas!!)
        savebitmap("testLargeOpNoLimitsText.png")
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
        display.draw(canvas!!)
        savebitmap("testLargeOpNoLimitsSymbol.png")

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
        display.draw(canvas!!)
        savebitmap("testLargeOpNoLimitsSymbolWithScripts.png")
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
        display.draw(canvas!!)
        savebitmap("testLargeOpWithLimitsTextWithScripts.png")
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
        display.draw(canvas!!)
        savebitmap("testLargeOpWithLimitsSymbolWithScripts.png")
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
        display.draw(canvas!!)
        savebitmap("testInner.png")
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
        display.draw(canvas!!)
        savebitmap("testOverline.png")
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
        display.draw(canvas!!)
        savebitmap("testUnderline.png")
    }

    @Test
    public fun testSpacing() {
        val mathList = MTMathList()
        mathList.addAtom(MTMathAtom.atomForCharacter('x')!!)
        mathList.addAtom(MTMathSpace(9.0f)!!)
        mathList.addAtom(MTMathAtom.atomForCharacter('y')!!)

        val display = MTTypesetter.createLineForMathList(mathList, font!!, MTLineStyle.KMTLineStyleDisplay)
        assertNotNull("testSpacing", display)
        display.draw(canvas!!)
        savebitmap("testSpacing.png")
    }


    @Test
    public fun testLargeRadicalDescent() {
        val mathList = MTMathListBuilder.buildFromString("\\sqrt{\\frac{\\sqrt{\\frac{1}{2}} + 3}{\\sqrt{5}^x}}")
        assertNotNull("testLargeRadicalDescent", mathList)
        val display = MTTypesetter.createLineForMathList(mathList!!, font!!, MTLineStyle.KMTLineStyleDisplay)

        assertNotNull(display)

        display.draw(canvas!!)
        savebitmap("testLargeRadicalDescent.png")

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
        display.draw(canvas!!)
        savebitmap("testMathTable.png")

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
        display.draw(canvas!!)
        savebitmap("testAccent.png")
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
        display.draw(canvas!!)
        savebitmap("testWideAccent.png")
    }


}


