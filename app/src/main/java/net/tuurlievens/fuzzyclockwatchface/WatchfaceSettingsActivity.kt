package net.tuurlievens.fuzzyclockwatchface

import android.os.Bundle
import preference.WearPreferenceActivity
import android.content.Intent
import android.support.wearable.view.WearableListView
import android.util.Log
import android.widget.Toast
import java.time.Duration


class WatchfaceSettingsActivity : WearPreferenceActivity() {

    private val myPackageName = "net.tuurlievens.fuzzyclockwatchface"
    private var messageShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.prefs)
    }

    override fun onStop() {
        super.onStop()

        // notify watchface service to invalidate screen
        val intent = Intent("$myPackageName.REFRESH")
        sendBroadcast(intent)
    }

    override fun onClick(viewHolder: WearableListView.ViewHolder?) {
        super.onClick(viewHolder)
        // show toast in case of showStatusbar and notifState that says you need to reboot etc...

        if (viewHolder?.position == 2 && !messageShown) {
            Toast.makeText(this, "These settings might need a restart of the watchface (switch to another one)", Toast.LENGTH_SHORT).show()
            messageShown = true
        }
    }


}