package net.tuurlievens.fuzzyclockscreensaver

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast


class DreamSettingsActivity : PreferenceActivity() {

    init {
        instance = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(android.R.color.black)
        }

        fragmentManager.beginTransaction().replace(android.R.id.content, AllPreferenceFragment()).commit()
    }

    class AllPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.prefs)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("maxTranslationDisplacement"))
            bindPreferenceSummaryToValue(findPreference("updateSeconds"))
            bindPreferenceSummaryToValue(findPreference("language"))
            bindPreferenceSummaryToValue(findPreference("fontSize"))
            bindPreferenceSummaryToValue(findPreference("textAlignment"))
            bindPreferenceSummaryToValue(findPreference("foregroundColor"))
            bindPreferenceSummaryToValue(findPreference("backgroundColor"))
//            bindPreferenceSummaryToValue(findPreference("removeLineBreak"))
//            bindPreferenceSummaryToValue(findPreference("showDate"))
//            bindPreferenceSummaryToValue(findPreference("brightScreen"))
            bindPreferenceSummaryToValue(findPreference("notifState"))
        }

    }

    companion object {

        // TODO: fix memory leak
        private var instance: Activity? = null

        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->

            var stringValue = value.toString()

            // check notification access permissions and request them
            if (preference.key == "notifState" && value != "hidden") {
                if (!Settings.Secure.getString(instance?.contentResolver, "enabled_notification_listeners")
                        .contains(instance?.applicationContext?.packageName!!)) {

                    instance!!.applicationContext.startActivity(
                        Intent(
                            "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
                        )
                    )
                    Toast.makeText(instance, instance!!.getString(R.string.msg_notificationaccess), Toast.LENGTH_SHORT).show()
                    stringValue = "false"
                }
            }

            if (preference is ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                val index = preference.findIndexOfValue(stringValue)

                // Set the summary to reflect the new value.
                preference.setSummary(
                    if (index >= 0)
                        preference.entries[index]
                    else
                        null
                )

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.summary = stringValue
            }
            true
        }

        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(
                preference,
                PreferenceManager.getDefaultSharedPreferences(preference.context).getString(preference.key, "")
            )
        }
    }
}
