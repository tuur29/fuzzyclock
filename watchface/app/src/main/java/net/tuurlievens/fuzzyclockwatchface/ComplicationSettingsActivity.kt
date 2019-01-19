package net.tuurlievens.fuzzyclockwatchface

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.wearable.complications.*
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import java.util.*
import java.util.concurrent.Executors
import android.view.MotionEvent


class ComplicationSettingsActivity : Activity() {

    // Selected complication id by user.
    private var selectedId: Int = 0
    private lateinit var canvas: Canvas
    private lateinit var bounds : Rect
    private var providerInfoRetriever: ProviderInfoRetriever? = null
    private var complicationDrawable: HashMap<Int, Drawable?> = HashMap()

    private var defaultAddComplicationDrawable: Drawable? = null
    private var backgroundComplicationDrawable: Drawable? = null

    private var background: Int = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.complications_settings)

        // set background color
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        background = Color.parseColor(prefs.getString("backgroundColor", "#ff000000"))

        // show complications
        defaultAddComplicationDrawable = getDrawable(R.drawable.ic_add_white_24dp)
        backgroundComplicationDrawable = getDrawable(R.drawable.complication_circle)

        selectedId = -1
        for (id in Complications.IDS) {
            complicationDrawable[id] = defaultAddComplicationDrawable
        }

        // Initialization of code to retrieve active complication data for the watch face.
        providerInfoRetriever = ProviderInfoRetriever(applicationContext, Executors.newCachedThreadPool())
        providerInfoRetriever?.init()

        retrieveInitialComplicationsData()
    }

    private fun setupCanvas() {
        val imageview = findViewById<ImageView>(R.id.canvas)
        bounds = Rect(imageview.left, imageview.top, imageview.right, imageview.bottom)
        val bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
        imageview.setImageBitmap(bitmap)
        canvas = Canvas(bitmap)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listenToTouches() {
        val imageview = findViewById<ImageView>(R.id.canvas)
        imageview.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val tappedComplicationId = getTappedComplicationId(event.x.toInt(), event.y.toInt())
                if (tappedComplicationId != -1) {
                    launchComplicationHelperActivity(tappedComplicationId)
                }
            }
            true
        }
    }

    private fun draw() {

        // couldn't find the proper place for this (oncreate or onviewcreated didn't work)
        setupCanvas()

        canvas.drawRect(0f, 0f, bounds.width().toFloat(), bounds.height().toFloat(), Paint().apply {
            color = background
        })

        for (entry in complicationDrawable) {
            val drawable = entry.value
            if (drawable != null) {
                val bounds = Complications.getPosition(entry.key, bounds)

                // check if is not square and resize it if so (otherwise app icon gets stretched)
                if (bounds.width() - bounds.height() > 4) {
                    if (bounds.width() > bounds.height()) {
                        val newstart = bounds.centerX() - bounds.height() / 2
                        bounds.left = newstart
                        bounds.right = newstart + bounds.height()
                    } else {
                        val newstart = bounds.centerY() - bounds.width() / 2
                        bounds.top = newstart
                        bounds.bottom = newstart + bounds.width()
                    }
                }

                drawable.bounds = bounds
                drawable.draw(canvas)
            }
        }

        listenToTouches()
    }

    private fun getTappedComplicationId(x: Int, y: Int): Int {

        for (entry in complicationDrawable.entries) {
            val drawable = entry.value
            val id = entry.key

            val rect = drawable?.bounds

            if (rect!!.width() > 0) {
                if (rect.contains(x, y)) {
                    Log.i("COMPLICATION","Tapped $id being $rect at $x,$y")
                    return id
                }
            }
        }
        return -1
    }

    override fun onDestroy() {
        Toast.makeText(this, resources.getText(R.string.msg_secondscreen), Toast.LENGTH_SHORT).show()
        super.onDestroy()

        // Required to release retriever for active complication data.
        providerInfoRetriever?.release()
    }

    private fun retrieveInitialComplicationsData() {

        providerInfoRetriever?.retrieveProviderInfo(
            object : ProviderInfoRetriever.OnProviderInfoReceivedCallback() {
                override fun onProviderInfoReceived( watchFaceComplicationId: Int, complicationProviderInfo: ComplicationProviderInfo?) {
                    updateComplicationViews(watchFaceComplicationId, complicationProviderInfo, (watchFaceComplicationId == Complications.count - 1))
                }
            },
            ComponentName(applicationContext, FuzzyClockWatchface::class.java),
            *Complications.IDS
        )
    }

    // Verifies the watch face supports the complication location, then launches the helper
    // class, so user can choose their complication data provider.
    private fun launchComplicationHelperActivity(id: Int) {

        selectedId = id

        if (selectedId >= 0) {

            val supportedTypes = Complications.COMPLICATION_SUPPORTED_TYPES[selectedId]

            startActivityForResult(
                ComplicationHelperActivity.createProviderChooserHelperIntent(
                    applicationContext,
                    ComponentName(applicationContext, FuzzyClockWatchface::class.java),
                    selectedId,
                    *supportedTypes
                ),
                ComplicationSettingsActivity.COMPLICATION_CONFIG_REQUEST_CODE
            )

        } else {
            Log.d("COMPLICATIONS", "Complication not supported by watch face.")
            Toast.makeText(applicationContext, "Complication not supported", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateComplicationViews(id: Int, complicationProviderInfo: ComplicationProviderInfo?, refresh: Boolean = true) {

        // layer background circle together with selected icon
        val final = LayerDrawable(arrayOf(
            backgroundComplicationDrawable?.constantState?.newDrawable()?.mutate(),
            if (complicationProviderInfo == null) {
                defaultAddComplicationDrawable?.constantState?.newDrawable()?.mutate()
            } else {
                complicationProviderInfo.providerIcon.loadDrawable(this)
            }
        )).apply{
            setLayerInset(1, 20,20,20,20)
        }

        complicationDrawable[id] = final

        if (!refresh) return

        draw()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == COMPLICATION_CONFIG_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {

            // Retrieves information for selected Complication provider.
            val complicationProviderInfo = data.getParcelableExtra<ComplicationProviderInfo>(ProviderChooserIntent.EXTRA_PROVIDER_INFO)

            if (selectedId >= 0) {
                updateComplicationViews(selectedId, complicationProviderInfo)
            }
        }
    }

    companion object {
        internal const val COMPLICATION_CONFIG_REQUEST_CODE = 1001
    }
}
