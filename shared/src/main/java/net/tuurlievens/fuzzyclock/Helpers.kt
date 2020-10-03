package net.tuurlievens.fuzzyclock

import android.content.Context
import android.graphics.Color
import android.util.TypedValue

class Helpers {
    companion object {

        fun dipToPixels(value: Int, context: Context) : Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), context.resources.displayMetrics).toInt()
        }

        fun pixelsToDip(value: Int, context: Context) : Int {
            return Math.ceil((value * context.resources.displayMetrics.density).toDouble()).toInt()
        }

        fun convertIntColor(value: Int): Int {
            val hexColour = Integer.toHexString(value)
            val rgbColour = if (hexColour.length > 6) hexColour.removeRange(0,2) else hexColour
            return Color.parseColor("#" + rgbColour)
        }

    }

}
