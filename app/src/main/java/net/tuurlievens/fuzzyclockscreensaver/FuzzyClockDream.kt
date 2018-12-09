package net.tuurlievens.fuzzyclockscreensaver

import android.os.Build
import android.os.Handler
import android.preference.PreferenceManager
import android.service.dreams.DreamService
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import java.util.*
import android.util.DisplayMetrics


class FuzzyClockDream : DreamService() {

    private var timer = Timer("FuzzyClockTimer", true)
    private lateinit var task: TimerTask
    private var handler =  Handler()

    private var maxTranslationDisplacement = 0.0
    private var updateSeconds = 60.0
    private var language = "default"
    private var fontSize = 36

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

        // Exit dream upon user touch
        isInteractive = false
        // Dim screen
        isScreenBright = false
        // Set the dream layout
        setContentView(R.layout.dream)

        // change fontSize
        findViewById<TextView>(R.id.clocktext).textSize = fontSize.toFloat()
    }

    override fun onDreamingStarted() {
        // Show initial value of clock
        task.run()
        // Update clock every minute
        timer.scheduleAtFixedRate(task, 0, (updateSeconds*1000).toLong())
    }

    override fun onDreamingStopped() {}

    override fun onDetachedFromWindow() {
        timer.cancel()
    }

    // HELPERS

    private fun loadSettings() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        maxTranslationDisplacement = prefs.getString("maxTranslationDisplacement", maxTranslationDisplacement.toString()).toDouble()
        updateSeconds = prefs.getString("updateSeconds", updateSeconds.toString()).toDouble()
        language = prefs.getString("language", language)
        fontSize = prefs.getInt("fontSize", fontSize)
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
                    findViewById<TextView>(R.id.clocktext).text = FuzzyTextGenerator.create(hour, min, pickedLanguage)

                    // move clock around (randomly max 10% of total) against burn in
                    if (maxTranslationDisplacement != 0.0) {
                        val parent = findViewById<RelativeLayout>(R.id.clockparent)
                        val display = DisplayMetrics()
                        windowManager.defaultDisplay.getMetrics(display)

                        parent.translationX = calcRandomTranslation(display.widthPixels * maxTranslationDisplacement)
                        parent.translationY = calcRandomTranslation(display.heightPixels * maxTranslationDisplacement)
                    }
                }
            }
        }

    }

    private fun calcRandomTranslation(max: Double) : Float {
        val random = Random().nextFloat()
        return Math.round( random*max*2 - max ).toFloat()
    }

}
