package com.agog.mathdisplay.parse


class MathDisplayException(override var message: String) : Exception(message)

/**
@typedef MTMathAtomType
@brief The type of atom in a `MTMathList`.

The type of the atom determines how it is rendered, and spacing between the atoms.
 */

enum class MTMathAtomType {
    // A non-atom
    KMTMathAtomNone,
    /// A number or text in ordinary format - Ord in TeX
    KMTMathAtomOrdinary,
    /// A number - Does not exist in TeX
    KMTMathAtomNumber,
    /// A variable (i.e. text in italic format) - Does not exist in TeX
    KMTMathAtomVariable,
    /// A large operator such as (sin/cos, integral etc.) - Op in TeX
    KMTMathAtomLargeOperator,
    /// A binary operator - Bin in TeX
    KMTMathAtomBinaryOperator,
    /// A unary operator - Does not exist in TeX.
    KMTMathAtomUnaryOperator,
    /// A relation, e.g. = > < etc. - Rel in TeX
    KMTMathAtomRelation,
    /// Open brackets - Open in TeX
    KMTMathAtomOpen,
    /// Close brackets - Close in TeX
    KMTMathAtomClose,
    /// An fraction e.g 1/2 - generalized fraction noad in TeX
    KMTMathAtomFraction,
    /// A radical operator e.g. sqrt(2)
    KMTMathAtomRadical,
    /// Punctuation such as , - Punct in TeX
    KMTMathAtomPunctuation,
    /// A placeholder square for future input. Does not exist in TeX
    KMTMathAtomPlaceholder,
    /// An inner atom, i.e. an embedded math list - Inner in TeX
    KMTMathAtomInner,
    /// An underlined atom - Under in TeX
    KMTMathAtomUnderline,
    /// An overlined atom - Over in TeX
    KMTMathAtomOverline,
    /// An accented atom - Accent in TeX
    KMTMathAtomAccent,

    // Atoms after this point do not support subscripts or superscripts

    /// A left atom - Left & Right in TeX. We don't need two since we track boundaries separately.
    KMTMathAtomBoundary,

    // Atoms after this are non-math TeX nodes that are still useful in math mode. They do not have
    // the usual structure.

    /// Spacing between math atoms. This denotes both glue and kern for TeX. We do not
    /// distinguish between glue and kern.
    KMTMathAtomSpace,
    /// Denotes style changes during rendering.
    KMTMathAtomStyle,
    KMTMathAtomColor,
    KMTMathAtomTextColor,

    // Atoms after this point are not part of TeX and do not have the usual structure.

    /// An table atom. This atom does not exist in TeX. It is equivalent to the TeX command
    /// halign which is handled outside of the TeX math rendering engine. We bring it into our
    /// math typesetting to handle matrices and other tables.
    KMTMathAtomTable
}

const val NSNotFound: Int = -1

data class NSRange(var location: Int = NSNotFound, var length: Int = 0) {
    // Return true if equal to passed range
    fun equal(cmp: NSRange): Boolean {
        return (cmp.location == this.location && cmp.length == this.length)
    }

    val maxrange
        get() = location + length

    fun union(a: NSRange): NSRange {
        val b = this
        val e = maxOf(a.maxrange, b.maxrange)
        val s = minOf(a.location, b.location)
        return NSRange(s, e - s)
    }
}

enum class MTFontStyle {
    /// The default latex rendering style. i.e. variables are italic and numbers are roman.
    KMTFontStyleDefault,
    /// Roman font style i.e. \mathrm
    KMTFontStyleRoman,
    /// Bold font style i.e. \mathbf
    KMTFontStyleBold,
    /// Caligraphic font style i.e. \mathcal
    KMTFontStyleCaligraphic,
    /// Typewriter (monospace) style i.e. \mathtt
    KMTFontStyleTypewriter,
    /// Italic style i.e. \mathit
    KMTFontStyleItalic,
    /// San-serif font i.e. \mathss
    KMTFontStyleSansSerif,
    /// Fractur font i.e \mathfrak
    KMTFontStyleFraktur,
    /// Blackboard font i.e. \mathbb
    KMTFontStyleBlackboard,
    /// Bold italic
    KMTFontStyleBoldItalic,
}

