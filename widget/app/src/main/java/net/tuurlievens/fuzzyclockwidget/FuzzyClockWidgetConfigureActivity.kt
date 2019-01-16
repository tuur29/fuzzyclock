package net.tuurlievens.fuzzyclockwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

class FuzzyClockWidgetConfigureActivity : FragmentActivity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var settingsFragment: AllPreferencesFragment
    public var prefs: WidgetData? = null

    // create activity
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.fuzzy_clock_widget_configure)

        // set statusbar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(android.R.color.black)
        }

        // Find the widget id from the intent.
        val extras = intent.extras
        if (extras != null) {
            widgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        prefs = loadPrefs(this@FuzzyClockWidgetConfigureActivity, widgetId)

        // load preferences fragment
        settingsFragment = AllPreferencesFragment()
        supportFragmentManager.beginTransaction().replace(R.id.fragment, settingsFragment as Fragment).commit()

        // save button
        findViewById<View>(R.id.save_button).setOnClickListener{
            val context = this@FuzzyClockWidgetConfigureActivity
            savePrefs(context, widgetId, prefs!!)

            // It is the responsibility of the configuration activity to update the app widget
            updateWidget(widgetId)

            // Inform user where to press to edit current widget
            Toast.makeText(applicationContext, getString(R.string.msg_editbtn), Toast.LENGTH_SHORT).show()

            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }

    private fun updateWidget(id: Int) {
        val manager = AppWidgetManager.getInstance(application)
        UpdateWidgetService.updateWidget(this, manager, id)
    }


    companion object {

        // CRUD widget preferences
        private val PREFS_NAME = "net.tuurlievens.fuzzyclockwidget.FuzzyClockWidget"
        private val PREF_PREFIX_KEY = "fuzzyclockwidget_"

        internal fun savePrefs(context: Context, appWidgetId: Int, data: WidgetData) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putString(PREF_PREFIX_KEY + appWidgetId, data.toDataString())
            prefs.apply()
        }

        internal fun loadPrefs(context: Context, appWidgetId: Int): WidgetData {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0)
            val string = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null) ?: WidgetData.default.toDataString()
            return WidgetData.fromDataString(string)
        }

        internal fun deletePrefs(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.remove(PREF_PREFIX_KEY + appWidgetId)
            prefs.apply()
        }

    }
}

