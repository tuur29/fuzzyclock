package net.tuurlievens.fuzzyclockwatchface

import android.os.Bundle
import preference.WearPreferenceActivity

class WatchfaceSettingsActivity : WearPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.prefs)
    }

    override fun onStop() {
        super.onStop()
        // TODO: notify watchface service to invalidate screen
    }

}