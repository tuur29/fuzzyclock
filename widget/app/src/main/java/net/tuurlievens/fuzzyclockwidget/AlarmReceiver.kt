package net.tuurlievens.fuzzyclockwidget

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.support.v4.app.JobIntentService
import android.util.Log
import android.support.v4.content.ContextCompat


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        JobIntentService.enqueueWork(context, UpdateWidgetService::class.java, 1000, intent)
        Log.i("ALARM","update enqueued")

    }
}