/** A `MTMathAtom` is the basic unit of a math list. Each atom represents a single character
or mathematical operator in a list. However certain atoms can represent more complex structures
such as fractions and radicals. Each atom has a type which determines how the atom is rendered and
a nucleus. The nucleus contains the character(s) that need to be rendered. However the nucleus may
be empty for certain types of atoms. An atom has an optional subscript or superscript which represents
the subscript or superscript that is to be rendered.

Certain types of atoms inherit from `MTMathAtom` and may have additional fields.
 */
/*
constructor
/** Factory function to create an atom with a given type and value.
@param type The type of the atom to instantiate.
@param value The value of the atoms nucleus. The value is ignored for fractions and radicals.
 */
+ (instancetype) atomWithType: (MTMathAtomType) type value:(NSString*) value;

 */

open class MTMathAtom(var type: MTMathAtomType, var nucleus: String) {

    /** Returns a string representation of the MTMathAtom */
    /** The nucleus of the atom. */

    /** An optional superscript. */
    var superScript: MTMathList? = null
        set(value) {
            if (!this.scriptsAllowed()) {
                throw MathDisplayException("Superscripts not allowed for atom " + this)
            }
            field = value
        }

    /** An optional subscript. */
    var subScript: MTMathList? = null
        set(value) {
            if (!this.scriptsAllowed()) {
                throw MathDisplayException("Subscripts not allowed for atom " + this)
            }
            field = value
        }


    /** The font style to be used for the atom. */
    var fontStyle: MTFontStyle = MTFontStyle.KMTFontStyleDefault

    /// If this atom was formed by fusion of multiple atoms, then this stores the list of atoms that were fused to create this one.
    /// This is used in the finalizing and preprocessing steps.
    var fusedAtoms = mutableListOf<MTMathAtom>()

    /// The index range in the MTMathList this MTMathAtom tracks. This is used by the finalizing and preprocessing steps
    /// which fuse MTMathAtoms to track the position of the current MTMathAtom in the original list.
    // This will be the zero Range until finalize is called on the MTMathList
    var indexRange: NSRange = NSRange(0, 0)

    private fun dumpstr(s: String) {
        val ca = s.toCharArray()
        val cp = Character.codePointAt(ca, 0)
        println("str $s codepoint $cp")
        for (c in ca) {
            println("c $c")
        }
    }

