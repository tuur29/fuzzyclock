package net.tuurlievens.fuzzyclockwatchface

import android.os.Bundle
import preference.WearPreferenceActivity
import android.content.Intent
import android.os.Build
import android.support.wearable.view.WearableListView
import android.widget.Toast


class WatchfaceSettingsActivity : WearPreferenceActivity() {

    private val myPackageName = "net.tuurlievens.fuzzyclockwatchface"

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

    override fun onClick(viewHolder: WearableListView.ViewHolder) {
        super.onClick(viewHolder)

        // show toast in case of showStatusbar and notifState that says you need to reboot etc...
        if (viewHolder?.position == 3) {
            if (Complications.complicationsEnabled()) {
                val intent = Intent(this, ComplicationSettingsActivity::class.java)
                startActivity(intent)
                // TODO: find a better way to notify user some settings only update after app restart
//            } else {
//                Toast.makeText(this, resources.getText(R.string.msg_notsupported), Toast.LENGTH_SHORT).show()
            }
        }
    }

}
