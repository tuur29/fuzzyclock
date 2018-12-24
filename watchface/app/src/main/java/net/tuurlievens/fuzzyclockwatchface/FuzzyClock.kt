package net.tuurlievens.fuzzyclockwatchface

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
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
import android.view.WindowInsets
import net.tuurlievens.fuzzyclock.FuzzyTextGenerator

import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*


class FuzzyClock : CanvasWatchFaceService() {

    companion object {
        private val NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        private const val INTERACTIVE_UPDATE_RATE_MS = 30*1000
        private const val MSG_UPDATE_TIME = 0
    }

    override fun onCreateEngine(): Engine {
        return Engine()
    }

    private class EngineHandler(reference: FuzzyClock.Engine) : Handler() {
        private val mWeakReference: WeakReference<FuzzyClock.Engine> = WeakReference(reference)

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
        private lateinit var mCalendar: Calendar
        private var mLowBitAmbient: Boolean = false
        private var mBurnInProtection: Boolean = false
        private var mAmbient: Boolean = false
        private val mUpdateTimeHandler: Handler = EngineHandler(this)

        private lateinit var mBackgroundPaint: Paint
        private lateinit var mClockTextPaint: TextPaint
        private lateinit var mDateTextPaint: TextPaint

        private var settingsUpdateReceiver : BroadcastReceiver? = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                loadSettings()
                updateSettings()
            }
        }

        private val dateFormat = SimpleDateFormat("E, d MMM")

        // SETTINGS
        private var language = "default"
        private var fontSize = 26
        private var textAlignment = "center"
        private var foregroundColor = "#ffffff"
        private var backgroundColor = "#000000"
        private var showDate = "always"
        private var showDigitalClock = "never"

        // the following settings only get updated after watchface restarts
        private var notifState = "hidden"
        private var showStatusbar = true

        // MAIN

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)

            loadSettings()

            setWatchFaceStyle(
                WatchFaceStyle.Builder(this@FuzzyClock)
                    .setAcceptsTapEvents(true)
                    .setStatusBarGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP)
                    .setShowUnreadCountIndicator(notifState == "count")
                    .setHideNotificationIndicator(notifState == "count" || notifState == "hidden")
                    .setHideStatusBar(!showStatusbar)
                    .build()
            )

            mCalendar = Calendar.getInstance()
            updateSettings()
        }

        private fun loadSettings() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this@FuzzyClock)

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
        }

        private fun updateSettings() {

            val size = dipToPixels(fontSize)

            mBackgroundPaint = Paint().apply {
                color = Color.parseColor(backgroundColor)
            }

            mClockTextPaint = TextPaint().apply {
                typeface = NORMAL_TYPEFACE
                color = Color.parseColor(foregroundColor)
                isAntiAlias = true
                textSize = size
            }

            mDateTextPaint = TextPaint().apply {
                typeface = NORMAL_TYPEFACE
                isAntiAlias = true
                textSize = Math.round(size * 0.65).toFloat()
                color = Color.parseColor(foregroundColor)
                alpha = Math.round(255 * 0.65).toInt()
            }

            invalidate()
        }

        override fun onDraw(canvas: Canvas, bounds: Rect) {

            // Draw the background.
            if (mAmbient) {
                canvas.drawColor(Color.BLACK)
            } else {
                canvas.drawRect(0f, 0f, bounds.width().toFloat(), bounds.height().toFloat(), mBackgroundPaint)
            }

            // create clock
            val clock: String = when {
                (showDigitalClock == "always") || (showDigitalClock == "interactive" && !mAmbient) -> {
                    val hour = mCalendar.get(Calendar.HOUR_OF_DAY)
                    val min = mCalendar.get(Calendar.MINUTE)
                    val hourText = if (hour < 10) "0" + hour.toString() else hour.toString()
                    val minText = if (min < 10) "0" + min.toString() else min.toString()
                    "$hourText:$minText"
                }
                else ->
                    FuzzyTextGenerator.create(mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), language)
            }

            val alignment = when (textAlignment) {
                "left" -> Layout.Alignment.ALIGN_LEFT
                "right" -> Layout.Alignment.ALIGN_RIGHT
                else -> Layout.Alignment.ALIGN_CENTER
            }
            val clockLayout = DynamicLayout(clock, mClockTextPaint, bounds.width(), alignment, 1F, 1F, true)

            canvas.save()
            val textXCoordinate = bounds.left.toFloat()

            if (showDate == "always" || (showDate == "interactive" && !mAmbient)) {

                // create date
                val date = dateFormat.format(mCalendar.time)
                val dateLayout = DynamicLayout(date, mDateTextPaint, bounds.width(), alignment, 1F, 1F, true)

                // draw date
                val textYCoordinate = bounds.exactCenterY() - (clockLayout.height + dateLayout.height) / 2
                val lineHeight = dateLayout.height * 3 // TODO: this isn't the correct measurement, doesn't scale correctly with textsize (ex. digitalclock)
                canvas.translate(textXCoordinate, textYCoordinate + lineHeight)
                dateLayout.draw(canvas)

                // draw clock
                canvas.translate(0F, (-lineHeight).toFloat())

            } else {
                val textYCoordinate = bounds.exactCenterY() - (clockLayout.height / 2 )
                canvas.translate(textXCoordinate, textYCoordinate)
            }

            clockLayout.draw(canvas)
            canvas.restore()
        }

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
                    // TODO: Add code to handle the tap gesture.
                }
            }
            invalidate()
        }

        // GENERAL CLOCK SETTINGS

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
            } else {
                mBackgroundPaint.color = Color.parseColor(backgroundColor)
                mClockTextPaint.color = Color.parseColor(foregroundColor)
                mDateTextPaint.color = Color.parseColor(foregroundColor)
            }

            updateTimer()
        }

        // when your watchface has been picked
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)

            if (visible) {
                registerReceiver()

                // Update time zone in case it changed while we weren't visible.
                mCalendar.timeZone = TimeZone.getDefault()
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

        /**
         * Starts the [.mUpdateTimeHandler] timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private fun updateTimer() {
            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)
            }
        }

        /**
         * Returns whether the [.mUpdateTimeHandler] timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private fun shouldTimerBeRunning(): Boolean {
            return isVisible && !isInAmbientMode
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
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
            val metrics = this@FuzzyClock.resources.displayMetrics
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), metrics)
        }
    }
}