    companion object Factory : MTMathAtomFactory() {

        // Returns true if the current binary operator is not really binary.
        fun isNotBinaryOperator(prevNode: MTMathAtom?): Boolean {
            if (prevNode == null) {
                return true
            }

            if (prevNode.type == MTMathAtomType.KMTMathAtomBinaryOperator || prevNode.type == MTMathAtomType.KMTMathAtomRelation || prevNode.type == MTMathAtomType.KMTMathAtomOpen || prevNode.type == MTMathAtomType.KMTMathAtomPunctuation || prevNode.type == MTMathAtomType.KMTMathAtomLargeOperator) {
                return true
            }
            return false
        }

        fun typeToText(type: MTMathAtomType): String {
            when (type) {
                MTMathAtomType.KMTMathAtomNone -> {
                    return ("None")
                }
                MTMathAtomType.KMTMathAtomOrdinary -> {
                    return ("Ordinary")
                }
                MTMathAtomType.KMTMathAtomNumber -> {
                    return ("Number")
                }
                MTMathAtomType.KMTMathAtomVariable -> {
                    return ("Variable")
                }
                MTMathAtomType.KMTMathAtomBinaryOperator -> {
                    return ("Binary Operator")
                }
                MTMathAtomType.KMTMathAtomUnaryOperator -> {
                    return ("Unary Operator")
                }
                MTMathAtomType.KMTMathAtomRelation -> {
                    return ("Relation")
                }
                MTMathAtomType.KMTMathAtomOpen -> {
                    return ("Open")
                }
                MTMathAtomType.KMTMathAtomClose -> {
                    return ("Close")
                }
                MTMathAtomType.KMTMathAtomFraction -> {
                    return ("Fraction")
                }
                MTMathAtomType.KMTMathAtomRadical -> {
                    return ("Radical")
                }
                MTMathAtomType.KMTMathAtomPunctuation -> {
                    return ("Punctuation")
                }
                MTMathAtomType.KMTMathAtomPlaceholder -> {
                    return ("Placeholder")
                }
                MTMathAtomType.KMTMathAtomLargeOperator -> {
                    return ("Large Operator")
                }
                MTMathAtomType.KMTMathAtomInner -> {
                    return ("Inner")
                }
                MTMathAtomType.KMTMathAtomUnderline -> {
                    return ("Underline")
                }
                MTMathAtomType.KMTMathAtomOverline -> {
                    return ("Overline")
                }
                MTMathAtomType.KMTMathAtomAccent -> {
                    return ("Accent")
                }
                MTMathAtomType.KMTMathAtomBoundary -> {
                    return ("Boundary")
                }
                MTMathAtomType.KMTMathAtomSpace -> {
                    return ("Space")
                }
                MTMathAtomType.KMTMathAtomStyle -> {
                    return ("Style")
                }
                MTMathAtomType.KMTMathAtomColor -> {
                    return ("Color")
                }
                MTMathAtomType.KMTMathAtomTextColor -> {
                    return ("TextColor")
                }
                MTMathAtomType.KMTMathAtomTable -> {
                    return ("Table")
                }
            }
        }


        /*
          Some types have special classes instead of MTMathAtom. Based on the type create the correct class
         */
        fun atomWithType(type: MTMathAtomType, value: String): MTMathAtom {
            when (type) {
            // Default setting of rule is true
                MTMathAtomType.KMTMathAtomFraction -> {
                    return MTFraction(true)
                }

                MTMathAtomType.KMTMathAtomPlaceholder -> {
                    // A placeholder is created with a white square.
                    return MTMathAtom(MTMathAtomType.KMTMathAtomPlaceholder, "\u25A1")
                }

                MTMathAtomType.KMTMathAtomRadical -> {
                    return MTRadical()
                }

            // Default setting of limits is true
                MTMathAtomType.KMTMathAtomLargeOperator -> {
                    return MTLargeOperator(value, true)
                }

                MTMathAtomType.KMTMathAtomInner -> {
                    return MTInner()
                }

                MTMathAtomType.KMTMathAtomOverline -> {
                    return MTOverLine()
                }

                MTMathAtomType.KMTMathAtomUnderline -> {
                    return MTUnderLine()
                }

                MTMathAtomType.KMTMathAtomAccent -> {
                    return MTAccent(value)
                }

                MTMathAtomType.KMTMathAtomSpace -> {
                    return MTMathSpace(0.0f)
                }

                MTMathAtomType.KMTMathAtomColor -> {
                    return MTMathColor()
                }
                else -> {
                    return MTMathAtom(type, value)
                }
            }
        }

        fun atomForCharacter(ch: Char): MTMathAtom? {
            val chStr = Character.toString(ch)


            if (ch.toInt() < 0x21 || ch.toInt() > 0x7E) {
                // skip non ascii characters and spaces
                return null
            } else if (ch == '$' || ch == '%' || ch == '#' || ch == '&' || ch == '~' || ch == '\'') {
                // These are latex control characters that have special meanings. We don't support them.
                return null
            } else if (ch == '^' || ch == '_' || ch == '{' || ch == '}' || ch == '\\') {
                // more special characters for Latex.
                return null
            } else if (ch == '(' || ch == '[') {
                return atomWithType(MTMathAtomType.KMTMathAtomOpen, chStr)
            } else if (ch == ')' || ch == ']' || ch == '!' || ch == '?') {
                return atomWithType(MTMathAtomType.KMTMathAtomClose, chStr)
            } else if (ch == ',' || ch == ';') {
                return atomWithType(MTMathAtomType.KMTMathAtomPunctuation, chStr)
            } else if (ch == '=' || ch == '>' || ch == '<') {
                return atomWithType(MTMathAtomType.KMTMathAtomRelation, chStr)
            } else if (ch == ':') {
                // Math colon is ratio. Regular colon is \colon
                return atomWithType(MTMathAtomType.KMTMathAtomRelation, "\u2236")
            } else if (ch == '-') {
                // Use the math minus sign
                return atomWithType(MTMathAtomType.KMTMathAtomBinaryOperator, "\u2212")
            } else if (ch == '+' || ch == '*') {
                return atomWithType(MTMathAtomType.KMTMathAtomBinaryOperator, chStr)
            } else if (ch == '.' || (ch in '0'..'9')) {
                return atomWithType(MTMathAtomType.KMTMathAtomNumber, chStr)
            } else if ((ch in 'a'..'z') || (ch in 'A'..'Z')) {
                return atomWithType(MTMathAtomType.KMTMathAtomVariable, chStr)
            } else if (ch == '"' || ch == '/' || ch == '@' || ch == '`' || ch == '|') {
                // just an ordinary character. The following are allowed ordinary chars
                // | / ` @ "
                return atomWithType(MTMathAtomType.KMTMathAtomOrdinary, chStr)
            } else {
                throw MathDisplayException("Unknown ascii character $ch. Should have been accounted for.")
            }
        }


    }


