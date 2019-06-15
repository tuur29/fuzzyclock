package net.tuurlievens.fuzzyclock

import android.content.Context
import android.util.TypedValue

// TODO: use helpers in widget and watchface
class Helpers {
    companion object {

        fun dipToPixels(value: Int, context: Context) : Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), context.resources.displayMetrics).toInt()
        }

        fun pixelsToDip(value: Int, context: Context) : Int {
            return Math.ceil((value * context.resources.displayMetrics.density).toDouble()).toInt()
        }

    }

}
