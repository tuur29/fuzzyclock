package net.tuurlievens.fuzzyclockwatchface

import android.app.PendingIntent
import android.content.*
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.preference.PreferenceManager
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationHelperActivity
import android.support.wearable.complications.rendering.ComplicationDrawable
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.util.Log
import android.view.Gravity
import android.view.SurfaceHolder
import androidx.core.graphics.ColorUtils
import net.tuurlievens.fuzzyclock.ClockFaceDrawer
import net.tuurlievens.fuzzyclock.Helpers
import java.lang.ref.WeakReference
import kotlin.collections.HashMap


class FuzzyClockWatchface : CanvasWatchFaceService() {

    // SETUP

    companion object {
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

        private var settingsUpdateReceiver : BroadcastReceiver? = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                loadSettings()
                updateSettings()
            }
        }

        private var prefs: WatchfaceData = WatchfaceData()
        private var normalprefs: WatchfaceData = WatchfaceData()
        private var ambientprefs: WatchfaceData = WatchfaceData()

        private var complicationColor: Int = Color.WHITE

        // MAIN

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)

            loadSettings()
            if (Complications.complicationsEnabled()) {
                setupComplications()
            }
            updateSettings()
        }

        private fun loadSettings() {
            val manager = PreferenceManager.getDefaultSharedPreferences(this@FuzzyClockWatchface)
            normalprefs = WatchfaceData.loadFromMap(manager.all)
            ambientprefs = WatchfaceData.loadFromMap(manager.all)
            ambientprefs.backgroundColor = Color.BLACK
            ambientprefs.foregroundColor= Color.WHITE
            ambientprefs.antialiasing = false
            ambientprefs.shadowSize = 0
            prefs = if (mAmbient) ambientprefs else normalprefs

            // Decide if complications should be light or dark color
            val foreground = Helpers.convertIntColor(prefs.foregroundColor)
            complicationColor = if (Color.red(foreground)*0.299 + Color.green(foreground)*0.587 + Color.blue(foreground)*0.114 > 186) {
                ColorUtils.setAlphaComponent(Color.BLACK, 100)
            } else {
                ColorUtils.setAlphaComponent(Color.WHITE, 100)
            }
        }

        private fun updateSettings() {

            for (entry in activeComplicationDrawable.entries) {
                applyComplicationSettings(entry.key, entry.value)
            }

            setWatchFaceStyle(
                WatchFaceStyle.Builder(this@FuzzyClockWatchface)
                    .setAcceptsTapEvents(true)
                    .setStatusBarGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP)
                    .setShowUnreadCountIndicator(prefs.notifState == "count")
                    .setHideNotificationIndicator(prefs.notifState == "count" || prefs.notifState == "hidden")
                    .setHideStatusBar(!prefs.showStatusbar)
                    .build()
            )

            invalidate()
        }

        // DRAWING FACES

        override fun onDraw(canvas: Canvas, bounds: Rect) {

            // for some reason these checks don't work in updateSettings()
            normalprefs.showDate = !prefs.showDateString.contentEquals("never")
            normalprefs.showDigitalClock = !prefs.showDigitalClockString.contentEquals("never")

            ambientprefs.showDate = prefs.showDateString.contentEquals("always")
            ambientprefs.showDigitalClock = prefs.showDigitalClockString.contentEquals("always")

            // start drawing
            drawBackground(canvas, bounds)
            when(currentScreen) {
                1 -> drawComplicationsScreen(canvas, bounds)
                else -> ClockFaceDrawer.draw(canvas, bounds, prefs, applicationContext)
            }
        }

        private fun drawBackground(canvas: Canvas, bounds: Rect) {
            if (mAmbient) {
                canvas.drawColor(Color.BLACK)
            } else {
                canvas.drawColor(prefs.backgroundColor)
            }
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
            val foreground = Helpers.convertIntColor(prefs.foregroundColor)
            val dateForeground = Helpers.convertIntColor(prefs.dateForegroundColor)

            drawable.setTextColorActive(foreground)
            drawable.setRangedValuePrimaryColorActive(foreground)
            drawable.setTitleColorActive(foreground)
            drawable.setIconColorActive(foreground)

            drawable.setRangedValueSecondaryColorActive(dateForeground)
            drawable.setHighlightColorActive(dateForeground)

            if (activeComplicationData[id]?.type != ComplicationData.TYPE_RANGED_VALUE)
                drawable.setBorderColorActive(ColorUtils.setAlphaComponent(foreground, 100))

            drawable.setBackgroundColorActive(complicationColor)
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
                    if (Complications.complicationsEnabled()) {
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

            if (inAmbientMode) {
                currentScreen = 0
                prefs = ambientprefs
            } else {
                prefs = normalprefs
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
    }
}
