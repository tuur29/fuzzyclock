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
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
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
            var hitRegions: Array<Rect> = arrayOf()

            // create canvas and draw
            if (width > 0 && height > 0) {
                val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(image)
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                hitRegions = ClockFaceDrawer.draw(canvas, bounds, prefs, context)
                view.setImageViewBitmap(R.id.canvas, image)
            }

            // add config click handler
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

             // add calendar click handlers
            if (prefs.showDate && hitRegions.size > 1) {
                val calendarPackageName = getDefaultPackageName(context, Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)
                if (calendarPackageName != "") {
                    view.setViewPadding(R.id.datebuttoncontainer,
                        pixelsToDip(hitRegions[1].left, context),
                        pixelsToDip(hitRegions[1].top, context),
                        pixelsToDip(width - hitRegions[1].right, context),
                        pixelsToDip(height - hitRegions[1].bottom, context)
                    )
                    view.setOnClickPendingIntent(R.id.datebutton, getPendingIntentByPackageName(context, calendarPackageName))
                } else {
                    view.setViewVisibility(R.id.datebuttoncontainer, View.GONE)
                }
            } else {
                view.setViewVisibility(R.id.datebuttoncontainer, View.GONE)
            }

            // add clock click handler
            val clockPackageName = getDefaultPackageName(context, AlarmClock.ACTION_SET_ALARM)
            if (clockPackageName != "") {
                view.setViewPadding(R.id.clockbuttoncontainer,
                    pixelsToDip(hitRegions[0].left, context),
                    pixelsToDip(hitRegions[0].top, context),
                    pixelsToDip(width - hitRegions[0].right, context),
                    pixelsToDip(height - hitRegions[0].bottom, context)
                )
                view.setOnClickPendingIntent(R.id.clockbutton, getPendingIntentByPackageName(context, clockPackageName))
            } else {
                view.setViewVisibility(R.id.clockbuttoncontainer, View.GONE)
            }

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
            return try {
                val localPackageManager = context.packageManager
                val intent = Intent(action)
                if (data != null) {
                    intent.data = data
                }
                val info = localPackageManager.resolveActivity(
                    intent,
                    PackageManager.MATCH_DEFAULT_ONLY
                ).activityInfo

                info.packageName
            } catch (e: Exception) {
                ""
            }
        }

        private fun dipToPixels(value: Int, context: Context) : Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), context.resources.displayMetrics).toInt()
        }

        private fun pixelsToDip(value: Int, context: Context) : Int {
            return Math.ceil((value * context.resources.displayMetrics.density).toDouble()).toInt()

        }
    }

}