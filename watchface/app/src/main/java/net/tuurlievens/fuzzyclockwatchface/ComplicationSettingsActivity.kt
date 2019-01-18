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
        defaultAddComplicationDrawable = getDrawable(R.drawable.add_complication)
        backgroundComplicationDrawable = getDrawable(R.drawable.added_complication)
        selectedId = -1

        for (id in Complications.IDS) {
            complicationDrawable[id] = defaultAddComplicationDrawable
        }

        // Initialization of code to retrieve active complication data for the watch face.
        providerInfoRetriever = ProviderInfoRetriever(applicationContext, Executors.newCachedThreadPool())
        providerInfoRetriever?.init()

        retrieveInitialComplicationsData()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupCanvas() {
        val imageview = findViewById<ImageView>(R.id.canvas)
        bounds = Rect(imageview.left, imageview.top, imageview.right, imageview.bottom)
        val bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888)
        imageview.setImageBitmap(bitmap)
        canvas = Canvas(bitmap)

        imageview.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
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

            // layer background circle together with selected icon
            val final = LayerDrawable(arrayOf(
                backgroundComplicationDrawable,
                drawable
            )).apply{
                setLayerInset(1, 20,20,20,20)
            }

            final.bounds = Complications.getPosition(entry.key, bounds)
            final.draw(canvas)
        }
    }

    private fun getTappedComplicationId(x: Int, y: Int): Int {

        for (entry in complicationDrawable.entries) {
            val complicationDrawable = entry.value
            val complicationId = entry.key

            val complicationBoundingRect = complicationDrawable?.bounds

            if (complicationBoundingRect!!.width() > 0) {
                if (complicationBoundingRect.contains(x, y)) {
                    return complicationId
                }
            }
        }
        return -1
    }

    override fun onDestroy() {
        super.onDestroy()

        // Required to release retriever for active complication data.
        providerInfoRetriever?.release()
    }

    private fun retrieveInitialComplicationsData() {

        val complicationIds = Complications.IDS

        providerInfoRetriever?.retrieveProviderInfo(
            object : ProviderInfoRetriever.OnProviderInfoReceivedCallback() {
                override fun onProviderInfoReceived( watchFaceComplicationId: Int, complicationProviderInfo: ComplicationProviderInfo?) {
                    updateComplicationViews(watchFaceComplicationId, complicationProviderInfo)
                }
            },
            ComponentName(applicationContext, FuzzyClockWatchface::class.java),
            *complicationIds
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

    fun updateComplicationViews(id: Int, complicationProviderInfo: ComplicationProviderInfo?) {

        if (complicationProviderInfo == null) {
            complicationDrawable[id] = defaultAddComplicationDrawable
        } else {
            complicationDrawable[id] = complicationProviderInfo.providerIcon.loadDrawable(this)
        }

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
