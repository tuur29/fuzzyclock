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
import android.support.v4.content.ContextCompat
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.text.DynamicLayout
import android.text.Layout
import android.text.TextPaint
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.WindowInsets

import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*


class FuzzyClock : CanvasWatchFaceService() {

    companion object {
        private val NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)

        /**
         * Updates rate in milliseconds for interactive mode. We update once a second since seconds
         * are displayed in interactive mode.
         */
        private const val INTERACTIVE_UPDATE_RATE_MS = 30*1000

        /**
         * Handler message id for updating the time periodically in interactive mode.
         */
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
        private var mRegisteredTimeZoneReceiver = false

        private lateinit var mBackgroundPaint: Paint
        private lateinit var mClockTextPaint: TextPaint
        private lateinit var mDateTextPaint: TextPaint

        private val settingsUpdateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                invalidate()
            }
        }

        // settings
        private var showDate = true

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private var mLowBitAmbient: Boolean = false
        private var mBurnInProtection: Boolean = false
        private var mAmbient: Boolean = false

        private val mUpdateTimeHandler: Handler = EngineHandler(this)

        private val mTimeZoneReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                mCalendar.timeZone = TimeZone.getDefault()
                invalidate()
            }
        }

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)

            loadSettings()

            // register receiver
            val filter = IntentFilter()
            filter.addAction("$myPackageName.REFRESH")
            registerReceiver(settingsUpdateReceiver, filter)

            setWatchFaceStyle(
                WatchFaceStyle.Builder(this@FuzzyClock)
                    .setAcceptsTapEvents(true)
                    .setStatusBarGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP)
//                    .setShowUnreadCountIndicator(true) // TODO: test this
                    .build()
            )

            mCalendar = Calendar.getInstance()

            // Initializes background.
            mBackgroundPaint = Paint().apply {
                color = ContextCompat.getColor(applicationContext, R.color.background)
            }

            // Initializes Watch Face.
            mClockTextPaint = TextPaint().apply {
                typeface = NORMAL_TYPEFACE
                isAntiAlias = true
                color = ContextCompat.getColor(applicationContext, R.color.digital_text)
            }

            mDateTextPaint = TextPaint().apply {
                typeface = NORMAL_TYPEFACE
                isAntiAlias = true
                color = ContextCompat.getColor(applicationContext, R.color.digital_text)
                alpha = Math.round(255 * 0.65).toInt()
            }
        }

        private fun loadSettings() {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this@FuzzyClock)

            showDate = prefs.getBoolean("showDate", showDate)
        }

        override fun onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            unregisterReceiver(settingsUpdateReceiver)
            super.onDestroy()
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

        override fun onTimeTick() {
            super.onTimeTick()
            invalidate()
        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            mAmbient = inAmbientMode

            if (mLowBitAmbient) {
                mClockTextPaint.isAntiAlias = !inAmbientMode
                mDateTextPaint.isAntiAlias = !inAmbientMode
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer()
        }

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
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

        override fun onDraw(canvas: Canvas, bounds: Rect) {

            // Draw the background.
            if (mAmbient) {
                canvas.drawColor(Color.BLACK)
            } else {
                canvas.drawRect(
                    0f, 0f, bounds.width().toFloat(), bounds.height().toFloat(), mBackgroundPaint
                )
            }

            // create clock
            val language = Locale.getDefault().language
            val clock = FuzzyTextGenerator.create(mCalendar.get(Calendar.HOUR), mCalendar.get(Calendar.MINUTE), language)
            val clockLayout = DynamicLayout(clock, mClockTextPaint, bounds.width(), Layout.Alignment.ALIGN_CENTER, 1F, 1F, true)

            canvas.save()
            val textXCoordinate = bounds.left.toFloat()

            if (showDate) {

                // create date
                val format = SimpleDateFormat("E, d MMM")
                val date = format.format(mCalendar.time)
                val dateLayout = DynamicLayout(date, mDateTextPaint, bounds.width(), Layout.Alignment.ALIGN_CENTER, 1F, 1F, true)

                // draw date
                val textYCoordinate = bounds.exactCenterY() - (clockLayout.height + dateLayout.height) / 2
                val lineHeight = dateLayout.height * 3
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

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)

            if (visible) {
                registerReceiver()

                // Update time zone in case it changed while we weren't visible.
                mCalendar.timeZone = TimeZone.getDefault()
                invalidate()
            } else {
                unregisterReceiver()
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer()
        }

        private fun registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return
            }
            mRegisteredTimeZoneReceiver = true
            val filter = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED)
            this@FuzzyClock.registerReceiver(mTimeZoneReceiver, filter)
        }

        private fun unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return
            }
            mRegisteredTimeZoneReceiver = false
            this@FuzzyClock.unregisterReceiver(mTimeZoneReceiver)
        }

        override fun onApplyWindowInsets(insets: WindowInsets) {
            super.onApplyWindowInsets(insets)

            // Load resources that have alternate values for round watches.
            val resources = this@FuzzyClock.resources
            val isRound = insets.isRound

            val textSize = resources.getDimension(
                if (isRound)
                    R.dimen.digital_text_size_round
                else
                    R.dimen.digital_text_size
            )

            mClockTextPaint.textSize = textSize
            mDateTextPaint.textSize = Math.round(textSize * 0.65).toFloat()
        }

        /**
         * Starts the [.mUpdateTimeHandler] timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private fun updateTimer() {
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
    }
}