    open fun toLatexString(): String {
        var str = nucleus

        str = toStringSubs(str)
        return str
    }

    fun toStringSubs(s: String): String {
        var str = s
        val superscript: MTMathList? = this.superScript
        if (superscript != null) {
            str += "^{" + MTMathListBuilder.toLatexString(superscript) + "}"
        }

        val subscript: MTMathList? = this.subScript
        if (subscript != null) {
            str += "_{" + MTMathListBuilder.toLatexString(subscript) + "}"
        }

        return str
    }


    fun copyDeepContent(atom: MTMathAtom): MTMathAtom {
        if (this.subScript != null) {
            atom.subScript = this.subScript?.copyDeep()
        }
        if (this.superScript != null) {
            atom.superScript = this.superScript?.copyDeep()
        }
        // fusedAtoms are only used in preprocessing which comes after finalized which uses copyDeep()
        // No need to copy fusedAtoms but assert here to find any coding error
        assert(atom.fusedAtoms.isEmpty())
        atom.fontStyle = this.fontStyle
        atom.indexRange = this.indexRange.copy()
        return atom
    }

    open fun copyDeep(): MTMathAtom {
        val atom = MTMathAtom(this.type, this.nucleus)
        copyDeepContent(atom)
        return atom
    }

    fun finalized(newNode: MTMathAtom): MTMathAtom {
        if (this.superScript != null) {
            newNode.superScript = newNode.superScript?.finalized()
        }
        if (this.subScript != null) {
            newNode.subScript = newNode.subScript?.finalized()
        }
        newNode.fontStyle = this.fontStyle
        newNode.indexRange = this.indexRange.copy()
        return newNode
    }

    open fun finalized(): MTMathAtom {
        val atom = this.copyDeep()
        return finalized(atom)
    }


    /** Returns true if this atom allows scripts (sub or super). */

    fun scriptsAllowed(): Boolean {
        return (this.type < MTMathAtomType.KMTMathAtomBoundary)
    }


    fun description(): String {
        return typeToText(this.type) + " " + this
    }

    /// Fuse the given atom with this one by combining their nucleii.
    fun fuse(atom: MTMathAtom) {
        if (this.subScript != null) throw MathDisplayException("Cannot fuse into an atom which has a subscript: " + this)
        if (this.superScript != null) throw MathDisplayException("Cannot fuse into an atom which has a superscript: " + this)
        if (this.type != atom.type) throw MathDisplayException("Only atoms of the same type can be fused: " + this + " " + atom)

        // Update the fused atoms list
        if (this.fusedAtoms.size == 0) {
            this.fusedAtoms.add(this.copyDeep())
        }
        if (atom.fusedAtoms.size != 0) {
            this.fusedAtoms.addAll(atom.fusedAtoms.toTypedArray())
        } else {
            this.fusedAtoms.add(atom)
        }

        // Update the nucleus
        this.nucleus += atom.nucleus

        // Update the range
        this.indexRange.length += atom.indexRange.length

        // Update super/sub scripts
        this.subScript = atom.subScript
        this.superScript = atom.superScript
    }


}


