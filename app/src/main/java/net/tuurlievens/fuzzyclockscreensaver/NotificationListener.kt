package net.tuurlievens.fuzzyclockscreensaver

import android.service.notification.NotificationListenerService
import android.content.IntentFilter
import android.content.Intent
import android.service.notification.StatusBarNotification
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Build


class NotificationListener : NotificationListenerService() {

    private val myPackageName = "net.tuurlievens.fuzzyclockscreensaver"
    private lateinit var receiver: NLServiceReceiver

    override fun onCreate() {
        super.onCreate()

        receiver = NLServiceReceiver()
        val filter = IntentFilter()
        filter.addAction("$myPackageName.NOTIFICATION_LISTENER_SERVICE")
        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val data = NotificationData(sbn.packageName, "onNotificationPosted")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            data.icon = sbn.notification.smallIcon
        }
        sendParcelBroadcast(data)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val data = NotificationData(sbn.packageName, "onNotificationRemoved")
        sendParcelBroadcast(data)
    }

    // HELPERS

    private fun sendParcelBroadcast(data: NotificationData) {
        val i = Intent("$myPackageName.NOTIFICATION_LISTENER")
        i.putExtra("notification_event", data)
        sendBroadcast(i)
    }

    // RECEIVERS

    internal inner class NLServiceReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getStringExtra("command") == "list") {
                for (sbn in activeNotifications) {
                    onNotificationPosted(sbn)
                }
            }

        }
    }

}
