package com.agog.mathdisplay;

import com.agog.mathdisplay.parse.MTMathAtomType.*
import org.junit.Test
import org.junit.Assert.*
import com.agog.mathdisplay.parse.*


/**
 *   Unit Tests for the parsing code and MTMathList classes
 *
 *
 */
public class BuilderUnitTest {

    data class BuildTest(val srcLaTex: String, val atomTypeArray: Array<MTMathAtomType>, val retLaTex: String)

    var testBuilderTestData: Array<BuildTest> = arrayOf(
            BuildTest("x", arrayOf(KMTMathAtomVariable), "x"),
            BuildTest("1", arrayOf(KMTMathAtomNumber), "1"),
            BuildTest("*", arrayOf(KMTMathAtomBinaryOperator), "*"),
            BuildTest("+", arrayOf(KMTMathAtomBinaryOperator), "+"),
            BuildTest(".", arrayOf(KMTMathAtomNumber), "."),
            BuildTest("(", arrayOf(KMTMathAtomOpen), "("),
            BuildTest(")", arrayOf(KMTMathAtomClose), ")"),
            BuildTest(",", arrayOf(KMTMathAtomPunctuation), ","),
            BuildTest("!", arrayOf(KMTMathAtomClose), "!"),
            BuildTest("x+2", arrayOf(KMTMathAtomVariable, KMTMathAtomBinaryOperator, KMTMathAtomNumber), "x+2"),
            // spaces are ignored
            BuildTest("(2.3 * 8)",
                    arrayOf(KMTMathAtomOpen, KMTMathAtomNumber, KMTMathAtomNumber, KMTMathAtomNumber,
                            KMTMathAtomBinaryOperator, KMTMathAtomNumber, KMTMathAtomClose), "(2.3*8)"),
            // braces are just for grouping
            BuildTest("5{3+4}", arrayOf(KMTMathAtomNumber, KMTMathAtomNumber, KMTMathAtomBinaryOperator, KMTMathAtomNumber), "53+4"),
            // commands
            BuildTest("\\pi+\\theta\\geq 3", arrayOf(KMTMathAtomVariable, KMTMathAtomBinaryOperator, KMTMathAtomVariable, KMTMathAtomRelation, KMTMathAtomNumber),
                    "\\pi +\\theta \\geq 3"),
            // aliases
            BuildTest("\\pi\\ne 5 \\land 3", arrayOf(KMTMathAtomVariable, KMTMathAtomRelation, KMTMathAtomNumber, KMTMathAtomBinaryOperator, KMTMathAtomNumber),
                    "\\pi \\neq 5\\wedge 3"),
            // control space
            BuildTest("x \\ y", arrayOf(KMTMathAtomVariable, KMTMathAtomOrdinary, KMTMathAtomVariable), "x\\  y"),
            // spacing
            BuildTest("x \\quad y \\; z \\! q", arrayOf(KMTMathAtomVariable, KMTMathAtomSpace, KMTMathAtomVariable, KMTMathAtomSpace, KMTMathAtomVariable, KMTMathAtomSpace,
                    KMTMathAtomVariable), "x\\quad y\\; z\\! q")
    )


    fun dumptypes(atoms: MutableList<MTMathAtom>, types: Array<MTMathAtomType>) {
        println("atoms types " + atoms.size)
        for (atom in atoms) {
            println("   " + atom.type)
        }
        println("needed types " + types.size)
        for (type in types) {
            println("   $type")
        }
    }

    // Verify the list of atom types in the MTMathList matches the types array
    fun checkAtomTypes(list: MTMathList, types: Array<MTMathAtomType>, desc: String) {
        //assertNotNull(desc,list?.atoms)
        val atoms: MutableList<MTMathAtom> = list?.atoms

        if (atoms.count() != types.count()) {
            dumptypes(atoms, types)
        }
        assertEquals(desc, atoms.count(), types.count())
        for (i in 0 until types.count()) {
            val atom: MTMathAtom = atoms[i];
            if (atom.type != types[i]) {
                dumptypes(atoms, types)
            }
            assertEquals(desc, atom.type, types[i])
        }
    }


    // Parse some simple math latex strings and verify they were parsed into correct objects and test the resulting conversions back to a string
    @Test
    fun testBuilder() {
        println("In testBuilder")
        for (test in testBuilderTestData) {
            println("on test " + test.srcLaTex)

            val builder: MTMathListBuilder? = MTMathListBuilder(test.srcLaTex)
            val e: MTParseError = MTParseError()

            val list: MTMathList? = MTMathListBuilder.buildFromString(test.srcLaTex, e)
            assertEquals(MTParseErrors.ErrorNone, e.errorcode)
            val desc = "Error for string:$test.srcLaTex"
            assertNotNull(desc, list)
            if (list != null) {
                checkAtomTypes(list, test.atomTypeArray, desc)

                // convert it back to latex
                val latex: String = MTMathListBuilder.toLatexString(list)
                assertEquals(desc, test.retLaTex, latex)
                println(" returned $latex")
            }
        }
    }

    data class SuperSuperTest(val srcLaTex: String, val atomTypeArray: Array<MTMathAtomType>, val superTypeArray: Array<MTMathAtomType>, val supersuperTypeArray: Array<MTMathAtomType>?, val retLaTex: String)

    var testSuperScriptTestData: Array<SuperSuperTest> = arrayOf(
            SuperSuperTest("x^2", arrayOf(KMTMathAtomVariable), arrayOf(KMTMathAtomNumber), null, "x^{2}"),
            SuperSuperTest("x^23", arrayOf(KMTMathAtomVariable, KMTMathAtomNumber), arrayOf(KMTMathAtomNumber), null, "x^{2}3"),
            SuperSuperTest("x^{23}", arrayOf(KMTMathAtomVariable), arrayOf(KMTMathAtomNumber, KMTMathAtomNumber), null, "x^{23}"),
            SuperSuperTest("x^2^3", arrayOf(KMTMathAtomVariable, KMTMathAtomOrdinary), arrayOf(KMTMathAtomNumber), null, "x^{2}{}^{3}"),


            SuperSuperTest("^2", arrayOf(KMTMathAtomOrdinary), arrayOf(KMTMathAtomNumber), null, "{}^{2}"),
            SuperSuperTest("{}^2", arrayOf(KMTMathAtomOrdinary), arrayOf(KMTMathAtomNumber), null, "{}^{2}"),
            SuperSuperTest("x^^2", arrayOf(KMTMathAtomVariable, KMTMathAtomOrdinary), arrayOf(), null, "x^{}{}^{2}"),
            SuperSuperTest("5{x}^2", arrayOf(KMTMathAtomNumber, KMTMathAtomVariable), arrayOf(), null, "5x^{2}"),

            SuperSuperTest("x^{2^3}", arrayOf(KMTMathAtomVariable), arrayOf(KMTMathAtomNumber), arrayOf(KMTMathAtomNumber), "x^{2^{3}}"),
            SuperSuperTest("x^{^2*}", arrayOf(KMTMathAtomVariable), arrayOf(KMTMathAtomOrdinary, KMTMathAtomBinaryOperator), arrayOf(KMTMathAtomNumber), "x^{{}^{2}*}"))