// Fractions have no nucleus and are always KMTMathAtomFraction type

class MTFraction() : MTMathAtom(MTMathAtomType.KMTMathAtomFraction, "") {

    /// Numerator of the fraction
    var numerator: MTMathList? = null
    /// Denominator of the fraction
    var denominator: MTMathList? = null
    /**If true, the fraction has a rule (i.e. a line) between the numerator and denominator.
    The default value is true. */
    var hasRule: Boolean = true

    /** An optional delimiter for a fraction on the left. */
    var leftDelimiter: String? = null
    /** An optional delimiter for a fraction on the right. */
    var rightDelimiter: String? = null

    // fractions have no nucleus
    constructor(rule: Boolean) : this() {
        hasRule = rule
    }

    override fun toLatexString(): String {
        var str = ""

        str += if (this.hasRule) {
            "\\frac"
        } else {
            "\\atop"
        }
        if (this.leftDelimiter != null || this.rightDelimiter != null) {
            str += "[$this.leftDelimiter][$this.rightDelimiter]"
        }

        var nstr = ""
        val num: MTMathList? = this.numerator
        if (num != null) {
            nstr = MTMathListBuilder.toLatexString(num)
        }
        var dstr = ""
        val den: MTMathList? = this.denominator
        if (den != null) {
            dstr = MTMathListBuilder.toLatexString(den)
        }
        str += "{$nstr}{$dstr}"

        return super.toStringSubs(str)
    }


    override fun copyDeep(): MTFraction {
        val atom = MTFraction(this.hasRule)
        super.copyDeepContent(atom)
        atom.hasRule = this.hasRule
        atom.numerator = this.numerator?.copyDeep()
        atom.denominator = this.denominator?.copyDeep()
        atom.leftDelimiter = this.leftDelimiter
        atom.rightDelimiter = this.rightDelimiter
        return atom
    }

    override fun finalized(): MTFraction {
        val newFrac: MTFraction = this.copyDeep()
        super.finalized(newFrac)
        newFrac.numerator = newFrac.numerator?.finalized()
        newFrac.denominator = newFrac.denominator?.finalized()
        return newFrac
    }
}

// Radicals have no nucleus and are always KMTMathAtomRadical type
class MTRadical : MTMathAtom(MTMathAtomType.KMTMathAtomRadical, "") {

    /// Denotes the degree of the radical, i.e. the value to the top left of the radical sign
    /// This can be null if there is no degree.
    var degree: MTMathList? = null

    /// Denotes the term under the square root sign
    ///

    var radicand: MTMathList? = null


    override fun toLatexString(): String {
        var str = "\\sqrt"

        val deg: MTMathList? = this.degree
        if (deg != null) {
            val dstr = MTMathListBuilder.toLatexString(deg)
            str += "[$dstr]"
        }

        val rad: MTMathList? = this.radicand
        var rstr = ""
        if (rad != null) {
            rstr = MTMathListBuilder.toLatexString(rad)
        }

        str += "{$rstr}"

        return super.toStringSubs(str)
    }

    override fun copyDeep(): MTRadical {
        val atom = MTRadical()
        super.copyDeepContent(atom)
        atom.radicand = this.radicand?.copyDeep()
        atom.degree = this.degree?.copyDeep()
        return atom
    }

    override fun finalized(): MTRadical {
        val newRad: MTRadical = this.copyDeep()
        super.finalized(newRad)
        newRad.radicand = newRad.radicand?.finalized()
        newRad.degree = newRad.degree?.finalized()
        return newRad
    }

}


class MTLargeOperator(nucleus: String) : MTMathAtom(MTMathAtomType.KMTMathAtomLargeOperator, nucleus) {
    var hasLimits = false

