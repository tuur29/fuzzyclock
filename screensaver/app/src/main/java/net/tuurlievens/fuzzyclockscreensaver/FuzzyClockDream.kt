package net.tuurlievens.fuzzyclockscreensaver

import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.preference.PreferenceManager
import android.service.dreams.DreamService
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import java.util.*
import android.widget.LinearLayout
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.Gravity
import android.widget.ImageView
import android.graphics.drawable.GradientDrawable
import android.os.BatteryManager
import android.util.DisplayMetrics
import android.util.Log
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import net.tuurlievens.fuzzyclock.ClockFaceDrawer
import net.tuurlievens.fuzzyclock.Helpers
import java.lang.IllegalArgumentException


class FuzzyClockDream : DreamService() {

    private val myPackageName = "net.tuurlievens.fuzzyclockscreensaver"
    private val timer = Timer("FuzzyClockTimer", true)
    private lateinit var task: TimerTask
    private val handler =  Handler()
    private var nReceiver: NotificationReceiver? = null
    private var notifications: Array<NotificationData> = arrayOf()

    val defaultNotificationRefreshTimeout = 300
    var lastNotificationRefreshTime = 0L

    private var prefs = DreamData()
    private var normalFontSize = prefs.fontSize

    // LIFECYCLE

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val manager = PreferenceManager.getDefaultSharedPreferences(this)
        prefs = DreamData.loadFromMap(manager.all)
        normalFontSize = prefs.fontSize
        prefs.fontSize = Math.round(prefs.fontSize * getDisplayParams().scaledDensity) // manually scale up fontSize

        createTask()

