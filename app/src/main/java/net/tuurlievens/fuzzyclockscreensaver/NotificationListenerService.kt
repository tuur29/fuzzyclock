package net.tuurlievens.fuzzyclockscreensaver

import android.annotation.SuppressLint
import android.service.notification.NotificationListenerService
import android.content.IntentFilter
import android.content.Intent
import android.service.notification.StatusBarNotification
import android.content.BroadcastReceiver
import android.content.Context

// Source: https://github.com/kpbird/NotificationListenerService-Example

@SuppressLint("OverrideAbstract")
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

    // TODO: add conditional android api level -> under: only show notif count, else show notif
//    @RequiresApi(Build.VERSION_CODES.M)
    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val data = arrayOf("derp"
//            sbn.notification.extras.getInt(Notification.EXTRA_LARGE_ICON_BIG).toString()
        )
        sendBroadcast("onNotificationPosted", sbn.packageName + ";" + data.joinToString(","))
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        sendBroadcast("onNotificationRemoved", sbn.packageName)
    }

    // HELPERS

    private fun sendBroadcast(type: String, data: String) {
        val i = Intent("$myPackageName.NOTIFICATION_LISTENER")
        i.putExtra("notification_event", "$type : $data")
        sendBroadcast(i)
    }

    // RECEIVERS

    internal inner class NLServiceReceiver : BroadcastReceiver() {
//        @RequiresApi(Build.VERSION_CODES.M)
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getStringExtra("command") == "list") {
                for (sbn in activeNotifications) {
                    onNotificationPosted(sbn)
                }
            }

        }
    }

}