    constructor(nucleus: String, limits: Boolean) : this(nucleus) {
        hasLimits = limits
    }

    override fun copyDeep(): MTLargeOperator {
        val atom = MTLargeOperator(nucleus, hasLimits)
        super.copyDeepContent(atom)
        return atom
    }
}

// Inners have no nucleus and are always KMTMathAtomInner type
class MTInner : MTMathAtom(MTMathAtomType.KMTMathAtomInner, "") {


    /// The inner math list
    var innerList: MTMathList? = null
    /// The left boundary atom. This must be a node of type KMTMathAtomBoundary
    var leftBoundary: MTMathAtom? = null
        set(value) {
            if (value != null && value.type != MTMathAtomType.KMTMathAtomBoundary) {
                throw MathDisplayException("Left boundary must be of type KMTMathAtomBoundary $value")
            }
            field = value
        }

    /// The right boundary atom. This must be a node of type KMTMathAtomBoundary
    var rightBoundary: MTMathAtom? = null
        set(value) {
            if (value != null && value.type != MTMathAtomType.KMTMathAtomBoundary) {
                throw MathDisplayException("Right boundary must be of type KMTMathAtomBoundary $value")
            }
            field = value
        }


    override fun toLatexString(): String {
        var str = "\\inner"

        val lb = this.leftBoundary
        if (lb != null) {
            str += "[" + lb.nucleus + "]"
        }

        val il: MTMathList? = this.innerList
        var istr = ""
        if (il != null) {
            istr = MTMathListBuilder.toLatexString(il)
        }

        str += "{$istr}"

        val rb = this.rightBoundary
        if (rb != null) {
            str += "[" + rb.nucleus + "]"
        }
        return super.toStringSubs(str)
    }

    override fun copyDeep(): MTInner {
        val atom = MTInner()
        super.copyDeepContent(atom)
        atom.innerList = this.innerList?.copyDeep()
        atom.leftBoundary = this.leftBoundary?.copyDeep()
        atom.rightBoundary = this.rightBoundary?.copyDeep()
        return atom
    }

    override fun finalized(): MTInner {
        val newInner: MTInner = this.copyDeep()
        super.finalized(newInner)
        newInner.innerList = newInner.innerList?.finalized()
        newInner.leftBoundary = newInner.leftBoundary?.finalized()
        newInner.rightBoundary = newInner.rightBoundary?.finalized()
        return newInner
    }

}

// OverLines have no nucleus and are always KMTMathAtomOverline type
class MTOverLine : MTMathAtom(MTMathAtomType.KMTMathAtomOverline, "") {


    /// The inner math list
    var innerList: MTMathList? = null

    override fun toLatexString(): String {
        val il: MTMathList? = this.innerList
        var istr = ""
        if (il != null) {
            istr = MTMathListBuilder.toLatexString(il)
        }

        return "{$istr}"
    }

    override fun copyDeep(): MTOverLine {
        val atom = MTOverLine()
        super.copyDeepContent(atom)
        atom.innerList = this.innerList?.copyDeep()
        return atom
    }

    override fun finalized(): MTOverLine {
        val newOverLine: MTOverLine = this.copyDeep()
        super.finalized(newOverLine)
        newOverLine.innerList = newOverLine.innerList?.finalized()
        return newOverLine
    }


}

// UnderLines have no nucleus and are always KMTMathAtomUnderline type
class MTUnderLine : MTMathAtom(MTMathAtomType.KMTMathAtomUnderline, "") {


    /// The inner math list
    var innerList: MTMathList? = null

    override fun toLatexString(): String {
        val il: MTMathList? = this.innerList
        var istr = ""
        if (il != null) {
            istr = MTMathListBuilder.toLatexString(il)
        }

        return "{$istr}"
    }

    override fun copyDeep(): MTUnderLine {
        val atom = MTUnderLine()
        super.copyDeepContent(atom)
        atom.innerList = this.innerList?.copyDeep()
        return atom
    }

    override fun finalized(): MTUnderLine {
        val newUnderLine: MTUnderLine = this.copyDeep()
        super.finalized(newUnderLine)
        newUnderLine.innerList = newUnderLine.innerList?.finalized()
        return newUnderLine
    }

}