    // Parse some simple math latex strings and verify they were parsed into correct objects and test the resulting conversions back to a string
    @Test
    fun testSuperScript() {
        println("In testSuperScript")
        for (test in testSuperScriptTestData) {
            println("on test " + test.srcLaTex + " expecting return of " + test.retLaTex)

            val builder: MTMathListBuilder? = MTMathListBuilder(test.srcLaTex)
            val e: MTParseError = MTParseError()

            val list: MTMathList? = MTMathListBuilder.buildFromString(test.srcLaTex, e)
            assertEquals(e.errorcode, MTParseErrors.ErrorNone)
            val desc = "Error for string:$test.srcLaTex"
            assertNotNull(desc, list)
            if (list != null) {
                checkAtomTypes(list, test.atomTypeArray, desc)

                // get the first atom
                val first: MTMathAtom = list.atoms[0];
                // check it's superscript
                if (test.superTypeArray.count() > 0) {
                    val superlist: MTMathList? = first.superScript;
                    assertNotNull(desc, superlist)
                    if (superlist != null) {
                        checkAtomTypes(superlist, test.superTypeArray, desc)
                        if (test.supersuperTypeArray != null) {
                            // one more level
                            val superFirst: MTMathAtom = superlist.atoms[0];
                            val supersuperList: MTMathList? = superFirst.superScript;
                            assertNotNull(desc, supersuperList)
                            if (supersuperList != null) {
                                checkAtomTypes(supersuperList, test.supersuperTypeArray, desc)
                            }
                        }
                    }

                }
                val latex: String = MTMathListBuilder.toLatexString(list)
                assertEquals(desc, test.retLaTex, latex)
                println(" returned $latex")
            }
        }
    }

    data class SubSubTest(val srcLaTex: String, val atomTypeArray: Array<MTMathAtomType>, val subTypeArray: Array<MTMathAtomType>, val subsubTypeArray: Array<MTMathAtomType>?, val retLaTex: String)

    var testSubScriptTestData: Array<SubSubTest> = arrayOf(
            SubSubTest("x_2", arrayOf(KMTMathAtomVariable), arrayOf(KMTMathAtomNumber), null, "x_{2}"),
            SubSubTest("x_23", arrayOf(KMTMathAtomVariable, KMTMathAtomNumber), arrayOf(KMTMathAtomNumber), null, "x_{2}3"),
            SubSubTest("x_{23}", arrayOf(KMTMathAtomVariable), arrayOf(KMTMathAtomNumber, KMTMathAtomNumber), null, "x_{23}"),
            SubSubTest("x_2_3", arrayOf(KMTMathAtomVariable, KMTMathAtomOrdinary), arrayOf(KMTMathAtomNumber), null, "x_{2}{}_{3}"),
            SubSubTest("x_{2_3}", arrayOf(KMTMathAtomVariable), arrayOf(KMTMathAtomNumber), arrayOf(KMTMathAtomNumber), "x_{2_{3}}"),
            SubSubTest("x_{_2*}", arrayOf(KMTMathAtomVariable), arrayOf(KMTMathAtomOrdinary, KMTMathAtomBinaryOperator), null, "x_{{}_{2}*}"),
            SubSubTest("_2", arrayOf(KMTMathAtomOrdinary), arrayOf(KMTMathAtomNumber), null, "{}_{2}"),
            SubSubTest("{}_2", arrayOf(KMTMathAtomOrdinary), arrayOf(KMTMathAtomNumber), null, "{}_{2}"),
            SubSubTest("x__2", arrayOf(KMTMathAtomVariable, KMTMathAtomOrdinary), arrayOf(), null, "x_{}{}_{2}"),

            SubSubTest("5{x}_2", arrayOf(KMTMathAtomNumber, KMTMathAtomVariable), arrayOf(), null, "5x_{2}"))


    // Parse some simple math latex strings and verify they were parsed into correct objects and test the resulting conversions back to a string
    @Test
    fun testSubScript() {
        println("In testSubScript")
        for (test in testSubScriptTestData) {
            println("on test " + test.srcLaTex + " expecting return of " + test.retLaTex)

            val builder: MTMathListBuilder? = MTMathListBuilder(test.srcLaTex)
            val e: MTParseError = MTParseError()

            val list: MTMathList? = MTMathListBuilder.buildFromString(test.srcLaTex, e)
            assertEquals(e.errorcode, MTParseErrors.ErrorNone)
            val desc = "Error for string:$test.srcLaTex"
            assertNotNull(desc, list)
            if (list != null) {
                checkAtomTypes(list, test.atomTypeArray, desc)

                // get the first atom
                val first: MTMathAtom = list.atoms[0];
                // check it's subscript
                if (test.subTypeArray.count() > 0) {
                    val sublist: MTMathList? = first.subScript;
                    assertNotNull(desc, sublist)
                    if (sublist != null) {
                        checkAtomTypes(sublist, test.subTypeArray, desc)
                        if (test.subsubTypeArray != null) {
                            // one more level
                            val subFirst: MTMathAtom = sublist.atoms[0];
                            val subsubList: MTMathList? = subFirst.subScript;
                            assertNotNull(desc, subsubList)
                            if (subsubList != null) {
                                checkAtomTypes(subsubList, test.subsubTypeArray, desc)
                            }
                        }
                    }

                }
                val latex: String = MTMathListBuilder.toLatexString(list)
                assertEquals(desc, test.retLaTex, latex)
                println(" returned $latex")
            }
        }
    }

    data class SuperSubTest(val srcLaTex: String, val atomTypeArray: Array<MTMathAtomType>, val subTypeArray: Array<MTMathAtomType>, val superTypeArray: Array<MTMathAtomType>, val retLaTex: String)

    var testSuperSubTestData: Array<SuperSubTest> = arrayOf(
            SuperSubTest("x_2^*", arrayOf(KMTMathAtomVariable), arrayOf(KMTMathAtomNumber), arrayOf(KMTMathAtomBinaryOperator), "x^{*}_{2}"),
            SuperSubTest("x^*_2", arrayOf(KMTMathAtomVariable), arrayOf(KMTMathAtomNumber), arrayOf(KMTMathAtomBinaryOperator), "x^{*}_{2}"),
            SuperSubTest("x_^*", arrayOf(KMTMathAtomVariable), arrayOf(), arrayOf(KMTMathAtomBinaryOperator), "x^{*}_{}"),
            SuperSubTest("x^_2", arrayOf(KMTMathAtomVariable), arrayOf(KMTMathAtomNumber), arrayOf(), "x^{}_{2}"),
            SuperSubTest("x_{2^*}", arrayOf(KMTMathAtomVariable), arrayOf(KMTMathAtomNumber), arrayOf(), "x_{2^{*}}"),
            SuperSubTest("x^{*_2}", arrayOf(KMTMathAtomVariable), arrayOf(), arrayOf(KMTMathAtomBinaryOperator), "x^{*_{2}}"),
            SuperSubTest("_2^*", arrayOf(KMTMathAtomOrdinary), arrayOf(KMTMathAtomNumber), arrayOf(KMTMathAtomBinaryOperator), "{}^{*}_{2}"))


