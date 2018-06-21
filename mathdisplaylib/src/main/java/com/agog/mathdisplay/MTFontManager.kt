package com.agog.mathdisplay

import android.content.Context
import android.content.res.AssetManager
import com.agog.mathdisplay.parse.MathDisplayException
import com.agog.mathdisplay.render.MTFont


const val KDefaultFontSize = 20f

class MTFontManager {
    companion object {
        private var assets: AssetManager? = null
        private val nameToFontMap: HashMap<String, MTFont> = HashMap<String, MTFont>()

        /*
            @param name  filename in that assets directory of the opentype font minus the otf extension
            @param size  device pixels
         */
        fun fontWithName(name: String, size: Float): MTFont? {
            var f = nameToFontMap[name]
            if (f == null) {
                val a = assets
                if (a == null) {
                    throw MathDisplayException("MTFontManager assets is null")
                } else {
                    f = MTFont(a, name, size)
                    nameToFontMap[name] = f
                }
                return f
            }
            if (f.fontSize == size) {
                return f
            } else {
                return f.copyFontWithSize(size)
            }
        }

        fun setContext(context: Context) {
            assets = context.assets
        }

        fun latinModernFontWithSize(size: Float): MTFont? {
            return fontWithName("latinmodern-math", size)
        }

        fun xitsFontWithSize(size: Float): MTFont? {
            return fontWithName("xits-math", size)
        }

        fun termesFontWithSize(size: Float): MTFont? {
            return fontWithName("texgyretermes-math", size)
        }

        fun defaultFont(): MTFont? {
            return latinModernFontWithSize(KDefaultFontSize)
        }

    }
}