        // Hide system UI
        isFullscreen = true
        window.decorView.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
            } else {
                systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        }

        // Configure dream
        isInteractive = false
        isScreenBright = prefs.brightScreen
        setContentView(R.layout.dream)
        applySettings()
    }

    override fun onDreamingStarted() {

        if (prefs.notifState == "hidden") {
            // Show initial value of clock
            task.run()

        } else {
            // Register notification receiver
            nReceiver = NotificationReceiver()
            val filter = IntentFilter()
            filter.addAction("$myPackageName.NOTIFICATION_LISTENER")
            registerReceiver(nReceiver, filter)

            // request also updates clock
            requestNotificationUpdate()
        }

        // Update clock every minute
        timer.scheduleAtFixedRate(task, 0, (prefs.updateSeconds*1000).toLong())

    }

    override fun onDreamingStopped() {
        timer.cancel()
        if (nReceiver != null) unregisterReceiver(nReceiver)
    }

    override fun onDetachedFromWindow() {}

    // HELPERS

    private fun applySettings() {
        // apply settings to ui
        findViewById<RelativeLayout>(R.id.root).setBackgroundColor(convertIntColor(prefs.backgroundColor))
        findViewById<TextView>(R.id.notificationcount).setTextColor(convertIntColor(prefs.foregroundColor))
        findViewById<TextView>(R.id.notificationcount).setShadowLayer(prefs.shadowSize.toFloat(), 0F, 0F, convertIntColor(prefs.shadowColor))
        findViewById<TextView>(R.id.notificationcount).textSize = (normalFontSize * 0.65).toFloat()

        val alignment = when(prefs.textAlignment) {
            "center" -> TextView.TEXT_ALIGNMENT_CENTER
            "right" -> TextView.TEXT_ALIGNMENT_TEXT_END
            else -> TextView.TEXT_ALIGNMENT_TEXT_START
        }
        findViewById<TextView>(R.id.notificationcount).textAlignment = alignment
        findViewById<FlexboxLayout>(R.id.notifications).justifyContent = when(prefs.textAlignment) {
            "left" -> JustifyContent.FLEX_START
            "right" -> JustifyContent.FLEX_END
            else -> JustifyContent.CENTER
        }
        findViewById<LinearLayout>(R.id.parent).gravity = when(prefs.textAlignment) {
            "left" -> Gravity.START
            "right" -> Gravity.END
            else -> Gravity.CENTER
        }
        val layout = findViewById<FlexboxLayout>(R.id.notifications).layoutParams as LinearLayout.LayoutParams
        layout.setMargins(prefs.padding, prefs.padding, prefs.padding, prefs.padding)
        findViewById<FlexboxLayout>(R.id.notifications).layoutParams = layout

        val batt = findViewById<View>(R.id.battery);
        val gd = batt.background.current as GradientDrawable
        gd.setColor(convertIntColor(prefs.foregroundColor))

        if (!prefs.showBattery) {
            findViewById<RelativeLayout>(R.id.root).removeView(findViewById<View>(R.id.battery))
        }
    }

    private fun createTask() {

        task = object : TimerTask() {
            override fun run() {

                val now = System.currentTimeMillis() / 1000
                if (lastNotificationRefreshTime > 0 && now - lastNotificationRefreshTime > defaultNotificationRefreshTimeout) {
                    lastNotificationRefreshTime = now
                    requestNotificationUpdate()
                    return
                }

                handler.post {
                    Log.i("NOTIF","update clock")

                    // show clock
                    val width = getDisplayParams().widthPixels
                    val height = getDisplayParams().heightPixels

                    val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(image)
                    val hitRegions = ClockFaceDrawer.draw(canvas, Rect(0, 0, width, height), prefs, applicationContext)
                    findViewById<ImageView>(R.id.canvas).setImageBitmap(image)

                    // decrease canvas size to actual in use
                    val params = findViewById<ImageView>(R.id.canvas).layoutParams as LinearLayout.LayoutParams
                    params.height = hitRegions.last().bottom - hitRegions.first().top
                    findViewById<ImageView>(R.id.canvas).layoutParams = params

                    if (prefs.notifState != "hidden") {
                        // show notificationcount if textview still exists (only in < M SDL or manually forced)
                        if (prefs.notifState == "count" || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                            if (notifications.isEmpty()) {
                                findViewById<TextView>(R.id.notificationcount).text = ""
                            } else {
                                // val count = notifications.map { n -> n.count }.reduce{ a, c -> c }
                                findViewById<TextView>(R.id.notificationcount).text = "(${notifications.size})"
                            }

                        } else if (prefs.notifState == "visible" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val container = findViewById<FlexboxLayout>(R.id.notifications)
                            container.removeAllViews()

                            for (notif in notifications) {
                                if (notif.icon != null) {
                                    val img = ImageView(this@FuzzyClockDream)
                                    img.setImageDrawable(notif.icon!!.loadDrawable(this@FuzzyClockDream))
                                    img.alpha = 0.65F
                                    img.imageTintList = ColorStateList.valueOf(convertIntColor(prefs.foregroundColor))
                                    container.addView(img)

                                    val params = img.layoutParams as FlexboxLayout.LayoutParams
                                    params.setMargins(10,10,10,10)
                                    params.width = 64
                                    params.height= 64
                                    img.layoutParams = params
                                }
                            }
                        }
                    }

                    if (prefs.showBattery) {
                        val display = getDisplayParams()
                        val batteryStatus = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                            this@FuzzyClockDream.registerReceiver(null, filter)
                        }
                        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1).toDouble() / 100
                        val width = (display.widthPixels - 40) * level // 40 = 2* 20dp margin
                        findViewById<View>(R.id.battery).layoutParams.width = Math.ceil(width).toInt()
                    }

                    // move clock around (randomly max 10% of total) against burn in
                    if (prefs.maxTranslationDisplacement != 0.0) {
                        val parent = findViewById<LinearLayout>(R.id.parent)
                        val display = getDisplayParams()
                        parent.translationX = calcRandomTranslation(display.widthPixels * prefs.maxTranslationDisplacement)
                        parent.translationY = calcRandomTranslation(display.heightPixels * prefs.maxTranslationDisplacement)
                    }
                }
            }
        }

    }

    private fun requestNotificationUpdate() {
        if (prefs.notifState != "hidden") {
            val intent = Intent("$myPackageName.NOTIFICATION_LISTENER_SERVICE")
            sendBroadcast(intent)
        }
    }

    private fun getDisplayParams() : DisplayMetrics {
        val display = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(display)
        return display
    }

    private fun calcRandomTranslation(max: Double) : Float {
        val random = Random().nextFloat()
        return Math.round( random*max*2 - max ).toFloat()
    }

    private fun convertIntColor(value: Int): Int {
        return Color.parseColor("#" + Integer.toHexString(value))
    }

    // RECEIVERS

    internal inner class NotificationReceiver : BroadcastReceiver() {
        // On notification added / removed
        override fun onReceive(context: Context, intent: Intent) {

            val parcelables = intent.getParcelableArrayListExtra<NotificationData>("notification_event")
            if (parcelables != null)
                notifications = parcelables.toTypedArray()

            task.run()
        }
    }

}
