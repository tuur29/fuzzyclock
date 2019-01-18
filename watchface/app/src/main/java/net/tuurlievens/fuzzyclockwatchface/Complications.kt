package net.tuurlievens.fuzzyclockwatchface

import android.graphics.Rect
import android.support.wearable.complications.ComplicationData


class Complications {
    companion object {

        val IDS = intArrayOf(0, 1)
        val PREVIEW_ID_NAME = arrayOf("left", "right")

        val COMPLICATION_SUPPORTED_TYPES = arrayOf(
            intArrayOf(
                ComplicationData.TYPE_RANGED_VALUE,
                ComplicationData.TYPE_ICON,
                ComplicationData.TYPE_SHORT_TEXT,
                ComplicationData.TYPE_SMALL_IMAGE,
                ComplicationData.TYPE_LONG_TEXT
            ),
            intArrayOf(
                ComplicationData.TYPE_RANGED_VALUE,
                ComplicationData.TYPE_ICON,
                ComplicationData.TYPE_SHORT_TEXT,
                ComplicationData.TYPE_SMALL_IMAGE
            )
        )

        fun getPosition(id: Int, bounds: Rect): Rect {

            val sizeOfComplication = bounds.width() / 4
            val midpointOfScreen = bounds.width() / 2

            val horizontalOffset = (midpointOfScreen - sizeOfComplication) / 2
            val verticalOffset = midpointOfScreen - sizeOfComplication / 2

            return when(id) {
                0 -> Rect( // left
                        horizontalOffset,
                        verticalOffset,
                        horizontalOffset + sizeOfComplication,
                        verticalOffset + sizeOfComplication
                    )
                1 -> Rect( // right
                    midpointOfScreen + horizontalOffset,
                    verticalOffset,
                    midpointOfScreen + horizontalOffset + sizeOfComplication,
                    verticalOffset + sizeOfComplication
                )
                else -> Rect()
            }

        }

    }
}
