package net.tuurlievens.fuzzyclockwidget

import android.content.Intent
import android.util.Log
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import android.app.PendingIntent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import androidx.core.app.JobIntentService
import net.tuurlievens.fuzzyclock.ClockFaceDrawer


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
            val prefs = FuzzyClockWidgetConfigureActivity.loadPrefs(context, id)
            val view = RemoteViews(pName, R.layout.fuzzy_clock_widget)

            // calculate current widget size (https://stackoverflow.com/a/18723268)
            val options = manager.getAppWidgetOptions(id)
            val width: Int
            val height: Int
            if (context.resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
                height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
            } else {
                width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
                height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
            }
            val bounds = Rect(0, 0, width, height)

            // create canvas and draw
            if (width > 0 && height > 0) {
                val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(image)
                ClockFaceDrawer.draw(canvas, bounds, prefs, context)
                view.setImageViewBitmap(R.id.canvas, image)
            }

            // add click handlers
            val updateIntent = Intent(context, FuzzyClockWidget::class.java)
            updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            view.setOnClickPendingIntent(R.id.canvas, PendingIntent.getBroadcast(
                context,
                id,
                updateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            ))
            view.setOnClickPendingIntent(R.id.configbtn, getPendingSelfIntent(context, FuzzyClockWidget.ConfigTag, id))
            // TODO: re-add clock and calendar click handlers

            // display canvas
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