// Accents  are always KMTMathAtomUnderline type
class MTAccent(nucleus: String) : MTMathAtom(MTMathAtomType.KMTMathAtomAccent, nucleus) {


    /// The inner math list
    var innerList: MTMathList? = null

    override fun toLatexString(): String {
        val il: MTMathList? = this.innerList
        var istr = ""
        if (il != null) {
            istr = MTMathListBuilder.toLatexString(il)
        }

        return "{$istr}"
    }

    override fun copyDeep(): MTAccent {
        val atom = MTAccent(nucleus)
        super.copyDeepContent(atom)
        atom.innerList = this.innerList?.copyDeep()
        return atom
    }

    override fun finalized(): MTAccent {
        val newAccent: MTAccent = this.copyDeep()
        super.finalized(newAccent)
        newAccent.innerList = newAccent.innerList?.finalized()
        return newAccent
    }

}

// Spaces  are  KMTMathAtomSpace with a float for space and no nucleus
class MTMathSpace() : MTMathAtom(MTMathAtomType.KMTMathAtomSpace, "") {

    var space: Float = 0.0f

    constructor(sp: Float) : this() {
        space = sp
    }


    override fun copyDeep(): MTMathSpace {
        val atom = MTMathSpace(space)
        super.copyDeepContent(atom)
        return atom
    }


}

/**
@typedef MTLineStyle
@brief Styling of a line of math
 */
enum class MTLineStyle {
    /// Display style
    KMTLineStyleDisplay,
    /// Text style (inline)
    KMTLineStyleText,
    /// Script style (for sub/super scripts)
    KMTLineStyleScript,
    /// Script script style (for scripts of scripts)
    KMTLineStyleScriptScript
}

// Styles are  KMTMathAtomStyle with a MTLineStyle and no nucleus
class MTMathStyle() : MTMathAtom(MTMathAtomType.KMTMathAtomStyle, "") {

    var style: MTLineStyle = MTLineStyle.KMTLineStyleDisplay

    constructor(st: MTLineStyle) : this() {
        style = st
    }


    override fun copyDeep(): MTMathStyle {
        val atom = MTMathStyle(style)
        super.copyDeepContent(atom)
        return atom
    }


}


// Colors are always KMTMathAtomColor type with a string for the color
class MTMathColor : MTMathAtom(MTMathAtomType.KMTMathAtomColor, "") {


    /// The inner math list
    var innerList: MTMathList? = null
    var colorString: String? = null


    override fun toLatexString(): String {
        var str = "\\color"

        str += "{$this.colorString}{$this.innerList}"

        return super.toStringSubs(str)
    }

    override fun copyDeep(): MTMathColor {
        val atom = MTMathColor()
        super.copyDeepContent(atom)
        atom.innerList = this.innerList?.copyDeep()
        atom.colorString = this.colorString
        return atom
    }

    override fun finalized(): MTMathColor {
        val newColor: MTMathColor = this.copyDeep()
        super.finalized(newColor)
        newColor.innerList = newColor.innerList?.finalized()
        return newColor
    }

}


// Colors are always KMTMathAtomColor type with a string for the color
class MTMathTextColor : MTMathAtom(MTMathAtomType.KMTMathAtomTextColor, "") {


    /// The inner math list
    var innerList: MTMathList? = null
    var colorString: String? = null


    override fun toLatexString(): String {
        var str = "\\textcolor"

        str += "{$this.colorString}{$this.innerList}"

        return super.toStringSubs(str)
    }

    override fun copyDeep(): MTMathTextColor {
        val atom = MTMathTextColor()
        super.copyDeepContent(atom)
        atom.innerList = this.innerList?.copyDeep()
        atom.colorString = this.colorString
        return atom
    }

    override fun finalized(): MTMathTextColor {
        val newColor: MTMathTextColor = this.copyDeep()
        super.finalized(newColor)
        newColor.innerList = newColor.innerList?.finalized()
        return newColor
    }

}
