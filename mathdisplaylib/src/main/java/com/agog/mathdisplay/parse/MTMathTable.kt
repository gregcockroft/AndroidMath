package com.agog.mathdisplay.parse


/**
@typedef MTColumnAlignment
@brief Alignment for a column of MTMathTable
 */
enum class MTColumnAlignment {
    /// Align left.
    KMTColumnAlignmentLeft,
    /// Align center.
    KMTColumnAlignmentCenter,
    /// Align right.
    KMTColumnAlignmentRight
}

class MTMathTable() : MTMathAtom(MTMathAtomType.KMTMathAtomTable, "") {


    private var alignments = mutableListOf<MTColumnAlignment>()

    // 2D variable size array of MathLists
    var cells: MutableList<MutableList<MTMathList>> = mutableListOf()

    /// The name of the environment that this table denotes.
    var environment: String? = null

    /// Spacing between each column in mu units.
    var interColumnSpacing: Float = 0.0f

    /// Additional spacing between rows in jots (one jot is 0.3 times font size).
/// If the additional spacing is 0, then normal row spacing is used are used.
    var interRowAdditionalSpacing: Float = 0.0f

    constructor(env: String?) : this() {
        environment = env
    }

    override fun copyDeep(): MTMathTable {
        val atom = MTMathTable(environment)
        super.copyDeepContent(atom)

        atom.alignments = mutableListOf()
        atom.alignments.addAll(this.alignments.toSet())

        atom.cells = mutableListOf()
        for (row in this.cells) {
            val newrow = mutableListOf<MTMathList>()
            for (i in 0 until row.size) {
                val newcol = row[i].copyDeep()
                newrow.add(newcol)
            }
            atom.cells.add(newrow)
        }

        atom.interColumnSpacing = this.interColumnSpacing
        atom.interRowAdditionalSpacing = this.interRowAdditionalSpacing

        return atom
    }

    override fun finalized(): MTMathTable {
        val newMathTable = this.copyDeep()
        super.finalized(newMathTable)
        for (row in newMathTable.cells) {
            for (i in 0 until row.size) {
                row[i] = row[i].finalized()
            }
        }
        return newMathTable
    }

    fun setCell(list: MTMathList, row: Int, column: Int) {
        if (this.cells.size <= row) {
            // Add more rows
            var i: Int = this.cells.size
            while (i <= row) {
                this.cells.add(i++, mutableListOf())
            }

        }
        val rowArray: MutableList<MTMathList> = this.cells[row]
        if (rowArray.size <= column) {
            // Add more columns
            var i: Int = rowArray.size
            while (i <= column) {
                rowArray.add(i++, MTMathList())
            }
        }
        rowArray[column] = list

    }

    fun setAlignment(alignment: MTColumnAlignment, column: Int) {

        if (this.alignments.size <= column) {
            // Add more columns
            var i: Int = this.alignments.size
            while (i <= column) {
                this.alignments.add(i++, MTColumnAlignment.KMTColumnAlignmentCenter)
            }
        }
        this.alignments[column] = alignment
    }

    fun getAlignmentForColumn(column: Int): MTColumnAlignment {
        if (this.alignments.size <= column) {
            return MTColumnAlignment.KMTColumnAlignmentCenter
        } else {
            return this.alignments[column]
        }
    }

    fun numColumns(): Int {
        var numColumns = 0
        for (row in this.cells) {
            numColumns = maxOf(numColumns, row.size)
        }
        return numColumns
    }

    fun numRows(): Int {
        return this.cells.size
    }

}

