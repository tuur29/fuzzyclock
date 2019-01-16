package net.tuurlievens.fuzzyclockscreensaver

import android.annotation.SuppressLint
import android.service.notification.NotificationListenerService
import android.content.IntentFilter
import android.content.Intent
import android.service.notification.StatusBarNotification
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.graphics.drawable.IconCompat
import java.util.HashMap


class NotificationListener : NotificationListenerService() {

    private val myPackageName = "net.tuurlievens.fuzzyclockscreensaver"
    private val ignoredPackageNames = arrayOf("android","com.android.systemui")

    private lateinit var receiver: NotificationUpdateRequestReceiver
    private val notifications = HashMap<String, NotificationData>()

    override fun onCreate() {
        super.onCreate()

        receiver = NotificationUpdateRequestReceiver()
        val filter = IntentFilter()
        filter.addAction("$myPackageName.NOTIFICATION_LISTENER_SERVICE")
        registerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onNotificationPosted(notification: StatusBarNotification) {
        Log.i("NOTIF", notification.packageName + " posted new notification")
        handleActiveNotification(notification)
        updateDream()
    }

    override fun onNotificationRemoved(notification: StatusBarNotification) {
        Log.i("NOTIF", notification.packageName + " removed notification")

        // decrease notif count or remove
        val item = notifications[notification.packageName]
        if (item != null) {

            // removing grouped notifs only calls this method once, so `item.count--` wont work
            item.count = activeNotifications.filter { n -> n.packageName == notification.packageName }.size
            Log.i("NOTIF", notification.packageName + " current count " + item.count)

            if (item.count < 2)
                notifications.remove(notification.packageName)
            else {
                notifications[notification.packageName] = item
            }

            updateDream()
        }
    }

    // HELPERS

    @SuppressLint("RestrictedApi")
    private fun handleActiveNotification(notification: StatusBarNotification) {

        // ignore certain packages
        if (ignoredPackageNames.contains(notification.packageName))
            return

        val data = NotificationData(notification.packageName)

        // get icon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            data.icon = IconCompat.createFromIcon(notification.notification.smallIcon)
        }

        // update notif count of specific package
        val item = notifications[notification.packageName]
        if (item != null) {
            item.count++
            notifications[notification.packageName] = item
            Log.i("NOTIF", notification.packageName + " current count " + item.count)
        } else {
            notifications[notification.packageName] = data
        }

    }

    private fun updateDream() {
        val i = Intent("$myPackageName.NOTIFICATION_LISTENER")
        val temp = ArrayList(notifications.map { (k, v) -> v })
        i.putParcelableArrayListExtra("notification_event", temp)
        sendBroadcast(i)
    }

    // RECEIVERS

    internal inner class NotificationUpdateRequestReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i("NOTIF", "requested list of notifications")

            notifications.clear()
            for (notification in activeNotifications) {
                handleActiveNotification(notification)
            }
            updateDream()

        }
    }

}
