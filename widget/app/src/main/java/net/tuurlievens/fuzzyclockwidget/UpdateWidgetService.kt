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
import android.provider.AlarmClock
import net.tuurlievens.fuzzyclock.FuzzyTextGenerator
import java.text.SimpleDateFormat
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import android.view.View
import androidx.core.app.JobIntentService


class UpdateWidgetService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {
        // generates random number
        Log.i("ALARM","start widget update")
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
            val prefs = FuzzyClockWidgetConfigureActivity.loadPrefs(context, id)

            // get correct view for each alignment
            val view = when(prefs.textAlignment) {
                "left" -> RemoteViews(pName, R.layout.fuzzy_clock_widget_left)
                "right" -> RemoteViews(pName, R.layout.fuzzy_clock_widget_right)
                else -> RemoteViews(pName, R.layout.fuzzy_clock_widget_center)
            }

            val parsedForegroundColor = "#" + Integer.toHexString(prefs.foregroundColorInt)

            // get correct textviews depending on shadow setting and hide other
            val clockTextID = when(prefs.showShadow) {
                true -> {
                    view.setViewVisibility(R.id.clocktext, View.GONE)
                    R.id.clocktextshadow
                }
                else -> {
                    view.setViewVisibility(R.id.clocktextshadow, View.GONE)
                    R.id.clocktext
                }
            }
            view.setViewVisibility(clockTextID, View.VISIBLE)
            val dateTextID = when(prefs.showShadow) {
                true -> {
                    view.setViewVisibility(R.id.datetext, View.GONE)
                    R.id.datetextshadow
                }
                else -> {
                    view.setViewVisibility(R.id.datetextshadow, View.GONE)
                    R.id.datetext
                }
            }
            view.setViewVisibility(dateTextID, View.VISIBLE)

            // update clock
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val min = calendar.get(Calendar.MINUTE)

            view.setTextViewTextSize(clockTextID, TypedValue.COMPLEX_UNIT_SP, prefs.fontSize.toFloat())
            view.setTextColor(clockTextID, Color.parseColor(parsedForegroundColor))

            val pickedLanguage = if (prefs.language == "default") Locale.getDefault().language else prefs.language
            var text = FuzzyTextGenerator.create(hour, min, pickedLanguage)
            if (prefs.removeLineBreak) {
               text = text.replace("\n", " ")
            }
            view.setTextViewText(clockTextID, text)

            // update date
            if (prefs.showDate) {
                val loc = Locale(pickedLanguage)
                val format = if (prefs.simplerDate) SimpleDateFormat("EEEE", loc) else SimpleDateFormat("E, d MMM", loc)
                view.setTextViewText(dateTextID, format.format(calendar.time))
                view.setTextViewTextSize(dateTextID, TypedValue.COMPLEX_UNIT_SP, prefs.fontSize * 0.65F)
                view.setTextColor(dateTextID, Color.parseColor(parsedForegroundColor))
            } else {
                view.setTextViewText(dateTextID, "")
                view.setTextViewTextSize(dateTextID, TypedValue.COMPLEX_UNIT_SP, 0F)
            }

            // set widget click listeners
            val calendarPackageName = getDefaultPackageName(context, Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)
            val clockPackageName = getDefaultPackageName(context, AlarmClock.ACTION_SET_ALARM)
            if (calendarPackageName != "")
                view.setOnClickPendingIntent(dateTextID, getPendingIntentByPackageName(context, calendarPackageName))
            if (clockPackageName != "")
                view.setOnClickPendingIntent(clockTextID, getPendingIntentByPackageName(context, clockPackageName))
            view.setOnClickPendingIntent(R.id.configbtn, getPendingSelfIntent(context, FuzzyClockWidget.ConfigTag, id))

            manager.updateAppWidget(id, view)
            Log.i("ALARM","widget $id updated")
        }

        private fun getPendingSelfIntent(context: Context, action: String, id: Int? = null): PendingIntent {
            val intent = Intent(context, FuzzyClockWidget::class.java)
            intent.action = action + id
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            return getPendingIntent(context, intent, action + id)
        }

        private fun getPendingIntent(context: Context, intent: Intent, action: String? = null): PendingIntent {
            if (action != null) {
                intent.action = action
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        private fun getPendingIntentByPackageName(context: Context, packagename: String): PendingIntent {
            return PendingIntent.getActivity(
                context,
                0,
                context.packageManager.getLaunchIntentForPackage(packagename)!!,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        private fun getDefaultPackageName(context: Context, action: String, data: Uri? = null): String {
            try {
                val localPackageManager = context.packageManager
                val intent = Intent(action)
                if (data != null) {
                    intent.data = data
                }
                return localPackageManager.resolveActivity(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                ).activityInfo.packageName
            } catch (e: Exception) {
                return ""
            }
        }
    }

}