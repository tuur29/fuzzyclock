package net.tuurlievens.fuzzyclockwidget

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.TypedValue
import android.widget.RemoteViews
import java.util.*
import android.app.PendingIntent


class UpdateWidgetService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // generates random number
        Log.i("ALARM","update widgets")
        pName = packageName

        val manager = AppWidgetManager.getInstance(this)
        val ids = manager.getAppWidgetIds(ComponentName(application, FuzzyClockWidget::class.java))

        for (id in ids) {
            updateWidget(applicationContext, manager, id)
        }

        return super.onStartCommand(intent, flags, startId)
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

            val pickedLanguage = if (prefs.language == "default") Locale.getDefault().language else prefs.language
            view.setTextViewText(R.id.clocktext, FuzzyTextGenerator.create(hour, min, pickedLanguage))

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