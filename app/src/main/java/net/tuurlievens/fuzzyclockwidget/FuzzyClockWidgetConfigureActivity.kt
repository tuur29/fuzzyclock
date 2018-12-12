package net.tuurlievens.fuzzyclockwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText

class FuzzyClockWidgetConfigureActivity : Activity() {

    // TODO: change this activity to a PreferenceActivity

    private var mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    private lateinit var mAppWidgetText: EditText

    // adding the widget
    private var mOnClickListener: View.OnClickListener = View.OnClickListener {
        val context = this@FuzzyClockWidgetConfigureActivity

        // When the button is clicked, store the string locally
        val widgetText = mAppWidgetText.text.toString()
        val data = WidgetData(widgetText.toInt())
        savePrefs(context, mAppWidgetId, data)

        // It is the responsibility of the configuration activity to update the app widget
        updateWidgets()

        // Make sure we pass back the original appWidgetId
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }


    // create activity
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)

        setContentView(R.layout.fuzzy_clock_widget_configure)
        mAppWidgetText = findViewById<View>(R.id.appwidget_text) as EditText
        findViewById<View>(R.id.add_button).setOnClickListener(mOnClickListener)

        // Find the widget id from the intent.
        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // load default values
        val prefs = loadPrefs(this@FuzzyClockWidgetConfigureActivity, mAppWidgetId)
        mAppWidgetText.setText(prefs.fontSize.toString())
    }

    private fun updateWidgets() {
        val manager = AppWidgetManager.getInstance(application)
        val ids = manager.getAppWidgetIds(ComponentName(application, FuzzyClockWidget::class.java))

        for (id in ids) {
            UpdateWidgetService.updateWidget(this, manager, id)
        }
    }

    // CRUD widget preferences

    companion object {

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

