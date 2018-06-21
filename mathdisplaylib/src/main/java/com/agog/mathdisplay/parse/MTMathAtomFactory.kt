package com.agog.mathdisplay.parse

private const val MTSymbolMultiplication = "\u00D7"
private const val MTSymbolDivision = "\u00F7"
private const val MTSymbolFractionSlash = "\u2044"
private const val MTSymbolWhiteSquare = "\u25A1"
private const val MTSymbolBlackSquare = "\u25A0"
private const val MTSymbolLessEqual = "\u2264"
private const val MTSymbolGreaterEqual = "\u2265"
private const val MTSymbolNotEqual = "\u2260"
private const val MTSymbolSquareRoot = "\u221A" // \sqrt
private const val MTSymbolCubeRoot = "\u221B"
private const val MTSymbolInfinity = "\u221E" // \infty
private const val MTSymbolAngle = "\u2220" // \angle
private const val MTSymbolDegree = "\u00B0" // \circ


open class MTMathAtomFactory {

    fun times(): MTMathAtom {
        return MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, MTSymbolMultiplication)
    }

    private fun divide(): MTMathAtom {
        return MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, MTSymbolDivision)
    }

    private fun placeholder(): MTMathAtom {
        return MTMathAtom(MTMathAtomType.KMTMathAtomPlaceholder, MTSymbolWhiteSquare)
    }

    private fun placeholderList(): MTMathList {
        val newList = MTMathList()
        newList.addAtom(placeholder())
        return newList
    }

    @Suppress("unused")
    private fun placeholderFraction(): MTFraction {
        val frac = MTFraction()
        frac.numerator = placeholderList()
        frac.denominator = placeholderList()
        return frac
    }

    @Suppress("unused")
    private fun placeholderRadical(): MTRadical {
        val rad = MTRadical()
        rad.degree = placeholderList()
        rad.radicand = placeholderList()
        return rad
    }

    @Suppress("unused")
    private fun placeholderSquareRoot(): MTRadical {
        val rad = MTRadical()
        rad.radicand = placeholderList()
        return rad
    }

    internal fun operatorWithName(name: String, limits: Boolean): MTLargeOperator {
        return MTLargeOperator(name, limits)
    }


    fun mathListForCharacters(chars: String): MTMathList {
        val list = MTMathList()
        for (char in chars) {
            val atom: MTMathAtom? = MTMathAtom.atomForCharacter(char)
            if (atom != null) {
                list.addAtom(atom)
            }
        }
        return list
    }


    fun latexSymbolNameForAtom(atom: MTMathAtom): String? {
        if (atom.nucleus.isEmpty()) {
            return null
        }
        return textToLatexSymbolNames[atom.nucleus]
    }

    fun addLatexSymbol(name: String, atom: MTMathAtom) {
        supportedLatexSymbols[name] = atom
        if (atom.nucleus.isNotEmpty()) {
            textToLatexSymbolNames[atom.nucleus] = name
        }
    }

    fun supportedLatexSymbolNames(): List<String> {
        return supportedLatexSymbols.keys.sorted()
    }

    fun accentWithName(accentName: String): MTAccent? {
        val accentValue: String? = accents[accentName]
        return if (accentValue != null) {
            MTAccent(accentValue)
        } else {
            null
        }
    }

    fun accentName(accent: MTAccent): String? {
        return accentToCommands[accent.nucleus]
    }

    fun boundaryAtomForDelimiterName(delimName: String): MTMathAtom? {
        val delimValue = delimiters[delimName] ?: return null
        return MTMathAtom(MTMathAtomType.KMTMathAtomBoundary, delimValue)
    }

    fun delimiterNameForBoundaryAtom(boundary: MTMathAtom): String? {
        if (boundary.type != MTMathAtomType.KMTMathAtomBoundary) {
            return null
        }
        return delimValueToName[boundary.nucleus]
    }

    val fontStyleWithName: HashMap<String, MTFontStyle> = hashMapOf(
            "mathnormal" to MTFontStyle.KMTFontStyleDefault,
            "mathrm" to MTFontStyle.KMTFontStyleRoman,
            "textrm" to MTFontStyle.KMTFontStyleRoman,
            "rm" to MTFontStyle.KMTFontStyleRoman,
            "mathbf" to MTFontStyle.KMTFontStyleBold,
            "bf" to MTFontStyle.KMTFontStyleBold,
            "textbf" to MTFontStyle.KMTFontStyleBold,
            "mathcal" to MTFontStyle.KMTFontStyleCaligraphic,
            "cal" to MTFontStyle.KMTFontStyleCaligraphic,
            "mathtt" to MTFontStyle.KMTFontStyleTypewriter,
            "texttt" to MTFontStyle.KMTFontStyleTypewriter,
            "mathit" to MTFontStyle.KMTFontStyleItalic,
            "textit" to MTFontStyle.KMTFontStyleItalic,
            "mit" to MTFontStyle.KMTFontStyleItalic,
            "mathsf" to MTFontStyle.KMTFontStyleSansSerif,
            "textsf" to MTFontStyle.KMTFontStyleSansSerif,
            "mathfrak" to MTFontStyle.KMTFontStyleFraktur,
            "frak" to MTFontStyle.KMTFontStyleFraktur,
            "mathbb" to MTFontStyle.KMTFontStyleBlackboard,
            "mathbfit" to MTFontStyle.KMTFontStyleBoldItalic,
            "bm" to MTFontStyle.KMTFontStyleBoldItalic,
            "text" to MTFontStyle.KMTFontStyleRoman
    )


    fun fontNameForStyle(fontStyle: MTFontStyle): String {
        when (fontStyle) {
            MTFontStyle.KMTFontStyleDefault -> return "mathnormal"

            MTFontStyle.KMTFontStyleRoman -> return "mathrm"

            MTFontStyle.KMTFontStyleBold -> return "mathbf"

            MTFontStyle.KMTFontStyleFraktur -> return "mathfrak"

            MTFontStyle.KMTFontStyleCaligraphic -> return "mathcal"

            MTFontStyle.KMTFontStyleItalic -> return "mathit"

            MTFontStyle.KMTFontStyleSansSerif -> return "mathsf"

            MTFontStyle.KMTFontStyleBlackboard -> return "mathbb"

            MTFontStyle.KMTFontStyleTypewriter -> return "mathtt"

            MTFontStyle.KMTFontStyleBoldItalic -> return "bm"
        }
    }

    private fun fractionWithNumerator(num: MTMathList, denom: MTMathList): MTFraction {
        val frac = MTFraction()
        frac.numerator = num
        frac.denominator = denom
        return frac
    }

    fun fractionWithNumerator(numStr: String, denominatorStr: String): MTFraction {
        val num: MTMathList = mathListForCharacters(numStr)
        val denom: MTMathList = mathListForCharacters(denominatorStr)
        return fractionWithNumerator(num, denom)
    }

    fun tableWithEnvironment(env: String?, cells: MutableList<MutableList<MTMathList>>, error: MTParseError): MTMathAtom? {
        val table = MTMathTable(env)
        table.cells = cells
        val matrixEnvs: HashMap<String, Array<String>> =
                hashMapOf("matrix" to arrayOf(""),
                        "pmatrix" to arrayOf("(", ")"),
                        "bmatrix" to arrayOf("[", "]"),
                        "Bmatrix" to arrayOf("{", "}"),
                        "vmatrix" to arrayOf("vert", "vert"),
                        "Vmatrix" to arrayOf("Vert", "Vert"))


        if (matrixEnvs.containsKey(env)) {
            // it is set to matrix as the delimiters are converted to latex outside the table.
            table.environment = "matrix"
            table.interRowAdditionalSpacing = 0.0f
            table.interColumnSpacing = 18.0f
            // All the lists are in textstyle
            val style: MTMathAtom = MTMathStyle(MTLineStyle.KMTLineStyleText)

            for (i in 0 until table.cells.size) {
                val rowArray = table.cells[i]
                for (j in 0 until rowArray.size) {
                    rowArray[j].insertAtom(style, 0)
                }
            }
            // Add delimiters
            val delims: Array<String>? = matrixEnvs[env]
            return if (delims?.size == 2) {
                val inner = MTInner()
                inner.leftBoundary = boundaryAtomForDelimiterName(delims[0])
                inner.rightBoundary = boundaryAtomForDelimiterName(delims[1])
                inner.innerList = MTMathList(table)
                inner
            } else {
                table
            }
        } else if (env == null) {
            // The default env.
            table.interRowAdditionalSpacing = 1.0f
            table.interColumnSpacing = 0.0f
            val cols = table.numColumns()
            for (i in 0 until cols) {
                table.setAlignment(MTColumnAlignment.KMTColumnAlignmentLeft, i)
            }
            return table
        } else if (env == "eqalign" || env == "split" || env == "aligned") {
            if (table.numColumns() != 2) {
                error.copyFrom(MTParseError(MTParseErrors.InvalidNumColumns, "$env environment can only have 2 columns"))
                return null
            }
            // Add a spacer before each of the second column elements. This is to create the correct spacing for = and other releations.
            val spacer = MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "")
            for (i in 0 until table.cells.size) {
                val row: MutableList<MTMathList> = table.cells[i]
                if (row.size > 1) {
                    row[1].insertAtom(spacer, 0)
                }
            }
            table.interRowAdditionalSpacing = 1.0f
            table.interColumnSpacing = 0.0f
            table.setAlignment(MTColumnAlignment.KMTColumnAlignmentRight, 0)
            table.setAlignment(MTColumnAlignment.KMTColumnAlignmentLeft, 1)
            return table
        } else if (env == "displaylines" || env == "gather") {
            if (table.numColumns() != 1) {
                error.copyFrom(MTParseError(MTParseErrors.InvalidNumColumns, "$env environment can only have 1 column"))
                return null
            }
            table.interRowAdditionalSpacing = 1.0f
            table.interColumnSpacing = 0.0f
            table.setAlignment(MTColumnAlignment.KMTColumnAlignmentCenter, 0)
            return table
        } else if (env == "eqnarray") {
            if (table.numColumns() != 3) {
                error.copyFrom(MTParseError(MTParseErrors.InvalidNumColumns, "eqnarray environment can only have 3 columns"))
                return null
            }
            table.interRowAdditionalSpacing = 1.0f
            table.interColumnSpacing = 18.0f
            table.setAlignment(MTColumnAlignment.KMTColumnAlignmentRight, 0)
            table.setAlignment(MTColumnAlignment.KMTColumnAlignmentCenter, 1)
            table.setAlignment(MTColumnAlignment.KMTColumnAlignmentLeft, 2)
            return table
        } else if (env == "cases") {
            if (table.numColumns() != 2) {
                error.copyFrom(MTParseError(MTParseErrors.InvalidNumColumns, "cases environment can only have 2 columns"))
                return null
            }
            table.interRowAdditionalSpacing = 0.0f
            table.interColumnSpacing = 18.0f
            table.setAlignment(MTColumnAlignment.KMTColumnAlignmentLeft, 0)
            table.setAlignment(MTColumnAlignment.KMTColumnAlignmentLeft, 1)
            // All the lists are in textstyle
            val style: MTMathAtom = MTMathStyle(MTLineStyle.KMTLineStyleText)
            for (i in 0 until table.cells.size) {
                val row: MutableList<MTMathList> = table.cells[i]
                for (j in 0 until row.size) {
                    row[j].insertAtom(style, 0)
                }
            }
            // Add delimiters
            val inner = MTInner()
            inner.leftBoundary = boundaryAtomForDelimiterName("{")
            inner.rightBoundary = boundaryAtomForDelimiterName(".")
            val space: MTMathAtom? = atomForLatexSymbolName(",")
            if (space != null) {
                inner.innerList = MTMathList(space, table)
            }
            return inner
        }
        error.copyFrom(MTParseError(MTParseErrors.InvalidEnv, "Unknown environment: $env"))
        return null
    }

    private val supportedLatexSymbols: HashMap<String, MTMathAtom> = hashMapOf(
            "square" to placeholder(),

            // Greek characters
            "alpha" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03B1"),
            "beta" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03B2"),
            "gamma" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03B3"),
            "delta" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03B4"),
            "varepsilon" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03B5"),
            "zeta" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03B6"),
            "eta" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03B7"),
            "theta" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03B8"),
            "iota" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03B9"),
            "kappa" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03BA"),
            "lambda" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03BB"),
            "mu" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03BC"),
            "nu" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03BD"),
            "xi" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03BE"),
            "omicron" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03BF"),
            "pi" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03C0"),
            "rho" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03C1"),
            "varsigma" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03C2"),
            "sigma" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03C3"),
            "tau" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03C4"),
            "upsilon" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03C5"),
            "varphi" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03C6"),
            "chi" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03C7"),
            "psi" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03C8"),
            "omega" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03C9"),

            "vartheta" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03D1"),
            "phi" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03D5"),
            "varpi" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03D6"),
            "varkappa" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03F0"),
            "varrho" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03F1"),
            "epsilon" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03F5"),

            // Capital greek characters
            "Gamma" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u0393"),
            "Delta" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u0394"),
            "Theta" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u0398"),
            "Lambda" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u039B"),
            "Xi" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u039E"),
            "Pi" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03A0"),
            "Sigma" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03A3"),
            "Upsilon" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03A5"),
            "Phi" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03A6"),
            "Psi" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03A8"),
            "Omega" to MTMathAtom(MTMathAtomType.KMTMathAtomVariable, "\u03A9"),

            // Open
            "lceil" to MTMathAtom(MTMathAtomType.KMTMathAtomOpen, "\u2308"),
            "lfloor" to MTMathAtom(MTMathAtomType.KMTMathAtomOpen, "\u230A"),
            "langle" to MTMathAtom(MTMathAtomType.KMTMathAtomOpen, "\u27E8"),
            "lgroup" to MTMathAtom(MTMathAtomType.KMTMathAtomOpen, "\u27EE"),

            // Close
            "rceil" to MTMathAtom(MTMathAtomType.KMTMathAtomClose, "\u2309"),
            "rfloor" to MTMathAtom(MTMathAtomType.KMTMathAtomClose, "\u230B"),
            "rangle" to MTMathAtom(MTMathAtomType.KMTMathAtomClose, "\u27E9"),
            "rgroup" to MTMathAtom(MTMathAtomType.KMTMathAtomClose, "\u27EF"),

            // Arrows
            "leftarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2190"),
            "uparrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2191"),
            "rightarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2192"),
            "downarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2193"),
            "leftrightarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2194"),
            "updownarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2195"),
            "nwarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2196"),
            "nearrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2197"),
            "searrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2198"),
            "swarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2199"),
            "mapsto" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u21A6"),
            "Leftarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u21D0"),
            "Uparrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u21D1"),
            "Rightarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u21D2"),
            "Downarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u21D3"),
            "Leftrightarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u21D4"),
            "Updownarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u21D5"),
            "longleftarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u27F5"),
            "longrightarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u27F6"),
            "longleftrightarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u27F7"),
            "Longleftarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u27F8"),
            "Longrightarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u27F9"),
            "Longleftrightarrow" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u27FA"),


            // Relations
            "leq" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, MTSymbolLessEqual),
            "geq" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, MTSymbolGreaterEqual),
            "neq" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, MTSymbolNotEqual),
            "in" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2208"),
            "notin" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2209"),
            "ni" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u220B"),
            "propto" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u221D"),
            "mid" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2223"),
            "parallel" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2225"),
            "sim" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u223C"),
            "simeq" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2243"),
            "cong" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2245"),
            "approx" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2248"),
            "asymp" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u224D"),
            "doteq" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2250"),
            "equiv" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2261"),
            "gg" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u226A"),
            "ll" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u226B"),
            "prec" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u227A"),
            "succ" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u227B"),
            "subset" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2282"),
            "supset" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2283"),
            "subseteq" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2286"),
            "supseteq" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2287"),
            "sqsubset" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u228F"),
            "sqsupset" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2290"),
            "sqsubseteq" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2291"),
            "sqsupseteq" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u2292"),
            "models" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u22A7"),
            "perp" to MTMathAtom(MTMathAtomType.KMTMathAtomRelation, "\u27C2"),

            // operators
            "times" to times(),
            "div" to divide(),
            "pm" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u00B1"),
            "dagger" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2020"),
            "ddagger" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2021"),
            "mp" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2213"),
            "setminus" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2216"),
            "ast" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2217"),
            "circ" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2218"),
            "bullet" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2219"),
            "wedge" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2227"),
            "vee" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2228"),
            "cap" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2229"),
            "cup" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u222A"),
            "wr" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2240"),
            "uplus" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u228E"),
            "sqcap" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2293"),
            "sqcup" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2294"),
            "oplus" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2295"),
            "ominus" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2296"),
            "otimes" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2297"),
            "oslash" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2298"),
            "odot" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2299"),
            "star" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u22C6"),
            "cdot" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u22C5"),
            "amalg" to MTMathAtom(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2A3F"),

            // No limit operators
            "log" to operatorWithName("log", false),
            "lg" to operatorWithName("lg", false),
            "ln" to operatorWithName("ln", false),
            "sin" to operatorWithName("sin", false),
            "arcsin" to operatorWithName("arcsin", false),
            "sinh" to operatorWithName("sinh", false),
            "cos" to operatorWithName("cos", false),
            "arccos" to operatorWithName("arccos", false),
            "cosh" to operatorWithName("cosh", false),
            "tan" to operatorWithName("tan", false),
            "arctan" to operatorWithName("arctan", false),
            "tanh" to operatorWithName("tanh", false),
            "cot" to operatorWithName("cot", false),
            "coth" to operatorWithName("coth", false),
            "sec" to operatorWithName("sec", false),
            "csc" to operatorWithName("csc", false),
            "arg" to operatorWithName("arg", false),
            "ker" to operatorWithName("ker", false),
            "dim" to operatorWithName("dim", false),
            "hom" to operatorWithName("hom", false),
            "exp" to operatorWithName("exp", false),
            "deg" to operatorWithName("deg", false),

            // Limit operators
            "lim" to operatorWithName("lim", true),
            "limsup" to operatorWithName("lim sup", true),
            "liminf" to operatorWithName("lim inf", true),
            "max" to operatorWithName("max", true),
            "min" to operatorWithName("min", true),
            "sup" to operatorWithName("sup", true),
            "inf" to operatorWithName("inf", true),
            "det" to operatorWithName("det", true),
            "Pr" to operatorWithName("Pr", true),
            "gcd" to operatorWithName("gcd", true),

            // Large operators
            "prod" to operatorWithName("\u220F", true),
            "coprod" to operatorWithName("\u2210", true),
            "sum" to operatorWithName("\u2211", true),
            "int" to operatorWithName("\u222B", false),
            "oint" to operatorWithName("\u222E", false),
            "bigwedge" to operatorWithName("\u22C0", true),
            "bigvee" to operatorWithName("\u22C1", true),
            "bigcap" to operatorWithName("\u22C2", true),
            "bigcup" to operatorWithName("\u22C3", true),
            "bigodot" to operatorWithName("\u2A00", true),
            "bigoplus" to operatorWithName("\u2A01", true),
            "bigotimes" to operatorWithName("\u2A02", true),
            "biguplus" to operatorWithName("\u2A04", true),
            "bigsqcup" to operatorWithName("\u2A06", true),

            // Latex command characters
            "{" to MTMathAtom(MTMathAtomType.KMTMathAtomOpen, "{"),
            "}" to MTMathAtom(MTMathAtomType.KMTMathAtomClose, "}"),
            "$" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "$"),
            "&" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "&"),
            "#" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "#"),
            "%" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "%"),
            "_" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "_"),
            " " to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, " "),
            "backslash" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\\"),

            // Punctuation
            // Note: \colon is different from : which is a relation
            "colon" to MTMathAtom(MTMathAtomType.KMTMathAtomPunctuation, ":"),
            "cdotp" to MTMathAtom(MTMathAtomType.KMTMathAtomPunctuation, "\u00B7"),

            // Other symbols
            "degree" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u00B0"),
            "neg" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u00AC"),
            "angstrom" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u00C5"),
            "|" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u2016"),
            "vert" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "|"),
            "ldots" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u2026"),
            "prime" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u2032"),
            "hbar" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u210F"),
            "Im" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u2111"),
            "ell" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u2113"),
            "wp" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u2118"),
            "Re" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u211C"),
            "mho" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u2127"),
            "aleph" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u2135"),
            "forall" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u2200"),
            "exists" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u2203"),
            "emptyset" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u2205"),
            "nabla" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u2207"),
            "infty" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u221E"),
            "angle" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u2220"),
            "top" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u22A4"),
            "bot" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u22A5"),
            "vdots" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u22EE"),
            "cdots" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u22EF"),
            "ddots" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u22F1"),
            "triangle" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\u25B3"),
            // These expand into 2 unicode chars
            "imath" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\uD835\uDEA4"),
            "jmath" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\uD835\uDEA5"),
            "partial" to MTMathAtom(MTMathAtomType.KMTMathAtomOrdinary, "\uD835\uDF15"),

            // Spacing
            "," to MTMathSpace(3.0f),
            ">" to MTMathSpace(4.0f),
            ";" to MTMathSpace(5.0f),
            "!" to MTMathSpace(-3.0f),
            "quad" to MTMathSpace(18.0f),  // quad = 1em = 18mu
            "qquad" to MTMathSpace(36.0f), // qquad = 2em

            // Style
            "displaystyle" to MTMathStyle(MTLineStyle.KMTLineStyleDisplay),
            "textstyle" to MTMathStyle(MTLineStyle.KMTLineStyleText),
            "scriptstyle" to MTMathStyle(MTLineStyle.KMTLineStyleScript),
            "scriptscriptstyle" to MTMathStyle(MTLineStyle.KMTLineStyleScriptScript)
    )

    val aliases: HashMap<String, String> = hashMapOf(
            "lnot" to "neg",
            "land" to "wedge",
            "lor" to "vee",
            "ne" to "neq",
            "le" to "leq",
            "ge" to "geq",
            "lbrace" to "{",
            "rbrace" to "}",
            "Vert" to "|",
            "gets" to "leftarrow",
            "to" to "rightarrow",
            "iff" to "Longleftrightarrow",
            "AA" to "angstrom"
    )

    // Reverse mapping of supportedLatexSymbols with preference for shortest latex command if two commands have the same nucleus mapping
    private val textToLatexSymbolNames = hashMapOf<String, String>()

    init {
        for ((command, atom) in supportedLatexSymbols) {
            if (atom.nucleus.isEmpty()) {
                continue
            }

            val existingCommand: String? = textToLatexSymbolNames[atom.nucleus]
            if (existingCommand != null) {
                // If there are 2 commands for the same symbol, choose one deterministically.
                if (command.length > existingCommand.length) {
                    // Keep the shorter command
                    continue
                } else if (command.length == existingCommand.length) {
                    // If the length is the same, keep the alphabetically first
                    if (command > existingCommand) {
                        continue
                    }
                }
            }
            // In other cases replace the command.
            textToLatexSymbolNames[atom.nucleus] = command
        }
    }


    private val accents: HashMap<String, String> = hashMapOf(
            "grave" to "\u0300",
            "acute" to "\u0301",
            "hat" to "\u0302",  // In our implementation hat and widehat behave the same.
            "tilde" to "\u0303", // In our implementation tilde and widetilde behave the same.
            "bar" to "\u0304",
            "breve" to "\u0306",
            "dot" to "\u0307",
            "ddot" to "\u0308",
            "check" to "\u030C",
            "vec" to "\u20D7",
            "widehat" to "\u0302",
            "widetilde" to "\u0303"
    )

    // Reverse of above with preference for shortest command on overlap
    private val accentToCommands = hashMapOf<String, String>()

    init {
        for ((command, nucleus) in accents) {
            val existingCommand: String? = accentToCommands[nucleus]
            if (existingCommand != null) {
                if (command.length > existingCommand.length) {
                    // Keep the shorter command
                    continue
                } else if (command.length == existingCommand.length) {
                    // If the length is the same, keep the alphabetically first
                    if (command > existingCommand) {
                        continue
                    }
                }
            }
            accentToCommands[nucleus] = command
        }
    }


    private val delimiters: HashMap<String, String> = hashMapOf(
            "." to "", // . means no delimiter
            "(" to "(",
            ")" to ")",
            "[" to "[",
            "]" to "]",
            "<" to "\u2329",
            ">" to "\u232A",
            "/" to "/",
            "\\" to "\\",
            "|" to "|",
            "lgroup" to "\u27EE",
            "rgroup" to "\u27EF",
            "||" to "\u2016",
            "Vert" to "\u2016",
            "vert" to "|",
            "uparrow" to "\u2191",
            "downarrow" to "\u2193",
            "updownarrow" to "\u2195",
            "Uparrow" to "21D1",
            "Downarrow" to "21D3",
            "Updownarrow" to "21D5",
            "backslash" to "\\",
            "rangle" to "\u232A",
            "langle" to "\u2329",
            "rbrace" to "}",
            "}" to "}",
            "{" to "{",
            "lbrace" to "{",
            "lceil" to "\u2308",
            "rceil" to "\u2309",
            "lfloor" to "\u230A",
            "rfloor" to "\u230B"
    )

    // Reverse of above with preference for shortest command on overlap
    private val delimValueToName = hashMapOf<String, String>()

    init {
        for ((command, delim) in delimiters) {
            val existingCommand: String? = delimValueToName[delim]
            if (existingCommand != null) {
                if (command.length > existingCommand.length) {
                    // Keep the shorter command
                    continue
                } else if (command.length == existingCommand.length) {
                    // If the length is the same, keep the alphabetically first
                    if (command > existingCommand) {
                        continue
                    }
                }
            }
            delimValueToName[delim] = command
        }
    }


    fun atomForLatexSymbolName(symbolName: String): MTMathAtom? {
        var name: String = symbolName

        // First check if this is an alias
        val canonicalName: String? = aliases[symbolName]

        if (canonicalName != null) {
            // Switch to the canonical name
            name = canonicalName
        }

        val atom: MTMathAtom? = supportedLatexSymbols[name]
        if (atom != null) {
            // Return a copy of the atom since atoms are mutable.
            return atom.copyDeep()
        }
        return null
    }

}