    // Parse some simple math latex strings and verify they were parsed into correct objects and test the resulting conversions back to a string
    @Test
    fun testSuperSubScript() {
        println("In testSuperSubScript")
        for (test in testSuperSubTestData) {
            println("on test " + test.srcLaTex + " expecting return of " + test.retLaTex)

            val builder: MTMathListBuilder? = MTMathListBuilder(test.srcLaTex)
            val e: MTParseError = MTParseError()

            val list: MTMathList? = MTMathListBuilder.buildFromString(test.srcLaTex, e)
            assertEquals(e.errorcode, MTParseErrors.ErrorNone)
            val desc = "Error for string:$test.srcLaTex"
            assertNotNull(desc, list)
            if (list != null) {
                checkAtomTypes(list, test.atomTypeArray, desc)

                // get the first atom
                val first: MTMathAtom = list.atoms[0];
                // check it's subscript
                if (test.subTypeArray.count() > 0) {
                    val sublist: MTMathList? = first.subScript;
                    assertNotNull(desc, sublist)
                    if (sublist != null) {
                        checkAtomTypes(sublist, test.subTypeArray, desc)
                    }
                }
                // check it's superscript
                if (test.superTypeArray.count() > 0) {
                    val superlist: MTMathList? = first.superScript;
                    assertNotNull(desc, superlist)
                    if (superlist != null) {
                        checkAtomTypes(superlist, test.superTypeArray, desc)
                    }
                }
                val latex: String = MTMathListBuilder.toLatexString(list)
                assertEquals(desc, test.retLaTex, latex)
                println(" returned $latex")
            }
        }
    }

    @Test
    fun testSymbols() {
        val str = "5\\times3^{2\\div2}"
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        println("In testSymbols")
        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 3)
            var atom: MTMathAtom = list.atoms[0];
            assertEquals(desc, atom.type, KMTMathAtomNumber)
            assertEquals(desc, atom.nucleus, "5")
            atom = list.atoms[1];
            assertEquals(desc, atom.type, KMTMathAtomBinaryOperator)
            assertEquals(desc, atom.nucleus, "\u00D7")
            atom = list.atoms[2];
            assertEquals(desc, atom.type, KMTMathAtomNumber)
            assertEquals(desc, atom.nucleus, "3")

