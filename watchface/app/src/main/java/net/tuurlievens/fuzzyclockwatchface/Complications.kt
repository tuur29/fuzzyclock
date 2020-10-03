package net.tuurlievens.fuzzyclockwatchface

import android.graphics.Rect
import android.os.Build
import android.support.wearable.complications.ComplicationData


class Complications {
    companion object {

        val count = 7
        val IDS = IntArray(count) { it }

        val COMPLICATION_SUPPORTED_TYPES = arrayOf(
            intArrayOf(
                ComplicationData.TYPE_RANGED_VALUE,
                ComplicationData.TYPE_ICON,
                ComplicationData.TYPE_SHORT_TEXT,
                ComplicationData.TYPE_SMALL_IMAGE
            ),
            intArrayOf(
                ComplicationData.TYPE_RANGED_VALUE,
                ComplicationData.TYPE_ICON,
                ComplicationData.TYPE_SHORT_TEXT,
                ComplicationData.TYPE_SMALL_IMAGE
            ),
            intArrayOf(
                ComplicationData.TYPE_RANGED_VALUE,
                ComplicationData.TYPE_ICON,
                ComplicationData.TYPE_SHORT_TEXT,
                ComplicationData.TYPE_SMALL_IMAGE
            ),
            intArrayOf(
                ComplicationData.TYPE_RANGED_VALUE,
                ComplicationData.TYPE_ICON,
                ComplicationData.TYPE_SHORT_TEXT,
                ComplicationData.TYPE_SMALL_IMAGE
            ),
            intArrayOf(
                ComplicationData.TYPE_RANGED_VALUE,
                ComplicationData.TYPE_ICON,
                ComplicationData.TYPE_SHORT_TEXT,
                ComplicationData.TYPE_SMALL_IMAGE
            ),
            intArrayOf(
                ComplicationData.TYPE_RANGED_VALUE,
                ComplicationData.TYPE_ICON,
                ComplicationData.TYPE_SHORT_TEXT,
                ComplicationData.TYPE_SMALL_IMAGE
            ),
            intArrayOf(
                ComplicationData.TYPE_LONG_TEXT,
                ComplicationData.TYPE_SHORT_TEXT
            )
        )

        fun complicationsEnabled(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
        }

        fun getPosition(id: Int, bounds: Rect): Rect {

            // a unit is 10% of the smallest dimensions
            val u = (0.1 * if(bounds.width() < bounds.height()){bounds.height()}else{bounds.width()}).toInt()
            val scale = 1.2 // scale up complications
            val padding = -(Math.abs(1-scale)*2*u).toInt() // padding
            val size = (2*scale*u).toInt()

            return when(id) {

                0 -> Rect(
                    u+padding,
                    u+padding,
                    u+size,
                    u+size
                )  // top left

                1 -> Rect(
                    4*u+padding,
                    u+padding,
                    4*u+size,
                    u+size
                ) // top middle

                2 -> Rect(
                    7*u+padding,
                    u+padding,
                    7*u+size,
                    u+size
                ) // top right

                3 -> Rect(
                    u+padding,
                    4*u+padding,
                    u+size,
                    4*u+size
                ) // middle left

                4-> Rect(
                    4*u+padding,
                    4*u+padding,
                    4*u+size,
                    4*u+size
                ) // middle middle

                5 -> Rect(
                    7*u+padding,
                    4*u+padding,
                    7*u+size,
                    4*u+size
                ) // middle right

                6 -> Rect(
                    u+padding,
                    7*u+padding,
                    (u+3.5*size).toInt(),
                    7*u+size
                ) // bottom

                else -> Rect(-u,-u,-u,-u)
            }

        }

    }
}
