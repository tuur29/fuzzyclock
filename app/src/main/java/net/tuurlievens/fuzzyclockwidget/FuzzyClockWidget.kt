package net.tuurlievens.fuzzyclockwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import java.util.*
import android.util.Log

class FuzzyClockWidget : AppWidgetProvider() {

    private var alarmIntent: PendingIntent? = null

    // LIFECYCLE

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {

        // TODO: fix Attempt to invoke virtual method writeToParcel on a null object reference
        for (appWidgetId in appWidgetIds) {
             UpdateWidgetService.updateWidget(context, appWidgetManager, appWidgetId)
        }

        // start alarm for updates
        startAlarm(context, 10)

        Log.i("ALARM","onUpdate")
    }

    override fun onEnabled(context: Context) {
        Log.i("ALARM","onEnabled")
    }

    fun startAlarm(context: Context, seconds: Int) {

        val cal = Calendar.getInstance()
        val intent = Intent(context, AlarmReceiver::class.java)
        alarmIntent = PendingIntent.getBroadcast(context, 123, intent, 0)

        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?

        // TODO: change alarm types here and find a way to add flag_cancel_current
        manager!!.setRepeating(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            (seconds * 1000).toLong(),
            alarmIntent
        )

        Log.i("ALARM","startAlarm")
    }

    fun stopAlarm(context: Context) {

        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        manager!!.cancel(alarmIntent)

        context.stopService(Intent(context, UpdateWidgetService::class.java))

        Log.i("ALARM","stopAlarm")
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes a widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            FuzzyClockWidgetConfigureActivity.deletePrefs(context, appWidgetId)
        }
    }

    override fun onDisabled(context: Context) {
        stopAlarm(context)
        Log.i("ALARM","onDisabled")
    }
}

