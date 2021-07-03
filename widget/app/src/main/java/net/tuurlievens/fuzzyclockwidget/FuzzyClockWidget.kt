package net.tuurlievens.fuzzyclockwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.RemoteViews
import java.util.*


class FuzzyClockWidget : AppWidgetProvider() {

    private var alarmIntent: PendingIntent? = null

    // LIFECYCLE

    // when parent wants to refresh or on app update
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

        // update widgets
        for (appWidgetId in appWidgetIds) {
            UpdateWidgetService.updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    fun startAlarm(context: Context) {
        if (alarmIntent != null) return

        val cal = Calendar.getInstance()
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val intent = Intent(context, AlarmReceiver::class.java)
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)

        manager!!.setInexactRepeating(AlarmManager.RTC, cal.timeInMillis, (3 * 60 * 1000).toLong(), alarmIntent)

        Log.i("ALARM", "alarm started")
    }

    // when the first widget is made
    override fun onEnabled(context: Context) {
        // start alarm for updates
        startAlarm(context)
    }

    // when broadcast received
    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)

        val extras = intent?.extras

        // open config panel again
        if (intent?.action!!.contains(ConfigTag)){
            val appWidgetId = extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val i = Intent(context, FuzzyClockWidgetConfigureActivity::class.java)
            i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context?.startActivity(i)
            Log.i("ALARM", "open $appWidgetId config")
            return
        }

        // rerender widget
        if (extras != null && context != null) {
            val appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val manager = AppWidgetManager.getInstance(context)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                UpdateWidgetService.updateWidget(context, manager, appWidgetId)
            }

            // Highlight config button when interacting with widget
            // manual flag differentiates between the launcher updating the widget
            if (extras.getBoolean("manual")){
                val view = RemoteViews(context.packageName, R.layout.fuzzy_clock_widget)
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, FuzzyClockWidget::class.java))
                view.setInt(R.id.configbtn, "setBackgroundColor", 0x44888888)
                appWidgetManager.updateAppWidget(appWidgetIds, view)
                Handler(Looper.getMainLooper()).postDelayed(
                    {
                        view.setInt(R.id.configbtn, "setBackgroundColor", Color.TRANSPARENT)
                        appWidgetManager.updateAppWidget(appWidgetIds, view)
                    },
                    300
                )
            }
        }
    }

    // when a widget gets deleted
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            FuzzyClockWidgetConfigureActivity.deletePrefs(context, appWidgetId)
        }
    }

    // when no more widgets active
    override fun onDisabled(context: Context) {
        // stop alarm
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        if (alarmIntent != null)
        manager!!.cancel(alarmIntent)
        context.stopService(Intent(context, UpdateWidgetService::class.java))
    }

    companion object {
        var ConfigTag = "CONFIG"
    }
}

