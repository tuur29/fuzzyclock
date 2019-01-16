package net.tuurlievens.fuzzyclockwidget

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log
import androidx.core.app.JobIntentService


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        JobIntentService.enqueueWork(context, UpdateWidgetService::class.java, 1000, intent)
        Log.i("ALARM","updaterequest received and work enqueued")

    }
}
