package net.tuurlievens.fuzzyclock

import android.content.Context
import android.graphics.*
import android.text.DynamicLayout
import android.text.Layout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import net.tuurlievens.fuzzyclock.text.FuzzyTextGenerator
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class ClockFaceDrawer {

    companion object {
        fun draw(canvas: Canvas, bounds: Rect, prefs: PossiblePreferences, context: Context): Array<Rect> {
            canvas.translate(0F, 0F)

            val calendar = Calendar.getInstance()

            val emphasis = when(prefs.emphasis) {
                "bold" -> Typeface.BOLD
                "italic" -> Typeface.ITALIC
                "bold_italic" -> Typeface.BOLD_ITALIC
                else -> Typeface.NORMAL
            }
            val font = when (prefs.fontFamily) {
                "sans_serif" -> Typeface.create(Typeface.SANS_SERIF, emphasis)
                "serif" -> Typeface.create(Typeface.SERIF, emphasis)
                "monospace" -> Typeface.create(Typeface.MONOSPACE, emphasis)
                else -> {
                    val fontID = context.resources.getIdentifier(prefs.fontFamily, "font", context.packageName)
                    var typeface = Typeface.create(Typeface.SANS_SERIF, emphasis)
                    try {
                        if (fontID != 0) typeface = Typeface.create(ResourcesCompat.getFont(context, fontID), emphasis)
                    } catch (e: Exception) {}
                    typeface
                }
            }

            val mClockTextPaint = TextPaint().apply {
                typeface = font
                color = prefs.foregroundColor
                isAntiAlias = true
                textSize = if (prefs.showDigitalClock) (prefs.fontSize * prefs.scaling * prefs.digitalClockScaling).toFloat() else (prefs.fontSize * prefs.scaling).toFloat()
                isAntiAlias = prefs.antialiasing
                setShadowLayer((prefs.shadowSize * prefs.scaling).toFloat(), 0F, 0F, prefs.shadowColor)
            }

            val scaledDateShadowSize = ((prefs.shadowSize * prefs.scaling) * prefs.dateFontSize / prefs.fontSize).toFloat()
            val mDateTextPaint = TextPaint().apply {
                typeface = if (prefs.useDateFont) { font } else { Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL) }
                color = prefs.dateForegroundColor
                isAntiAlias = true
                textSize = (prefs.dateFontSize * prefs.scaling).toFloat()
                isAntiAlias = prefs.antialiasing
                setShadowLayer(scaledDateShadowSize, 0F, 0F, prefs.shadowColor)
            }

            // update clock
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val min = calendar.get(Calendar.MINUTE)

            val pickedLanguage = if (prefs.language == "default") Locale.getDefault().language else prefs.language
            var clock: String = when {
                prefs.showDigitalClock -> "${hour.toString().padStart(2, '0')}:${hour.toString().padStart(2, '0')}"
                else -> FuzzyTextGenerator.create(hour, min, pickedLanguage)
            }
            if (prefs.removeLineBreak) {
                clock = clock.replace("\n", " ")
            }

            val alignment = when (prefs.textAlignment) {
                "left" -> Layout.Alignment.ALIGN_NORMAL
                "right" -> Layout.Alignment.ALIGN_OPPOSITE
                else -> Layout.Alignment.ALIGN_CENTER
            }
            val clockLayout = DynamicLayout(clock, mClockTextPaint, bounds.width() - (prefs.padding*prefs.scaling*2).roundToInt(), alignment, 1F, 1F, true)

            canvas.save()
            // used for clock and date listeners
            val hitRegions = mutableListOf(Rect(), Rect())
            val textXCoordinate = (bounds.left + prefs.padding * prefs.scaling).toFloat()
            val textYCoordinate: Float

            if (prefs.showDate) {
                // create date
                val loc = Locale(prefs.language)
                val format = if (prefs.simplerDate) SimpleDateFormat("EEEE", loc) else SimpleDateFormat("E, d MMM", loc)
                val date = format.format(calendar.time)
                val dateLayout = DynamicLayout(date, mDateTextPaint, bounds.width() - (prefs.padding*prefs.scaling*2).roundToInt(), alignment, 1F, 1F, true)

                // draw date
                textYCoordinate = bounds.exactCenterY() - (clockLayout.height + dateLayout.height) / 2
                canvas.translate(textXCoordinate, textYCoordinate + clockLayout.height)
                dateLayout.draw(canvas)

                // draw clock
                canvas.translate(0F, -clockLayout.height.toFloat())

                hitRegions[1] = Rect(
                    (textXCoordinate / prefs.scaling).roundToInt(),
                    ((textYCoordinate + clockLayout.height) / prefs.scaling).roundToInt(),
                    ((textXCoordinate + dateLayout.width) / prefs.scaling).roundToInt(),
                    ((textYCoordinate + clockLayout.height + dateLayout.height) / prefs.scaling).roundToInt()
                )

            } else {
                textYCoordinate = bounds.exactCenterY() - (clockLayout.height / 2 )
                canvas.translate(textXCoordinate, textYCoordinate)
            }
            clockLayout.draw(canvas)

            hitRegions[0] = Rect( // clock bounds
                (textXCoordinate / prefs.scaling).roundToInt(),
                (textYCoordinate / prefs.scaling).roundToInt(),
                ((textXCoordinate + clockLayout.width) / prefs.scaling).roundToInt(),
                ((textYCoordinate + clockLayout.height) / prefs.scaling).roundToInt()
            )

            return hitRegions.toTypedArray()
        }
    }

}