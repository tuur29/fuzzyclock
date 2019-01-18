package net.tuurlievens.fuzzyclockwatchface

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.wearable.complications.ComplicationHelperActivity
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderChooserIntent
import android.support.wearable.complications.ProviderInfoRetriever
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import java.util.*
import java.util.concurrent.Executors


class ComplicationSettingsActivity : Activity() {

    // Selected complication id by user.
    private var selectedId: Int = 0

    private var providerInfoRetriever: ProviderInfoRetriever? = null

    private var complicationBackgrounds: HashMap<Int, ImageView?> = HashMap()
    private var complicationButtons: HashMap<Int, ImageButton?> = HashMap()

    private var defaultAddComplicationDrawable: Drawable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.complications_settings)

        // set background color
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val backgroundColor = prefs.getString("backgroundColor", "#ff000000")
        findViewById<RelativeLayout>(R.id.root).setBackgroundColor(Color.parseColor(backgroundColor))

        // show complications
        defaultAddComplicationDrawable = getDrawable(R.drawable.add_complication)
        selectedId = -1

        for (id in Complications.IDS) {

            val buttonId = resources.getIdentifier("${Complications.PREVIEW_ID_NAME[id]}_complication", "id", packageName)
            val backgroundID = resources.getIdentifier("${Complications.PREVIEW_ID_NAME[id]}_complication_background", "id", packageName)

            complicationBackgrounds[id] = findViewById(backgroundID)
            complicationButtons[id] = findViewById(buttonId)

            complicationButtons[id]?.setOnClickListener{ view ->
                for (e in complicationButtons.entries) {
                    if (view == e.value) {
                        launchComplicationHelperActivity(e.key)
                    }
                }
            }

            // Sets default as "Add Complication" icon.
            complicationButtons[id]?.setImageDrawable(defaultAddComplicationDrawable)
            complicationBackgrounds[id]?.visibility = View.INVISIBLE
        }

        // Initialization of code to retrieve active complication data for the watch face.
        providerInfoRetriever = ProviderInfoRetriever(applicationContext, Executors.newCachedThreadPool())
        providerInfoRetriever?.init()

        retrieveInitialComplicationsData()
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
            complicationButtons[id]?.setImageDrawable(defaultAddComplicationDrawable)
            complicationBackgrounds[id]?.visibility = View.INVISIBLE

        } else {
            complicationButtons[id]?.setImageIcon(complicationProviderInfo.providerIcon)
            complicationBackgrounds[id]?.visibility = View.VISIBLE
        }
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
