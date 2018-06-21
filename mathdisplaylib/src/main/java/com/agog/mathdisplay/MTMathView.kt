package com.agog.mathdisplay

import android.view.View
import com.agog.mathdisplay.render.MTFont
import com.agog.mathdisplay.render.MTMathListDisplay
import android.content.Context
import android.util.AttributeSet
import com.agog.mathdisplay.parse.*
import com.agog.mathdisplay.render.MTTypesetter
import com.agog.mathdisplay.MTMathView.MTTextAlignment.*
import com.agog.mathdisplay.MTMathView.MTMathViewMode.*
import android.content.res.Resources
import android.graphics.*


/** View subclass for rendering LaTeX Math.

`MTMathView` accepts either a string in LaTeX or an `MTMathList` to display. Use
`MTMathList` directly only if you are building it programmatically (e.g. using an
editor), otherwise using LaTeX is the preferable method.

The math display is centered vertically in the label. The default horizontal alignment is
is left. This can be changed by setting `textAlignment`. The math is default displayed in
 *Display* mode. This can be changed using `labelMode`.

When created it uses `MTFontManager.defaultFont` as its font. This can be changed using
the `font` parameter.
 */
class MTMathView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var displayList: MTMathListDisplay? = null
    private var _mathList: MTMathList? = null

    /**
     * Holds the error status from the last parse of the LaTeX string.
     * The errorcode of this can be checked to determine if the string was well formatted.
     */
    val lastError = MTParseError()

    /**
     * Not normally used. Only if you are building a mathlist in code.
     * Standard usage is setting a String in latex property.
     */
    var mathList: MTMathList? = null
        set(value) {
            field = value
            if (value != null) {
                latex = MTMathListBuilder.toLatexString(value)
            }
        }

    /**
     * The LaTeX Math string to display in the view.
     *
     * Sample mathview.latex = "x = \frac{-b \pm \sqrt{b^2-4ac}}{2a}"
     */
    var latex: String = ""
        set(value) {
            field = value

            val list: MTMathList? = MTMathListBuilder.buildFromString(latex, lastError)
            if (lastError.errorcode != MTParseErrors.ErrorNone) {
                this._mathList = null
            } else {
                this._mathList = list
            }
            displayList = null
            requestLayout()
            invalidate()
        }

    companion object {
        /**
         * Utility function to convert device independent pixel values to device pixels
         */
        fun convertDpToPixel(dp: Float): Float {
            val metrics = Resources.getSystem().displayMetrics
            val px = dp * (metrics.densityDpi / 160f)
            return Math.round(px).toFloat()
        }
    }

    /**
     * Different display styles supported by the `MTMathView`.
     *
     * The only significant difference between the two modes is how fractions
     * and limits on large operators are displayed.
     */
    enum class MTMathViewMode {
        /// Display mode. Equivalent to $$ in TeX
        KMTMathViewModeDisplay,
        /// Text mode. Equivalent to $ in TeX.
        KMTMathViewModeText
    }

    /**
     *  If view width is not measured to fit equation size this will specify placement within the view.
     * See **textAlignment**
     */
    enum class MTTextAlignment {
        /// Align left.
        KMTTextAlignmentLeft,
        /// Align center.
        KMTTextAlignmentCenter,
        /// Align right.
        KMTTextAlignmentRight
    }

    /**
     * If true the default parse errors will be drawn as text instead of math equation.
     * Default value is true
     */
    var displayErrorInline = true

    init {
        MTFontManager.setContext(context)
    }

    /**
     * Font used to draw the equation. See MTFontManager
     */
    var font: MTFont? = MTFontManager.defaultFont()
        set(value) {
            field = value
            displayList = null
            requestLayout()
            invalidate()
        }

    /**
     * This is in device pixels. Default value is see KDefaultFontSize
     */
    var fontSize = KDefaultFontSize // This is in device pixels.
        set(value) {
            field = value
            val of = this.font
            if (of != null) {
                val f = of.copyFontWithSize(value)
                this.font = f
            }
        }

    /**
     * Should display or text mode be used.
     */
    var labelMode = KMTMathViewModeDisplay
        set(value) {
            field = value
            displayList = null
            requestLayout()
            invalidate()
        }

    /**
     * Color of the equation if not overridden with local color changes by TeX commands
     */
    var textColor = Color.BLACK
        set(value) {
            field = value
            val dl = displayList
            if (dl != null) {
                dl.textColor = value
            }
            invalidate()
        }

    /**
     * Alignment within the view
     */
    var textAlignment = KMTTextAlignmentLeft
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    private var currentStyle = MTLineStyle.KMTLineStyleDisplay
        get() {
            return when (labelMode) {
                KMTMathViewModeDisplay -> MTLineStyle.KMTLineStyleDisplay
                KMTMathViewModeText -> MTLineStyle.KMTLineStyleText
            }
        }

    private fun displayError(): Boolean {
        return (lastError.errorcode != MTParseErrors.ErrorNone &&
                this.displayErrorInline)
    }

    /**
     * When parsing errors are drawn this will control the size of the resulting error text and therefore view measured size.
     * In device pixels
     */
    val errorFontSize = 20.0f

    private fun drawError(canvas: Canvas): Boolean {
        if (!displayError()) {
            return false
        }
        val paint = Paint()
        paint.typeface = Typeface.DEFAULT
        canvas.drawPaint(paint)
        paint.color = Color.RED
        paint.textSize = convertDpToPixel(errorFontSize)
        val r = errorBounds()
        canvas.drawText(lastError.errordesc, 0.0f, -r.top.toFloat(), paint)
        return true
    }

    private fun errorBounds(): Rect {
        if (displayError()) {
            val paint = Paint()
            paint.typeface = Typeface.DEFAULT// your preference here
            paint.textSize = convertDpToPixel(errorFontSize)
            val bounds = Rect()
            paint.getTextBounds(lastError.errordesc, 0, lastError.errordesc!!.length, bounds)
            return bounds
        } else {
            return Rect(0, 0, 0, 0)
        }
    }


    override fun onDraw(canvas: Canvas) {
        // call the super method to keep any drawing from the parent side.
        super.onDraw(canvas)

        if (drawError(canvas)) {
            return
        }

        var dl = displayList
        val ml = this._mathList
        if (ml != null && dl == null) {
            displayList = MTTypesetter.createLineForMathList(ml, font!!, currentStyle)
            dl = displayList
        }

        if (dl != null) {
            dl.textColor = this.textColor
            // Determine x position based on alignment
            val textX = when (this.textAlignment) {
                KMTTextAlignmentLeft -> paddingLeft

                KMTTextAlignmentCenter ->
                    (width - paddingLeft - paddingRight - dl.width.toInt()) / 2 + paddingLeft

                KMTTextAlignmentRight ->
                    width - dl.width.toInt() - paddingRight
            }

            val availableHeight = height - paddingBottom - paddingTop
            // center things vertically
            var eqheight = dl.ascent + dl.descent
            if (eqheight < fontSize / 2) {
                // Set the height to the half the size of the font
                eqheight = fontSize / 2
            }
            // This will put center of vertical bounds to vertical center
            val textY = (availableHeight - eqheight) / 2 + dl.descent + paddingBottom
            dl.position.x = textX.toFloat()
            dl.position.y = textY
            canvas.save()
            canvas.translate(0.0f, height.toFloat())
            canvas.scale(1.0f, -1.0f)
            dl.draw(canvas)
            canvas.restore()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // Account for padding
        val xpad = paddingLeft + paddingRight
        val ypad = paddingTop + paddingBottom

        var dl = displayList
        val ml = this._mathList
        if (ml != null && dl == null) {
            displayList = MTTypesetter.createLineForMathList(ml, font!!, currentStyle)
            dl = displayList
        }
        var height = 0.0f
        var width = 0.0f

        if (dl != null) {
            height = dl.ascent + dl.descent + ypad
            width = dl.width + xpad
        }

        val r = errorBounds()
        height = maxOf(height, r.height().toFloat())
        width = maxOf(width, r.width().toFloat())
        setMeasuredDimension((width + 1.0f).toInt(), (height + 1.0f).toInt())
    }


}