package net.tuurlievens.fuzzyclock
// TODO: move this to UI package and generator classes to text package
import android.content.Context
import android.graphics.*
import android.text.DynamicLayout
import android.text.Layout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import java.text.SimpleDateFormat
import java.util.*

class ClockFaceDrawer {

    companion object {
        fun draw(canvas: Canvas, bounds: Rect, prefs: PossiblePreferences, context: Context) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            canvas.translate(0F, 0F)

            val calendar = Calendar.getInstance()
            val foregroundColor = "#" + Integer.toHexString(prefs.foregroundColor)
            val lighterForeground = ColorUtils.setAlphaComponent(prefs.foregroundColor, 150)
            val shadowColor = "#" + Integer.toHexString(prefs.shadowColor)

            val fontFile = context.resources.getIdentifier(prefs.fontFamily, "font", context.packageName)
            var font = Typeface.DEFAULT
            if (fontFile != 0) {
                try {
                    font = ResourcesCompat.getFont(context, fontFile)
                } catch (e: Exception) {}
            }

            val mClockTextPaint = TextPaint().apply {
                typeface = font
                color = Color.parseColor(foregroundColor)
                isAntiAlias = true
                textSize = prefs.fontSize.toFloat()
                setShadowLayer(prefs.shadowSize.toFloat(), 0F, 0F, Color.parseColor(shadowColor))
            }

            val mDateTextPaint = TextPaint().apply {
                typeface = if (prefs.useDateFont) { Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL) } else { font }
                isAntiAlias = true
                textSize = Math.round(prefs.fontSize * 0.65).toFloat()
                color = lighterForeground
                setShadowLayer(prefs.shadowSize.toFloat(), 0F, 0F, ColorUtils.setAlphaComponent(Color.parseColor(shadowColor), 150))
            }

            // update clock
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val min = calendar.get(Calendar.MINUTE)

            val pickedLanguage = if (prefs.language == "default") Locale.getDefault().language else prefs.language
            var clock = FuzzyTextGenerator.create(hour, min, pickedLanguage)
            if (prefs.removeLineBreak) {
                clock = clock.replace("\n", " ")
            }

            val alignment = when (prefs.textAlignment) {
                "left" -> Layout.Alignment.ALIGN_NORMAL
                "right" -> Layout.Alignment.ALIGN_OPPOSITE
                else -> Layout.Alignment.ALIGN_CENTER
            }
            val clockLayout = DynamicLayout(clock, mClockTextPaint, bounds.width() - prefs.padding*2, alignment, 1F, 1F, true)

            canvas.save()
            val textXCoordinate = bounds.left.toFloat() + prefs.padding

            if (prefs.showDate) {
                // create date
                val loc = Locale(prefs.language)
                val format = if (prefs.simplerDate) SimpleDateFormat("EEEE", loc) else SimpleDateFormat("E, d MMM", loc)
                val date = format.format(calendar.time)
                val dateLayout = DynamicLayout(date, mDateTextPaint, bounds.width() - prefs.padding*2, alignment, 1F, 1F, true)

                // draw date
                val textYCoordinate = bounds.exactCenterY() - (clockLayout.height + dateLayout.height) / 2
                val lineHeight = mClockTextPaint.textSize * clockLayout.lineCount * 1.25F
                canvas.translate(textXCoordinate, textYCoordinate + lineHeight)
                dateLayout.draw(canvas)

                // draw clock
                canvas.translate(0F, -lineHeight)

            } else {
                val textYCoordinate = bounds.exactCenterY() - (clockLayout.height / 2 )
                canvas.translate(textXCoordinate, textYCoordinate)
            }

            clockLayout.draw(canvas)
        }
    }

}