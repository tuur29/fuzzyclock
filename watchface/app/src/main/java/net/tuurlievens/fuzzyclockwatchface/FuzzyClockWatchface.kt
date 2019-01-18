package net.tuurlievens.fuzzyclockwatchface

import android.app.PendingIntent
import android.content.*
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.support.v4.graphics.ColorUtils
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationHelperActivity
import android.support.wearable.complications.rendering.ComplicationDrawable
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.text.DynamicLayout
import android.text.Layout
import android.text.TextPaint
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.SurfaceHolder
import net.tuurlievens.fuzzyclock.FuzzyTextGenerator
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class FuzzyClockWatchface : CanvasWatchFaceService() {

    // SETUP

    companion object {
        private val NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        private const val INTERACTIVE_UPDATE_RATE_MS = 30*1000
        private const val MSG_UPDATE_TIME = 0
    }

    override fun onCreateEngine(): Engine {
        return Engine()
    }

    private class EngineHandler(reference: FuzzyClockWatchface.Engine) : Handler() {
        private val mWeakReference: WeakReference<FuzzyClockWatchface.Engine> = WeakReference(reference)

        override fun handleMessage(msg: Message) {
            val engine = mWeakReference.get()
            if (engine != null) {
                when (msg.what) {
                    MSG_UPDATE_TIME -> engine.handleUpdateTimeMessage()
                }
            }
        }
    }

    inner class Engine : CanvasWatchFaceService.Engine() {

        private val myPackageName = "net.tuurlievens.fuzzyclockwatchface"
        private var mLowBitAmbient: Boolean = false
        private var mBurnInProtection: Boolean = false
        private var mAmbient: Boolean = false
        private val mUpdateTimeHandler: Handler = EngineHandler(this)

        /* Maps active complication ids to the data and drawables that complication. Note: Data will only be
         * present if the user has chosen a provider via the settings activity for the watch face.
         */
        private var activeComplicationData: HashMap<Int, ComplicationData> = HashMap()
        private var activeComplicationDrawable: HashMap<Int, ComplicationDrawable> = HashMap()

        private var currentScreen: Int = 0

        private lateinit var mBackgroundPaint: Paint
        private lateinit var mClockTextPaint: TextPaint
        private lateinit var mDateTextPaint: TextPaint

        private var settingsUpdateReceiver : BroadcastReceiver? = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                loadSettings()
                updateSettings()
            }
        }

        // SETTINGS
        private var language = "en"
        private var fontSize = 26
        private var textAlignment = "center"
        private var foregroundColor = "#ffffff"
        private var backgroundColor = "#000000"
        private var showDate = "always"
        private var showDigitalClock = "never"
        private var simplerDate = true

        // the following settings only get updated after watchface restarts
        private var notifState = "hidden"
        private var showStatusbar = true

        private var foreground: Int = 0
        private var lighterforeground: Int = 0
        private var complicationcolor: Int = 0

        // MAIN

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)

            loadSettings()
            setupComplications()
            updateSettings()
        }

        private fun loadSettings() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this@FuzzyClockWatchface)

            val pickedLanguage = prefs.getString("language", language)
            language = (if (pickedLanguage == "default") Locale.getDefault().language else pickedLanguage) ?: language
            fontSize = prefs.getString("fontSize", fontSize.toString()).toInt()
            textAlignment = prefs.getString("textAlignment", textAlignment)
            foregroundColor = prefs.getString("foregroundColor", foregroundColor)
            backgroundColor = prefs.getString("backgroundColor", backgroundColor)
            showDate = prefs.getString("showDate", showDate)
            notifState = prefs.getString("notifState", notifState)
            showStatusbar = prefs.getBoolean("showStatusbar", showStatusbar)
            showDigitalClock = prefs.getString("showDigitalClock", showDigitalClock)
            simplerDate = prefs.getBoolean("simplerDate", simplerDate)

            foreground = Color.parseColor(foregroundColor)
            lighterforeground = ColorUtils.setAlphaComponent(foreground, 150)
            complicationcolor = if (Color.red(foreground)*0.299 + Color.green(foreground)*0.587 + Color.blue(foreground)*0.114 > 186) {
                ColorUtils.setAlphaComponent(Color.BLACK, 100)
            } else {
                ColorUtils.setAlphaComponent(Color.WHITE, 100)
            }
        }

        private fun updateSettings() {

            val size = dipToPixels(fontSize)

            mBackgroundPaint = Paint().apply {
                color = Color.parseColor(backgroundColor)
            }

            mClockTextPaint = TextPaint().apply {
                typeface = NORMAL_TYPEFACE
                color = foreground
                isAntiAlias = true
                textSize = size
            }

            mDateTextPaint = TextPaint().apply {
                typeface = NORMAL_TYPEFACE
                isAntiAlias = true
                textSize = Math.round(size * 0.65).toFloat()
                color = lighterforeground
            }

            for (entry in activeComplicationDrawable.entries) {
                applyComplicationSettings(entry.key, entry.value)
            }

            setWatchFaceStyle(
                WatchFaceStyle.Builder(this@FuzzyClockWatchface)
                    .setAcceptsTapEvents(true)
                    .setStatusBarGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP)
                    .setShowUnreadCountIndicator(notifState == "count")
                    .setHideNotificationIndicator(notifState == "count" || notifState == "hidden")
                    .setHideStatusBar(!showStatusbar)
                    .build()
            )

            invalidate()
        }

        // DRAWING FACES

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            drawBackground(canvas, bounds)
            when(currentScreen) {
                1 -> drawComplicationsScreen(canvas, bounds)
                else -> drawWatchScreen(canvas, bounds)
            }
        }

        private fun drawBackground(canvas: Canvas, bounds: Rect) {
            if (mAmbient) {
                canvas.drawColor(Color.BLACK)
            } else {
                canvas.drawRect(0f, 0f, bounds.width().toFloat(), bounds.height().toFloat(), mBackgroundPaint)
            }
        }


        // do these static calculations once
        val preparedPadding = Math.round(dipToPixels(18))

        private fun drawWatchScreen(canvas: Canvas, bounds: Rect) {
            val calendar = Calendar.getInstance()

            // resize textsize when in ambient
            if ((showDigitalClock == "always") || (showDigitalClock == "interactive" && !mAmbient)) {
                mClockTextPaint.textSize = dipToPixels(Math.round(fontSize * 1.75F))
            } else {
                mClockTextPaint.textSize = dipToPixels(fontSize)
            }

            // create clock
            val clock: String = when {
                (showDigitalClock == "always") || (showDigitalClock == "interactive" && !mAmbient) -> {

                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val min = calendar.get(Calendar.MINUTE)
                    val hourText = if (hour < 10) "0" + hour.toString() else hour.toString()
                    val minText = if (min < 10) "0" + min.toString() else min.toString()
                    "$hourText:$minText"
                }
                else ->
                    FuzzyTextGenerator.create(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), language)
            }

            val alignment = when (textAlignment) {
                "left" -> Layout.Alignment.ALIGN_NORMAL
                "right" -> Layout.Alignment.ALIGN_OPPOSITE
                else -> Layout.Alignment.ALIGN_CENTER
            }
            val clockLayout = DynamicLayout(clock, mClockTextPaint, bounds.width() - preparedPadding*2, alignment, 1F, 1F, true)

            canvas.save()
            val textXCoordinate = bounds.left.toFloat() + preparedPadding

            if (showDate == "always" || (showDate == "interactive" && !mAmbient)) {

                // create date
                val loc = Locale(language)
                val format = if (simplerDate) SimpleDateFormat("EEEE", loc) else SimpleDateFormat("E, d MMM", loc)
                val date = format.format(calendar.time)
                val dateLayout = DynamicLayout(date, mDateTextPaint, bounds.width() - preparedPadding*2, alignment, 1F, 1F, true)

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
            canvas.restore()
        }

        private fun drawComplicationsScreen(canvas: Canvas, bounds: Rect) {

            for (entry in activeComplicationDrawable.entries) {
                val drawable = entry.value
                drawable.bounds = Complications.getPosition(entry.key, bounds)
                drawable.draw(canvas)
            }

        }

        // COMPLICATIONS

        private fun createComplication(id: Int, data: ComplicationData?) {

            val drawable = (getDrawable(R.drawable.custom_complication_styles) as ComplicationDrawable).apply{
                setContext(applicationContext)
            }

            if (data != null) {
                activeComplicationData[id] = data
                drawable.setComplicationData(data)
            } else {
                activeComplicationData.remove(id)
            }

            applyComplicationSettings(id, drawable)
            activeComplicationDrawable[id] = drawable
        }

        private fun setupComplications() {
            for (id in Complications.IDS) {
                createComplication(id, null)
            }
            setActiveComplications(*Complications.IDS)
        }

        private fun applyComplicationSettings(id: Int, drawable: ComplicationDrawable) {
            drawable.setTextColorActive(foreground)
            drawable.setRangedValuePrimaryColorActive(foreground)
            drawable.setTitleColorActive(foreground)
            drawable.setIconColorActive(foreground)

            drawable.setRangedValueSecondaryColorActive(lighterforeground)
            drawable.setHighlightColorActive(lighterforeground)

            if (activeComplicationData[id]?.type != ComplicationData.TYPE_RANGED_VALUE)
                drawable.setBorderColorActive(ColorUtils.setAlphaComponent(foreground, 100))

            drawable.setBackgroundColorActive(complicationcolor)
        }

        override fun onComplicationDataUpdate(id: Int, data: ComplicationData) {
            if (data.type == ComplicationData.TYPE_EMPTY) {
                activeComplicationData.remove(id)
                activeComplicationDrawable.remove(id)
            } else {
                createComplication(id, data)
            }

            invalidate()
        }

        // HANDLE TAPS

        override fun onTapCommand(tapType: Int, x: Int, y: Int, eventTime: Long) {
            when (tapType) {
                WatchFaceService.TAP_TYPE_TOUCH -> {
                    // The user has started touching the screen.
                }
                WatchFaceService.TAP_TYPE_TOUCH_CANCEL -> {
                    // The user has started a different gesture or otherwise cancelled the tap.
                }
                WatchFaceService.TAP_TYPE_TAP -> {
                    // The user has completed the tap gesture.

                    // check if complication tapped
                    if (currentScreen == 1) {
                        val tappedComplicationId = getTappedComplicationId(x, y)
                        if (tappedComplicationId != -1) {
                            onComplicationTap(tappedComplicationId)
                            return
                        }
                    }

                    if (activeComplicationData.size > 0)
                        currentScreen = (currentScreen + 1) % 2
                }
            }
            invalidate()
        }

        private fun getTappedComplicationId(x: Int, y: Int): Int {

            val currentTimeMillis = System.currentTimeMillis()

            for (entry in activeComplicationDrawable.entries) {
                val complicationDrawable = entry.value
                val complicationId = entry.key
                val complicationData = activeComplicationData[complicationId]

                if (complicationData != null
                    && complicationData.isActive(currentTimeMillis)
                    && complicationData.type != ComplicationData.TYPE_NOT_CONFIGURED
                    && complicationData.type != ComplicationData.TYPE_EMPTY
                ) {

                    val complicationBoundingRect = complicationDrawable.bounds

                    // Give a bit more space to go to the next screen
                    complicationBoundingRect.inset(10, 10)

                    if (complicationBoundingRect.width() > 0) {
                        if (complicationBoundingRect.contains(x, y)) {
                            return complicationId
                        }
                    }
                }
            }
            return -1
        }

        private fun onComplicationTap(complicationId: Int) {

            val complicationData = activeComplicationData[complicationId]
            if (complicationData != null) {

                if (complicationData.tapAction != null) {
                    try {
                        complicationData.tapAction.send()
                    } catch (e: PendingIntent.CanceledException) {
                        Log.e("COMPLICATIONS", "onComplicationTap() tap action error: $e")
                    }

                } else if (complicationData.type == ComplicationData.TYPE_NO_PERMISSION) {
                    // Watch face does not have permission to receive complication data, so launch
                    // permission request.
                    val componentName = ComponentName(applicationContext, FuzzyClockWatchface::class.java)
                    val permissionRequestIntent = ComplicationHelperActivity.createPermissionRequestHelperIntent(applicationContext, componentName)
                    startActivity(permissionRequestIntent)
                }
            }
        }

        // AMBIENT MODE

        override fun onTimeTick() {
            super.onTimeTick()
            invalidate()
        }

        override fun onPropertiesChanged(properties: Bundle) {
            super.onPropertiesChanged(properties)
            mLowBitAmbient = properties.getBoolean(
                WatchFaceService.PROPERTY_LOW_BIT_AMBIENT, false
            )
            mBurnInProtection = properties.getBoolean(
                WatchFaceService.PROPERTY_BURN_IN_PROTECTION, false
            )

            for (entry in activeComplicationDrawable.entries) {
                val complicationDrawable = entry.value
                complicationDrawable.setLowBitAmbient(mLowBitAmbient)
                complicationDrawable.setBurnInProtection(mBurnInProtection)
            }

        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            mAmbient = inAmbientMode

            if (mLowBitAmbient) {
                mClockTextPaint.isAntiAlias = !inAmbientMode
                mDateTextPaint.isAntiAlias = !inAmbientMode
            }

            if (mAmbient) {
                mBackgroundPaint.color = Color.BLACK
                mClockTextPaint.color = Color.WHITE
                mDateTextPaint.color = Color.WHITE

                currentScreen = 0

            } else {
                mBackgroundPaint.color = Color.parseColor(backgroundColor)
                mClockTextPaint.color = Color.parseColor(foregroundColor)
                mDateTextPaint.color = Color.parseColor(foregroundColor)
            }

            for (entry in activeComplicationDrawable.entries) {
                val complicationDrawable = entry.value
                complicationDrawable.setInAmbientMode(mAmbient)
            }

            updateTimer()
        }

        // RECEIVERS & TIMERS

        // when your watchface has been picked
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)

            if (visible) {
                registerReceiver()
                invalidate()
            } else {
                unregisterReceivers()
            }

            updateTimer()
        }

        private fun registerReceiver() {
            val filter = IntentFilter()
            filter.addAction("$myPackageName.REFRESH")
            registerReceiver(settingsUpdateReceiver, filter)
        }

        private fun unregisterReceivers() {
            if (settingsUpdateReceiver != null) {
                return
            }
            settingsUpdateReceiver = null
            unregisterReceiver(settingsUpdateReceiver)
        }

        override fun onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            unregisterReceivers()
            super.onDestroy()
        }

        private fun updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)
            }
        }

        private fun shouldTimerBeRunning(): Boolean {
            return isVisible && !isInAmbientMode
        }

        fun handleUpdateTimeMessage() {
            invalidate()
            if (shouldTimerBeRunning()) {
                val timeMs = System.currentTimeMillis()
                val delayMs = INTERACTIVE_UPDATE_RATE_MS - timeMs % INTERACTIVE_UPDATE_RATE_MS
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs)
            }
        }

        // HELPERS

        private fun dipToPixels(value: Int) : Float {
            val metrics = this@FuzzyClockWatchface.resources.displayMetrics
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), metrics)
        }
    }
}
