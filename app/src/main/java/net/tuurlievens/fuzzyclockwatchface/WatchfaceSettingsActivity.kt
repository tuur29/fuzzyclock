package net.tuurlievens.fuzzyclockwatchface

import android.os.Bundle
import preference.WearPreferenceActivity
import android.content.Intent


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

}