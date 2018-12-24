package net.tuurlievens.fuzzyclockwidget

import android.content.Intent
import android.util.Log
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.TypedValue
import android.widget.RemoteViews
import java.util.*
import android.app.PendingIntent
import android.graphics.Color
import android.support.v4.app.JobIntentService
import net.tuurlievens.fuzzyclock.FuzzyTextGenerator
import java.text.SimpleDateFormat


class UpdateWidgetService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        // generates random number
        Log.i("ALARM","widgets updated")
        pName = packageName

        val manager = AppWidgetManager.getInstance(this)
        val ids = manager.getAppWidgetIds(ComponentName(application, FuzzyClockWidget::class.java))

        for (id in ids) {
            updateWidget(applicationContext, manager, id)
        }
    }


    companion object {

        var pName: String = "net.tuurlievens.fuzzyclockwidget"


        // UPDATE widget view

        internal fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {

            val calendar = Calendar.getInstance()
            val view = RemoteViews(pName, R.layout.fuzzy_clock_widget)
            val prefs = FuzzyClockWidgetConfigureActivity.loadPrefs(context, id)

            // update clock
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val min = calendar.get(Calendar.MINUTE)

            view.setTextViewTextSize(R.id.clocktext, TypedValue.COMPLEX_UNIT_SP, prefs.fontSize.toFloat())
            view.setTextColor(R.id.clocktext, Color.parseColor(prefs.foregroundColor))

            val pickedLanguage = if (prefs.language == "default") Locale.getDefault().language else prefs.language
            var text = FuzzyTextGenerator.create(hour, min, pickedLanguage)
            if (prefs.removeLineBreak) {
               text = text.replace("\n", " ")
            }
            view.setTextViewText(R.id.clocktext, text)

            // update date
            if (prefs.showDate) {
                val loc = Locale(pickedLanguage)
                val format = if (prefs.simplerDate) SimpleDateFormat("EEEE", loc) else SimpleDateFormat("E, d MMM", loc)
                view.setTextViewText(R.id.datetext, format.format(calendar.time))
                view.setTextViewTextSize(R.id.datetext, TypedValue.COMPLEX_UNIT_SP, prefs.fontSize * 0.65F)
                view.setTextColor(R.id.datetext, Color.parseColor(prefs.foregroundColor))
            } else {
                view.setTextViewText(R.id.datetext, "")
                view.setTextViewTextSize(R.id.datetext, TypedValue.COMPLEX_UNIT_SP, 0F)
            }

            view.setOnClickPendingIntent(R.id.root, getPendingSelfIntent(context, FuzzyClockWidget.ConfigTag, id))
            manager.updateAppWidget(id, view)
        }

        private fun getPendingSelfIntent(context: Context, action: String, id: Int? = null): PendingIntent {
            val intent = Intent(context, FuzzyClockWidget::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            intent.action = action
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

}