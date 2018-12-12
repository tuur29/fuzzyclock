package net.tuurlievens.fuzzyclockwidget

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        // TODO: embed this receiver in the widget

        Log.i("ALARM","request update")
        ContextCompat.startForegroundService(context, Intent(context, UpdateWidgetService::class.java))

//        context.startService(Intent(context, UpdateWidgetService::class.java))

    }
}
