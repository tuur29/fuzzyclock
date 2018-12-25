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
import android.util.DisplayMetrics
import android.widget.LinearLayout
import java.text.SimpleDateFormat
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.graphics.drawable.GradientDrawable
import android.os.BatteryManager
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import net.tuurlievens.fuzzyclock.FuzzyTextGenerator


class FuzzyClockDream : DreamService() {

    private val myPackageName = "net.tuurlievens.fuzzyclockscreensaver"
    private val timer = Timer("FuzzyClockTimer", true)
    private lateinit var task: TimerTask
    private val handler =  Handler()
    private var nReceiver: NotificationReceiver? = null
    private val notifications = HashMap<String, NotificationData>()
    private val ignoredPackageNames = arrayOf("android","com.android.systemui")

    private var maxTranslationDisplacement = 0.0
    private var updateSeconds = 60.0
    private var language = "default"
    private var fontSize = 36
    private var textAlignment = "center"
    private var foregroundColor = "#ffffff"
    private var backgroundColor = "#000000"
    private var removeLineBreak = false
    private var showDate = true
    private var brightScreen = false
    private var notifState = "hidden"
    private var showBattery = true
    private var simplerDate = true

    // LIFECYCLE

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        loadSettings()
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
        isScreenBright = brightScreen
        setContentView(R.layout.dream)
        applySettings()
    }

    override fun onDreamingStarted() {
        // Show initial value of clock
        task.run()
        // Update clock every minute
        timer.scheduleAtFixedRate(task, 0, (updateSeconds*1000).toLong())

        if (notifState != "hidden") {
            // Register notification receiver
            nReceiver = NotificationReceiver()
            val filter = IntentFilter()
            filter.addAction("$myPackageName.NOTIFICATION_LISTENER")
            registerReceiver(nReceiver, filter)

            // ask for all current notifications
            val intent = Intent("$myPackageName.NOTIFICATION_LISTENER_SERVICE")
            intent.putExtra("command", "list")
            sendBroadcast(intent)
        }
    }

    override fun onDreamingStopped() {
        timer.cancel()
        if (nReceiver != null) unregisterReceiver(nReceiver)
    }

    override fun onDetachedFromWindow() {}

    // HELPERS

    private fun loadSettings() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

        maxTranslationDisplacement = prefs.getString("maxTranslationDisplacement", maxTranslationDisplacement.toString()).toDouble()
        updateSeconds = prefs.getString("updateSeconds", updateSeconds.toString()).toDouble()
        language = prefs.getString("language", language)
        fontSize = prefs.getString("fontSize", fontSize.toString()).toInt()
        textAlignment = prefs.getString("textAlignment", textAlignment)
        foregroundColor = prefs.getString("foregroundColor", foregroundColor)
        backgroundColor = prefs.getString("backgroundColor", backgroundColor)
        removeLineBreak = prefs.getBoolean("removeLineBreak", removeLineBreak)
        showDate = prefs.getBoolean("showDate", showDate)
        brightScreen = prefs.getBoolean("brightScreen", brightScreen)
        notifState = prefs.getString("notifState", notifState)
        showBattery = prefs.getBoolean("showBattery", showBattery)
        simplerDate = prefs.getBoolean("simplerDate", simplerDate)
    }

    private fun applySettings() {
        // apply settings to ui
        findViewById<RelativeLayout>(R.id.root).setBackgroundColor(Color.parseColor(backgroundColor))

        findViewById<TextView>(R.id.clocktext).textSize = fontSize.toFloat()
        findViewById<TextView>(R.id.datetext).textSize = (fontSize * 0.65).toFloat()
        findViewById<TextView>(R.id.notificationcount).textSize = (fontSize * 0.65).toFloat()

        findViewById<TextView>(R.id.clocktext).setTextColor(Color.parseColor(foregroundColor))
        findViewById<TextView>(R.id.datetext).setTextColor(Color.parseColor(foregroundColor))
        findViewById<TextView>(R.id.notificationcount).setTextColor(Color.parseColor(foregroundColor))

        val batt = findViewById<View>(R.id.battery);
        val gd = batt.background.current as GradientDrawable
        gd.setColor(Color.parseColor(foregroundColor))

        val alignment = when(textAlignment) {
            "center" -> TextView.TEXT_ALIGNMENT_CENTER
            "right" -> TextView.TEXT_ALIGNMENT_TEXT_END
            else -> TextView.TEXT_ALIGNMENT_TEXT_START
        }
        findViewById<TextView>(R.id.clocktext).textAlignment = alignment
        findViewById<TextView>(R.id.datetext).textAlignment = alignment
        findViewById<TextView>(R.id.notificationcount).textAlignment = alignment
        findViewById<FlexboxLayout>(R.id.notifications).justifyContent = when(textAlignment) {
            "left" -> JustifyContent.FLEX_START
            "right" -> JustifyContent.FLEX_END
            else -> JustifyContent.CENTER
        }
        findViewById<LinearLayout>(R.id.parent).gravity = when(textAlignment) {
            "left" -> Gravity.START
            "right" -> Gravity.END
            else -> Gravity.CENTER
        }

        if (!showBattery) {
            findViewById<RelativeLayout>(R.id.root).removeView(findViewById<View>(R.id.battery))
        }
    }

    private fun createTask() {

        task = object : TimerTask() {
            override fun run() {

                handler.post {
                    // get the current timestamp
                    val calendar = Calendar.getInstance()
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    val min = calendar.get(Calendar.MINUTE)

                    // update clock
                    val pickedLanguage = if (language == "default") Locale.getDefault().language else language
                    var time = FuzzyTextGenerator.create(hour, min, pickedLanguage)
                    if (removeLineBreak) {
                        time = time.replace("\n"," ")
                    }
                    findViewById<TextView>(R.id.clocktext).text = time

                    // update date
                    if (showDate) {
                        val loc = Locale(pickedLanguage)
                        val format = if (simplerDate) SimpleDateFormat("EEEE", loc) else SimpleDateFormat("E, d MMM", loc)
                        findViewById<TextView>(R.id.datetext).text = format.format(calendar.time)
                    } else {
                        findViewById<TextView>(R.id.datetext).text = ""
                    }

                    // Show notifications
                    if (notifState != "hidden") {
                        // show notificationcount if textview still exists (only in < M SDL or manually forced)
                        if (notifState != "visible" || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                            if (notifications.size < 1)
                                findViewById<TextView>(R.id.notificationcount).text = ""
                            else
                                findViewById<TextView>(R.id.notificationcount).text = "(${notifications.size})"
                        }

                        if (notifState == "visible" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val container = findViewById<FlexboxLayout>(R.id.notifications)
                            container.removeAllViews()

                            for (notif in notifications) {
                                if (notif.value.icon != null) {
                                    val img = ImageView(this@FuzzyClockDream)
                                    img.setImageDrawable(notif.value.icon!!.loadDrawable(this@FuzzyClockDream))
                                    img.alpha = 0.65F
                                    img.imageTintList = ColorStateList.valueOf(Color.parseColor(foregroundColor))
                                    container.addView(img)

                                    val params = img.layoutParams as FlexboxLayout.LayoutParams
                                    params.setMargins(10,10,10,10)
                                    params.width = (fontSize * 2.5).toInt()
                                    params.height= (fontSize * 2.5).toInt()
                                    img.layoutParams = params
                                }
                            }
                        }
                    }

                    if (showBattery) {
                        val display = getDisplayParams()
                        val batteryStatus = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
                            this@FuzzyClockDream.registerReceiver(null, filter)
                        }
                        val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1).toDouble() / 100
                        val width = (display.widthPixels - 40) * level // 40 = 2* 20dp margin
                        findViewById<View>(R.id.battery).layoutParams.width = Math.ceil(width).toInt()
                    }

                    // move clock around (randomly max 10% of total) against burn in
                    if (maxTranslationDisplacement != 0.0) {
                        val parent = findViewById<LinearLayout>(R.id.parent)
                        val display = getDisplayParams()
                        parent.translationX = calcRandomTranslation(display.widthPixels * maxTranslationDisplacement)
                        parent.translationY = calcRandomTranslation(display.heightPixels * maxTranslationDisplacement)
                    }
                }
            }
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

    // RECEIVERS

    internal inner class NotificationReceiver : BroadcastReceiver() {
        // On notification added / removed
        override fun onReceive(context: Context, intent: Intent) {

            val parcel = intent.getParcelableExtra<NotificationData>("notification_event")
            Log.i("updatenotif packagename", parcel.packageName)
            if (ignoredPackageNames.contains(parcel.packageName))
                return

            if (parcel.type == "onNotificationPosted") {

                // add to notif count of specific package
                val item = notifications[parcel.packageName]
                if (item != null) {
                    item.count++
                    notifications[parcel.packageName] = item
                } else {
                    notifications[parcel.packageName] = parcel
                }

            } else if (parcel.type == "onNotificationRemoved") {

                // decrease notif count or remove
                val item = notifications[parcel.packageName]
                if (item != null) {
                    if (item.count < 2)
                        notifications.remove(parcel.packageName)
                    else {
                        item.count--
                        notifications[parcel.packageName] = item
                    }
                }

            }

            task.run()
        }
    }

}