            // super script
            val superList: MTMathList? = atom.superScript;
            assertNotNull(desc, superList)
            if (superList != null) {
                assertEquals(desc, superList.atoms.count(), 3)
                atom = superList.atoms[0];
                assertEquals(desc, atom.type, KMTMathAtomNumber)
                assertEquals(desc, atom.nucleus, "2")
                atom = superList.atoms[1];
                assertEquals(desc, atom.type, KMTMathAtomBinaryOperator)
                assertEquals(desc, atom.nucleus, "\u00F7")
                atom = superList.atoms[2];
                assertEquals(desc, atom.type, KMTMathAtomNumber)
                assertEquals(desc, atom.nucleus, "2")
            }
        }

    }

    @Test
    fun testFrac() {
        val str = "\\frac1c"
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        println("In testFrac")
        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            var frac: MTFraction = list.atoms[0] as MTFraction
            assertEquals(desc, frac.type, KMTMathAtomFraction)
            assertEquals(desc, frac.nucleus, "")
            assertTrue(frac.hasRule)
            assertNull(frac.rightDelimiter)
            assertNull(frac.leftDelimiter)

            var subList: MTMathList? = frac.numerator;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                var atom: MTMathAtom = subList.atoms[0];
                assertEquals(desc, atom.type, KMTMathAtomNumber)
                assertEquals(desc, atom.nucleus, "1")
            }

            subList = frac.denominator;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                var atom: MTMathAtom = subList.atoms[0];
                assertEquals(desc, atom.type, KMTMathAtomVariable)
                assertEquals(desc, atom.nucleus, "c")
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\frac{1}{c}")
        }
    }

    @Test
    fun testFracInFrac() {
        val str = "\\frac1\\frac23"
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        println("In testFracInFrac")
        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            var frac: MTFraction = list.atoms[0] as MTFraction
            assertEquals(desc, frac.type, KMTMathAtomFraction)
            assertEquals(desc, frac.nucleus, "")
            assertTrue(frac.hasRule)

            var subList: MTMathList? = frac.numerator;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                var atom: MTMathAtom = subList.atoms[0];
                assertEquals(desc, atom.type, KMTMathAtomNumber)
                assertEquals(desc, atom.nucleus, "1")
            }

            subList = frac.denominator;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                frac = subList.atoms[0] as MTFraction
                assertEquals(desc, frac.type, KMTMathAtomFraction)
                assertEquals(desc, frac.nucleus, "")


                subList = frac.numerator;
                assertNotNull(desc, subList)
                if (subList != null) {
                    assertEquals(desc, subList.atoms.count(), 1)
                    var atom: MTMathAtom = subList.atoms[0];
                    assertEquals(desc, atom.type, KMTMathAtomNumber)
                    assertEquals(desc, atom.nucleus, "2")

                    subList = frac.denominator;
                    assertNotNull(desc, subList)
                    if (subList != null) {
                        assertEquals(desc, subList.atoms.count(), 1)
                        atom = subList.atoms[0];
                        assertEquals(desc, atom.type, KMTMathAtomNumber)
                        assertEquals(desc, atom.nucleus, "3")
                    }
                }
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\frac{1}{\\frac{2}{3}}")
        }
    }

    @Test
    fun testSqrt() {
        val str = "\\sqrt2"
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        println("In testSqrt")

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)

            var rad: MTRadical = list.atoms[0] as MTRadical
            assertEquals(desc, rad.type, KMTMathAtomRadical)
            assertEquals(desc, rad.nucleus, "")

            var subList: MTMathList? = rad.radicand;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                var atom: MTMathAtom = subList.atoms[0];
                assertEquals(desc, atom.type, KMTMathAtomNumber)
                assertEquals(desc, atom.nucleus, "2")
            }


            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\sqrt{2}")
        }
    }

    @Test
    fun testSqrtInSqrt() {
        val str = "\\sqrt\\sqrt2"
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        println("In testSqrtInSqrt")

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)

            var rad: MTRadical = list.atoms[0] as MTRadical
            assertEquals(desc, rad.type, KMTMathAtomRadical)
            assertEquals(desc, rad.nucleus, "")

            var subList: MTMathList? = rad.radicand;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                rad = subList.atoms[0] as MTRadical
                assertEquals(desc, rad.type, KMTMathAtomRadical)
                assertEquals(desc, rad.nucleus, "")

                subList = rad.radicand;
                assertNotNull(desc, subList)
                if (subList != null) {
                    assertEquals(desc, subList.atoms.count(), 1)
                    var atom: MTMathAtom = subList.atoms[0];
                    assertEquals(desc, atom.type, KMTMathAtomNumber)
                    assertEquals(desc, atom.nucleus, "2")
                }
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\sqrt{\\sqrt{2}}")
        }
    }

    @Test
    fun testRad() {
        val str = "\\sqrt[3]2"
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        println("In testRad")

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)

            var rad: MTRadical = list.atoms[0] as MTRadical
            assertEquals(desc, rad.type, KMTMathAtomRadical)
            assertEquals(desc, rad.nucleus, "")

            var subList: MTMathList? = rad.radicand;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                var atom: MTMathAtom = subList.atoms[0];
                assertEquals(desc, atom.type, KMTMathAtomNumber)
                assertEquals(desc, atom.nucleus, "2")
            }

            subList = rad.degree
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                var atom: MTMathAtom = subList.atoms[0];
                assertEquals(desc, atom.type, KMTMathAtomNumber)
                assertEquals(desc, atom.nucleus, "3")
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\sqrt[3]{2}")
        }
    }

    data class LeftRightTest(val srcLaTex: String, val atomTypeArray: Array<MTMathAtomType>,
                             val innerLoc: Int, val innerTypeArray: Array<MTMathAtomType>,
                             val leftboundary: String, val rightboundary: String, val retLaTex: String)

    var testLeftRightTestData: Array<LeftRightTest> = arrayOf(

            LeftRightTest("\\left( 2 \\right)", arrayOf(KMTMathAtomInner), 0, arrayOf(KMTMathAtomNumber), "(", ")", "\\left( 2\\right) "),
            // spacing
            LeftRightTest("\\left ( 2 \\right )", arrayOf(KMTMathAtomInner), 0, arrayOf(KMTMathAtomNumber), "(", ")", "\\left( 2\\right) "),
            // commands
            LeftRightTest("\\left\\{ 2 \\right\\}", arrayOf(KMTMathAtomInner), 0, arrayOf(KMTMathAtomNumber), "{", "}", "\\left\\{ 2\\right\\} "),
            // complex commands
            LeftRightTest("\\left\\langle x \\right\\rangle", arrayOf(KMTMathAtomInner), 0, arrayOf(KMTMathAtomVariable), "\u2329", "\u232A", "\\left< x\\right> "),
            // bars
            LeftRightTest("\\left| x \\right\\|", arrayOf(KMTMathAtomInner), 0, arrayOf(KMTMathAtomVariable), "|", "\u2016", "\\left| x\\right\\| "),
            // inner in between
            LeftRightTest("5 + \\left( 2 \\right) - 2", arrayOf(KMTMathAtomNumber, KMTMathAtomBinaryOperator, KMTMathAtomInner, KMTMathAtomBinaryOperator, KMTMathAtomNumber),
                    2, arrayOf(KMTMathAtomNumber), "(", ")", "5+\\left( 2\\right) -2"),
            // long inner
            LeftRightTest("\\left( 2 + \\frac12\\right)", arrayOf(KMTMathAtomInner),
                    0, arrayOf(KMTMathAtomNumber, KMTMathAtomBinaryOperator, KMTMathAtomFraction), "(", ")", "\\left( 2+\\frac{1}{2}\\right) "),
            // nested
            LeftRightTest("\\left[ 2 + \\left|\\frac{-x}{2}\\right| \\right]", arrayOf(KMTMathAtomInner), 0, arrayOf(KMTMathAtomNumber, KMTMathAtomBinaryOperator, KMTMathAtomInner), "[", "]", "\\left[ 2+\\left| \\frac{-x}{2}\\right| \\right] "),
            // With scripts
            LeftRightTest("\\left( 2 \\right)^2", arrayOf(KMTMathAtomInner),
                    0, arrayOf(KMTMathAtomNumber), "(", ")", "\\left( 2\\right) ^{2}"),
            // Scripts on left
            LeftRightTest("\\left(^2 \\right )", arrayOf(KMTMathAtomInner),
                    0, arrayOf(KMTMathAtomOrdinary), "(", ")", "\\left( {}^{2}\\right) "),
            // Dot
            LeftRightTest("\\left( 2 \\right.", arrayOf(KMTMathAtomInner),
                    0, arrayOf(KMTMathAtomNumber), "(", "", "\\left( 2\\right. ")
    )

    @Test
    fun testLeftRight() {
        println("In testLeftRight")
        for (test in testLeftRightTestData) {
            println("on test " + test.srcLaTex + " expecting return of " + test.retLaTex)

            val builder: MTMathListBuilder? = MTMathListBuilder(test.srcLaTex)
            val e: MTParseError = MTParseError()

            val list: MTMathList? = MTMathListBuilder.buildFromString(test.srcLaTex, e)
            assertEquals(e.errorcode, MTParseErrors.ErrorNone)
            var desc = "$test.srcLaTex outer"
            assertNotNull(desc, list)
            if (list != null) {
                checkAtomTypes(list, test.atomTypeArray, desc)


                val inner: MTInner = list.atoms[test.innerLoc] as MTInner
                assertEquals(desc, inner.type, KMTMathAtomInner)
                assertEquals(desc, inner.nucleus, "")

                val innerList: MTMathList? = inner.innerList
                assertNotNull(desc, innerList)
                if (innerList != null) {
                    checkAtomTypes(innerList, test.innerTypeArray, "$test.srcLaTex inner")
                }

                val leftboundary = inner.leftBoundary
                assertNotNull(desc, leftboundary)
                if (leftboundary != null) {
                    assertEquals(desc, leftboundary.type, KMTMathAtomBoundary)
                    assertEquals(desc, leftboundary.nucleus, test.leftboundary)
                }

                val rightboundary = inner.rightBoundary
                assertNotNull(desc, rightboundary)
                if (rightboundary != null) {
                    assertEquals(desc, rightboundary.type, KMTMathAtomBoundary)
                    assertEquals(desc, rightboundary.nucleus, test.rightboundary)
                }

                // convert it back to latex
                val latex: String = MTMathListBuilder.toLatexString(list)
                assertEquals(desc, test.retLaTex, latex)
                println(" returned $latex")
            }
        }
    }

    @Test
    fun testOver() {
        println("In testOver")
        val str = "1 \\over c";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            var frac: MTFraction = list.atoms[0] as MTFraction
            assertEquals(desc, frac.type, KMTMathAtomFraction)
            assertEquals(desc, frac.nucleus, "")
            assertTrue(frac.hasRule)
            assertNull(frac.rightDelimiter)
            assertNull(frac.leftDelimiter)

            var subList: MTMathList? = frac.numerator;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                var atom: MTMathAtom = subList.atoms[0];
                assertEquals(desc, atom.type, KMTMathAtomNumber)
                assertEquals(desc, atom.nucleus, "1")
            }

            subList = frac.denominator;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                var atom: MTMathAtom = subList.atoms[0];
                assertEquals(desc, atom.type, KMTMathAtomVariable)
                assertEquals(desc, atom.nucleus, "c")
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\frac{1}{c}")
        }
    }

    @Test
    fun testOverInParens() {
        println("In testOverInParens")
        val str = "5 + {1 \\over c} + 8";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 5)
            val types: Array<MTMathAtomType> = arrayOf(KMTMathAtomNumber, KMTMathAtomBinaryOperator, KMTMathAtomFraction, KMTMathAtomBinaryOperator, KMTMathAtomNumber)
            checkAtomTypes(list, types, desc)

            var frac: MTFraction = list.atoms[2] as MTFraction
            assertEquals(desc, frac.type, KMTMathAtomFraction)
            assertEquals(desc, frac.nucleus, "")
            assertTrue(frac.hasRule)
            assertNull(frac.rightDelimiter)
            assertNull(frac.leftDelimiter)

            var subList: MTMathList? = frac.numerator
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                val atom: MTMathAtom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomNumber)
                assertEquals(desc, atom.nucleus, "1")
            }

            subList = frac.denominator;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                val atom: MTMathAtom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomVariable)
                assertEquals(desc, atom.nucleus, "c")
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "5+\\frac{1}{c}+8")
        }
    }


    @Test
    fun testAtop() {
        val str = "1 \\atop c";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            var frac: MTFraction = list.atoms[0] as MTFraction
            assertEquals(desc, frac.type, KMTMathAtomFraction)
            assertEquals(desc, frac.nucleus, "")
            assertFalse(frac.hasRule)
            assertNull(frac.rightDelimiter)
            assertNull(frac.leftDelimiter)

            var subList: MTMathList? = frac.numerator
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                val atom: MTMathAtom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomNumber)
                assertEquals(desc, atom.nucleus, "1")
            }

            subList = frac.denominator;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                val atom: MTMathAtom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomVariable)
                assertEquals(desc, atom.nucleus, "c")
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "{1 \\atop c}")
        }
    }

    @Test
    fun testAtopInParens() {
        val str = "5 + {1 \\atop c} + 8";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 5)
            val types: Array<MTMathAtomType> = arrayOf(KMTMathAtomNumber, KMTMathAtomBinaryOperator, KMTMathAtomFraction, KMTMathAtomBinaryOperator, KMTMathAtomNumber)
            checkAtomTypes(list, types, desc)

            var frac: MTFraction = list.atoms[2] as MTFraction
            assertEquals(desc, frac.type, KMTMathAtomFraction)
            assertEquals(desc, frac.nucleus, "")
            assertFalse(frac.hasRule)
            assertNull(frac.rightDelimiter)
            assertNull(frac.leftDelimiter)

            var subList: MTMathList? = frac.numerator
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                val atom: MTMathAtom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomNumber)
                assertEquals(desc, atom.nucleus, "1")
            }

            subList = frac.denominator;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                val atom: MTMathAtom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomVariable)
                assertEquals(desc, atom.nucleus, "c")
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "5+{1 \\atop c}+8")
        }
    }

    @Test
    fun testChoose() {
        val str = "n \\choose k";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            var frac: MTFraction = list.atoms[0] as MTFraction
            assertEquals(desc, frac.type, KMTMathAtomFraction)
            assertEquals(desc, frac.nucleus, "")
            assertFalse(frac.hasRule)
            assertEquals(desc, frac.rightDelimiter, ")")
            assertEquals(desc, frac.leftDelimiter, "(")

            var subList: MTMathList? = frac.numerator
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                val atom: MTMathAtom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomVariable)
                assertEquals(desc, atom.nucleus, "n")
            }

            subList = frac.denominator;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                val atom: MTMathAtom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomVariable)
                assertEquals(desc, atom.nucleus, "k")
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "{n \\choose k}")
        }
    }

    @Test
    fun testBrack() {
        val str = "n \\brack k";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            var frac: MTFraction = list.atoms[0] as MTFraction
            assertEquals(desc, frac.type, KMTMathAtomFraction)
            assertEquals(desc, frac.nucleus, "")
            assertFalse(frac.hasRule)
            assertEquals(desc, frac.rightDelimiter, "]")
            assertEquals(desc, frac.leftDelimiter, "[")

            var subList: MTMathList? = frac.numerator
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                val atom: MTMathAtom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomVariable)
                assertEquals(desc, atom.nucleus, "n")
            }

            subList = frac.denominator;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                val atom: MTMathAtom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomVariable)
                assertEquals(desc, atom.nucleus, "k")
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "{n \\brack k}")
        }
    }


    @Test
    fun testBrace() {
        val str = "n \\brace k";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            var frac: MTFraction = list.atoms[0] as MTFraction
            assertEquals(desc, frac.type, KMTMathAtomFraction)
            assertEquals(desc, frac.nucleus, "")
            assertFalse(frac.hasRule)
            assertEquals(desc, frac.rightDelimiter, "}")
            assertEquals(desc, frac.leftDelimiter, "{")

            var subList: MTMathList? = frac.numerator
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                val atom: MTMathAtom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomVariable)
                assertEquals(desc, atom.nucleus, "n")
            }

            subList = frac.denominator;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                val atom: MTMathAtom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomVariable)
                assertEquals(desc, atom.nucleus, "k")
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "{n \\brace k}")
        }
    }

    @Test
    fun testBinom() {
        val str = "\\binom{n}{k}";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            var frac: MTFraction = list.atoms[0] as MTFraction
            assertEquals(desc, frac.type, KMTMathAtomFraction)
            assertEquals(desc, frac.nucleus, "")
            assertFalse(frac.hasRule)
            assertEquals(desc, frac.rightDelimiter, ")")
            assertEquals(desc, frac.leftDelimiter, "(")

            var subList: MTMathList? = frac.numerator
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                val atom: MTMathAtom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomVariable)
                assertEquals(desc, atom.nucleus, "n")
            }

            subList = frac.denominator;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                val atom: MTMathAtom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomVariable)
                assertEquals(desc, atom.nucleus, "k")
            }

            // convert it back to latex (binom converts to choose)
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "{n \\choose k}")
        }
    }

    @Test
    fun testOverLine() {
        val str = "\\overline 2";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            var over: MTOverLine = list.atoms[0] as MTOverLine
            assertEquals(desc, over.type, KMTMathAtomOverline)
            assertEquals(desc, over.nucleus, "")

            var subList: MTMathList? = over.innerList
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                val atom: MTMathAtom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomNumber)
                assertEquals(desc, atom.nucleus, "2")
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\overline{2}")
        }
    }

    @Test
    fun testUnderline() {
        val str = "\\underline 2";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            var under: MTUnderLine = list.atoms[0] as MTUnderLine
            assertEquals(desc, under.type, KMTMathAtomUnderline)
            assertEquals(desc, under.nucleus, "")

            var subList: MTMathList? = under.innerList;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                val atom: MTMathAtom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomNumber)
                assertEquals(desc, atom.nucleus, "2")
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\underline{2}")
        }
    }

    @Test
    fun testAccent() {
        val str = "\\bar x";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            var accent: MTAccent = list.atoms[0] as MTAccent
            assertEquals(desc, accent.type, KMTMathAtomAccent)
            assertEquals(desc, accent.nucleus, "\u0304")

            var subList: MTMathList? = accent.innerList;
            assertNotNull(desc, subList)
            if (subList != null) {
                assertEquals(desc, subList.atoms.count(), 1)
                val atom: MTMathAtom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomVariable)
                assertEquals(desc, atom.nucleus, "x")
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\bar{x}")
        }
    }


    @Test
    fun testMathSpace() {
        val str = "\\!";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            var space: MTMathSpace = list.atoms[0] as MTMathSpace
            assertEquals(desc, space.type, KMTMathAtomSpace)
            assertEquals(desc, space.nucleus, "")
            assertEquals(desc, space.space, -3.0f)

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\! ")
        }
    }

    @Test
    fun testMathStyle() {
        val str = "\\textstyle y \\scriptstyle x";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 4)
            var style: MTMathStyle = list.atoms[0] as MTMathStyle
            assertEquals(desc, style.type, KMTMathAtomStyle)
            assertEquals(desc, style.nucleus, "")
            assertEquals(desc, style.style, MTLineStyle.KMTLineStyleText)

            var style2: MTMathStyle = list.atoms[2] as MTMathStyle
            assertEquals(desc, style2.type, KMTMathAtomStyle)
            assertEquals(desc, style2.nucleus, "")
            assertEquals(desc, style2.style, MTLineStyle.KMTLineStyleScript)

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\textstyle y\\scriptstyle x")
        }
    }

    @Test
    fun testMatrix() {
        val str = "\\begin{matrix} x & y \\\\ z & w \\end{matrix}";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            val table: MTMathTable = list.atoms[0] as MTMathTable
            assertEquals(desc, table.type, KMTMathAtomTable)
            assertEquals(desc, table.nucleus, "")
            assertEquals(desc, table.environment, "matrix")
            assertEquals(desc, table.interRowAdditionalSpacing, 0.0f)
            assertEquals(desc, table.interColumnSpacing, 18.0f)
            assertEquals(desc, table.numRows(), 2)
            assertEquals(desc, table.numColumns(), 2)

            for (i in 0 until 2) {
                val alignment: MTColumnAlignment = table.getAlignmentForColumn(i)
                assertEquals(desc, alignment, MTColumnAlignment.KMTColumnAlignmentCenter)
                for (j in 0 until 2) {
                    val cell: MTMathList = table.cells[j][i] as MTMathList
                    assertEquals(desc, cell.atoms.count(), 2)
                    val style: MTMathStyle = cell.atoms[0] as MTMathStyle
                    assertEquals(desc, style.type, KMTMathAtomStyle)
                    assertEquals(desc, style.style, MTLineStyle.KMTLineStyleText)

                    val atom: MTMathAtom = cell.atoms[1];
                    assertEquals(desc, atom.type, KMTMathAtomVariable)
                }
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\begin{matrix}x&y\\\\ z&w\\end{matrix}")
        }
    }

    @Test
    fun testPMatrix() {
        val str = "\\begin{pmatrix} x & y \\\\ z & w \\end{pmatrix}";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            val inner = list.atoms[0] as MTInner
            assertEquals(desc, inner.type, KMTMathAtomInner)
            assertEquals(desc, inner.nucleus, "")


            val lb = inner.leftBoundary
            assertNotNull(desc, lb)
            if (lb != null) {
                assertEquals(desc, lb.type, KMTMathAtomBoundary)
                assertEquals(desc, lb.nucleus, "(")
            }

            val rb = inner.rightBoundary
            assertNotNull(desc, rb)
            if (rb != null) {
                assertEquals(desc, rb.type, KMTMathAtomBoundary)
                assertEquals(desc, rb.nucleus, ")")
            }

            val innerList = inner.innerList;
            assertNotNull(desc, innerList)
            if (innerList != null) {
                assertEquals(desc, innerList.atoms.count(), 1)
                val table = innerList.atoms[0] as MTMathTable

                assertEquals(desc, table.type, KMTMathAtomTable)
                assertEquals(desc, table.nucleus, "")
                assertEquals(desc, table.environment, "matrix")
                assertEquals(desc, table.interRowAdditionalSpacing, 0.0f)
                assertEquals(desc, table.interColumnSpacing, 18.0f)
                assertEquals(desc, table.numRows(), 2)
                assertEquals(desc, table.numColumns(), 2)

                for (i in 0 until 2) {
                    val alignment: MTColumnAlignment = table.getAlignmentForColumn(i)
                    assertEquals(desc, alignment, MTColumnAlignment.KMTColumnAlignmentCenter)
                    for (j in 0 until 2) {
                        val cell = table.cells[j][i];
                        assertEquals(desc, cell.atoms.count(), 2)
                        val style = cell.atoms[0] as MTMathStyle
                        assertEquals(desc, style.type, KMTMathAtomStyle)
                        assertEquals(desc, style.style, MTLineStyle.KMTLineStyleText)

                        val atom = cell.atoms[1];
                        assertEquals(desc, atom.type, KMTMathAtomVariable)
                    }
                }
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\left( \\begin{matrix}x&y\\\\ z&w\\end{matrix}\\right) ")
        }
    }

    @Test
    fun testDefaultTable() {
        val str = "x \\\\ y";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            val table = list.atoms[0] as MTMathTable
            assertEquals(desc, table.type, KMTMathAtomTable)
            assertEquals(desc, table.nucleus, "")
            assertNull(table.environment)
            assertEquals(desc, table.interRowAdditionalSpacing, 1.0f)
            assertEquals(desc, table.interColumnSpacing, 0.0f)
            assertEquals(desc, table.numRows(), 2)
            assertEquals(desc, table.numColumns(), 1)

            for (i in 0 until 1) {
                val alignment: MTColumnAlignment = table.getAlignmentForColumn(i)
                assertEquals(desc, alignment, MTColumnAlignment.KMTColumnAlignmentLeft)
                for (j in 0 until 2) {
                    val cell = table.cells[j][i];
                    assertEquals(desc, cell.atoms.count(), 1)
                    val atom = cell.atoms[0];
                    assertEquals(desc, atom.type, KMTMathAtomVariable)
                }
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "x\\\\ y")
        }
    }

    @Test
    fun testDefaultTableWithCols() {
        val str = "x & y \\\\ z & w";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            val table = list.atoms[0] as MTMathTable
            assertEquals(desc, table.type, KMTMathAtomTable)
            assertEquals(desc, table.nucleus, "")
            assertNull(table.environment)
            assertEquals(desc, table.interRowAdditionalSpacing, 1.0f)
            assertEquals(desc, table.interColumnSpacing, 0.0f)
            assertEquals(desc, table.numRows(), 2)
            assertEquals(desc, table.numColumns(), 2)

            for (i in 0 until 2) {
                val alignment: MTColumnAlignment = table.getAlignmentForColumn(i)
                assertEquals(desc, alignment, MTColumnAlignment.KMTColumnAlignmentLeft)
                for (j in 0 until 2) {
                    val cell = table.cells[j][i];
                    assertEquals(desc, cell.atoms.count(), 1)
                    val atom = cell.atoms[0];
                    assertEquals(desc, atom.type, KMTMathAtomVariable)
                }
            }

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "x&y\\\\ z&w")
        }
    }

    @Test
    fun testEqalign() {
        val str1 = "\\begin{eqalign}x&y\\\\ z&w\\end{eqalign}";
        val str2 = "\\begin{split}x&y\\\\ z&w\\end{split}";
        val str3 = "\\begin{aligned}x&y\\\\ z&w\\end{aligned}";
        for (str in arrayOf(str1, str2, str3)) {
            val list: MTMathList? = MTMathListBuilder.buildFromString(str)
            val desc = "Error for string:$str"

            assertNotNull(desc, list)
            if (list != null) {
                assertEquals(desc, list.atoms.count(), 1)
                val table = list.atoms[0] as MTMathTable
                assertEquals(desc, table.type, KMTMathAtomTable)
                assertEquals(desc, table.nucleus, "")
                assertEquals(desc, table.interRowAdditionalSpacing, 1.0f)
                assertEquals(desc, table.interColumnSpacing, 0.0f)
                assertEquals(desc, table.numRows(), 2)
                assertEquals(desc, table.numColumns(), 2)

                for (i in 0 until 2) {
                    val alignment: MTColumnAlignment = table.getAlignmentForColumn(i)
                    val correctalignment = if (i == 0) MTColumnAlignment.KMTColumnAlignmentRight else MTColumnAlignment.KMTColumnAlignmentLeft
                    assertEquals(desc, alignment, correctalignment)
                    for (j in 0 until 2) {
                        val cell = table.cells[j][i];
                        if (i == 0) {
                            assertEquals(desc, cell.atoms.count(), 1)
                            val atom = cell.atoms[0];
                            assertEquals(desc, atom.type, KMTMathAtomVariable)
                        } else {
                            assertEquals(desc, cell.atoms.count(), 2)
                            checkAtomTypes(cell, arrayOf(KMTMathAtomOrdinary, KMTMathAtomVariable), desc)
                        }
                    }
                }

                // convert it back to latex
                val latex: String = MTMathListBuilder.toLatexString(list)
                assertEquals(desc, latex, str)
            }
        }
    }

    @Test
    fun testDisplayLines() {
        val str1 = "\\begin{displaylines}x\\\\ y\\end{displaylines}";
        val str2 = "\\begin{gather}x\\\\ y\\end{gather}";
        for (str in arrayOf(str1, str2)) {
            val list: MTMathList? = MTMathListBuilder.buildFromString(str)
            val desc = "Error for string:$str"

            assertNotNull(desc, list)
            if (list != null) {
                assertEquals(desc, list.atoms.count(), 1)
                val table = list.atoms[0] as MTMathTable
                assertEquals(desc, table.type, KMTMathAtomTable)
                assertEquals(desc, table.nucleus, "")
                assertEquals(desc, table.interRowAdditionalSpacing, 1.0f)
                assertEquals(desc, table.interColumnSpacing, 0.0f)
                assertEquals(desc, table.numRows(), 2)
                assertEquals(desc, table.numColumns(), 1)

                for (i in 0 until 1) {
                    val alignment: MTColumnAlignment = table.getAlignmentForColumn(i)
                    assertEquals(desc, alignment, MTColumnAlignment.KMTColumnAlignmentCenter)
                    for (j in 0 until 2) {
                        val cell = table.cells[j][i];
                        assertEquals(desc, cell.atoms.count(), 1)
                        val atom = cell.atoms[0];
                        assertEquals(desc, atom.type, KMTMathAtomVariable)
                    }
                }

                // convert it back to latex
                val latex: String = MTMathListBuilder.toLatexString(list)
                assertEquals(desc, latex, str)
            }
        }
    }


    data class DataParseErrorTest(val srcLaTex: String, val errorcode: MTParseErrors)

    var testParseErrorTestData: Array<DataParseErrorTest> = arrayOf(
            DataParseErrorTest("}a", MTParseErrors.MismatchBraces),
            DataParseErrorTest("\\notacommand", MTParseErrors.InvalidCommand),
            DataParseErrorTest("\\sqrt[5+3", MTParseErrors.CharacterNotFound),
            DataParseErrorTest("{5+3", MTParseErrors.MismatchBraces),
            DataParseErrorTest("5+3}", MTParseErrors.MismatchBraces),
            DataParseErrorTest("{1+\\frac{3+2", MTParseErrors.MismatchBraces),
            DataParseErrorTest("1+\\left", MTParseErrors.MissingDelimiter),
            DataParseErrorTest("\\left(\\frac12\\right", MTParseErrors.MissingDelimiter),
            DataParseErrorTest("\\left 5 + 3 \\right)", MTParseErrors.InvalidDelimiter),
            DataParseErrorTest("\\left(\\frac12\\right + 3", MTParseErrors.InvalidDelimiter),
            DataParseErrorTest("\\left\\lmoustache 5 + 3 \\right)", MTParseErrors.InvalidDelimiter),
            DataParseErrorTest("\\left(\\frac12\\right\\rmoustache + 3", MTParseErrors.InvalidDelimiter),
            DataParseErrorTest("5 + 3 \\right)", MTParseErrors.MissingLeft),
            DataParseErrorTest("\\left(\\frac12", MTParseErrors.MissingRight),
            DataParseErrorTest("\\left(5 + \\left| \\frac12 \\right)", MTParseErrors.MissingRight),
            DataParseErrorTest("5+ \\left|\\frac12\\right| \\right)", MTParseErrors.MissingLeft),
            DataParseErrorTest("\\begin matrix \\end matrix", MTParseErrors.CharacterNotFound), // missing {
            DataParseErrorTest("\\begin", MTParseErrors.CharacterNotFound), // missing {
            DataParseErrorTest("\\begin{", MTParseErrors.CharacterNotFound), // missing }
            DataParseErrorTest("\\begin{matrix parens}", MTParseErrors.CharacterNotFound), // missing } (no spaces in env)
            DataParseErrorTest("\\begin{matrix} x", MTParseErrors.MissingEnd),
            DataParseErrorTest("\\begin{matrix} x \\end", MTParseErrors.CharacterNotFound), // missing {
            DataParseErrorTest("\\begin{matrix} x \\end + 3", MTParseErrors.CharacterNotFound), // missing {
            DataParseErrorTest("\\begin{matrix} x \\end{", MTParseErrors.CharacterNotFound), // missing }
            DataParseErrorTest("\\begin{matrix} x \\end{matrix + 3", MTParseErrors.CharacterNotFound), // missing }
            DataParseErrorTest("\\begin{matrix} x \\end{pmatrix}", MTParseErrors.InvalidEnv),
            DataParseErrorTest("x \\end{matrix}", MTParseErrors.MissingBegin),
            DataParseErrorTest("\\begin{notanenv} x \\end{notanenv}", MTParseErrors.InvalidEnv),
            DataParseErrorTest("\\begin{matrix} \\notacommand \\end{matrix}", MTParseErrors.InvalidCommand),
            DataParseErrorTest("\\begin{displaylines} x & y \\end{displaylines}", MTParseErrors.InvalidNumColumns),
            DataParseErrorTest("\\begin{eqalign} x \\end{eqalign}", MTParseErrors.InvalidNumColumns),
            DataParseErrorTest("\\nolimits", MTParseErrors.InvalidLimits),
            DataParseErrorTest("\\frac\\limits{1}{2}", MTParseErrors.InvalidLimits)
    )

    @Test
    fun testErrors() {
        for (testCase in testParseErrorTestData) {
            val e: MTParseError = MTParseError()
            val list: MTMathList? = MTMathListBuilder.buildFromString(testCase.srcLaTex, e)
            val desc = "Error for string:${testCase.srcLaTex}"
            assertNull(list)
            assertNotEquals(desc, e.errorcode, MTParseErrors.ErrorNone)
            assertEquals(desc, e.errorcode, testCase.errorcode)
        }
    }

    @Test
    fun testCustom() {
        val str = "\\lcm(a,b)"
        var e: MTParseError = MTParseError()
        var list: MTMathList? = MTMathListBuilder.buildFromString(str, e)
        val desc = "Error for string:$str"
        assertNull(list)
        assertNotEquals(desc, e.errorcode, MTParseErrors.ErrorNone)

        MTMathAtom.addLatexSymbol("lcm", MTMathAtom.operatorWithName("lcm", false))
        //[MTMathAtomFactory addLatexSymbol:"lcm" value:[MTMathAtomFactory operatorWithName:"lcm" limits:NO]];
        e = MTParseError()
        list = MTMathListBuilder.buildFromString(str, e)
        val atomTypes = arrayOf(KMTMathAtomLargeOperator, KMTMathAtomOpen, KMTMathAtomVariable, KMTMathAtomPunctuation, KMTMathAtomVariable, KMTMathAtomClose)
        assertNotNull(list)
        if (list != null) {
            checkAtomTypes(list, atomTypes, desc)

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\lcm (a,b)")
        }
    }

    @Test
    fun testFontSingle() {
        val str = "\\mathbf x";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            val atom = list.atoms[0];
            assertEquals(desc, atom.type, KMTMathAtomVariable)
            assertEquals(desc, atom.nucleus, "x")
            assertEquals(desc, atom.fontStyle, MTFontStyle.KMTFontStyleBold)

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\mathbf{x}")
        }
    }

    @Test
    fun testFontOneChar() {
        val str = "\\cal xy";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 2)
            var atom = list.atoms[0];
            assertEquals(desc, atom.type, KMTMathAtomVariable)
            assertEquals(desc, atom.nucleus, "x")
            assertEquals(desc, atom.fontStyle, MTFontStyle.KMTFontStyleCaligraphic)

            atom = list.atoms[1];
            assertEquals(desc, atom.type, KMTMathAtomVariable)
            assertEquals(desc, atom.nucleus, "y")
            assertEquals(desc, atom.fontStyle, MTFontStyle.KMTFontStyleDefault)

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\mathcal{x}y")
        }
    }

    @Test
    fun testFontMultipleChars() {
        val str = "\\frak{xy}";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 2)
            var atom = list.atoms[0];
            assertEquals(desc, atom.type, KMTMathAtomVariable)
            assertEquals(desc, atom.nucleus, "x")
            assertEquals(desc, atom.fontStyle, MTFontStyle.KMTFontStyleFraktur)

            atom = list.atoms[1];
            assertEquals(desc, atom.type, KMTMathAtomVariable)
            assertEquals(desc, atom.nucleus, "y")
            assertEquals(desc, atom.fontStyle, MTFontStyle.KMTFontStyleFraktur)

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\mathfrak{xy}")
        }
    }

    @Test
    fun testFontOneCharInside() {
        val str = "\\sqrt \\mathrm x y";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 2)

            val rad = list.atoms[0] as MTRadical
            assertEquals(desc, rad.type, KMTMathAtomRadical)
            assertEquals(desc, rad.nucleus, "")

            val subList = rad.radicand
            if (subList != null) {
                val atom = subList.atoms[0]
                assertEquals(desc, atom.type, KMTMathAtomVariable)
                assertEquals(desc, atom.nucleus, "x")
                assertEquals(desc, atom.fontStyle, MTFontStyle.KMTFontStyleRoman)
            }

            val atom = list.atoms[1];
            assertEquals(desc, atom.type, KMTMathAtomVariable)
            assertEquals(desc, atom.nucleus, "y")
            assertEquals(desc, atom.fontStyle, MTFontStyle.KMTFontStyleDefault)

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\sqrt{\\mathrm{x}}y")
        }
    }

    @Test
    fun testText() {
        val str = "\\text{x y}";
        val list: MTMathList? = MTMathListBuilder.buildFromString(str)
        val desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 3)
            var atom = list.atoms[0];
            assertEquals(desc, atom.type, KMTMathAtomVariable)
            assertEquals(desc, atom.nucleus, "x")
            assertEquals(desc, atom.fontStyle, MTFontStyle.KMTFontStyleRoman)

            atom = list.atoms[1];
            assertEquals(desc, atom.type, KMTMathAtomOrdinary)
            assertEquals(desc, atom.nucleus, " ")

            atom = list.atoms[2];
            assertEquals(desc, atom.type, KMTMathAtomVariable)
            assertEquals(desc, atom.nucleus, "y")
            assertEquals(desc, atom.fontStyle, MTFontStyle.KMTFontStyleRoman)


            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\mathrm{x\\  y}")
        }
    }

    @Test
    fun testLimits() {
        // Int with no limits (default)
        var str = "\\int";
        var list: MTMathList? = MTMathListBuilder.buildFromString(str)
        var desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            val op = list.atoms[0] as MTLargeOperator
            assertEquals(desc, op.type, KMTMathAtomLargeOperator)
            assertFalse(op.hasLimits)

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\int ")
        }

        // Int with limits
        str = "\\int\\limits";
        list = MTMathListBuilder.buildFromString(str)
        desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            val op = list.atoms[0] as MTLargeOperator
            assertEquals(desc, op.type, KMTMathAtomLargeOperator)
            assertTrue(op.hasLimits)

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\int \\limits ")
        }
    }

    @Test
    fun testNoLimits() {
        // Sum with limits (default)
        var str = "\\sum";
        var list: MTMathList? = MTMathListBuilder.buildFromString(str)
        var desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            val op = list.atoms[0] as MTLargeOperator
            assertEquals(desc, op.type, KMTMathAtomLargeOperator)
            assertTrue(op.hasLimits)

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\sum ")
        }

        // Int with limits
        str = "\\sum\\nolimits"
        list = MTMathListBuilder.buildFromString(str)
        desc = "Error for string:$str"

        assertNotNull(desc, list)
        if (list != null) {
            assertEquals(desc, list.atoms.count(), 1)
            val op = list.atoms[0] as MTLargeOperator
            assertEquals(desc, op.type, KMTMathAtomLargeOperator)
            assertFalse(op.hasLimits)

            // convert it back to latex
            val latex: String = MTMathListBuilder.toLatexString(list)
            assertEquals(desc, latex, "\\sum \\nolimits ")
        }
    }


}


