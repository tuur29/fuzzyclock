package net.tuurlievens.fuzzyclockwidget

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log
import android.support.v4.content.ContextCompat


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        ContextCompat.startForegroundService(context, Intent(context, UpdateWidgetService::class.java))
        Log.i("ALARM","widget update")

    }
}
