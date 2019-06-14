package net.tuurlievens.fuzzyclockscreensaver

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentActivity
import androidx.preference.*
import net.tuurlievens.fuzzyclock.PreferenceValidator


class DreamSettingsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(android.R.color.black)
        }

        supportFragmentManager.beginTransaction().replace(android.R.id.content, AllPreferenceFragment()).commit()
    }

    class AllPreferenceFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(p0: Bundle?, p1: String?) {

            addPreferencesFromResource(R.xml.prefs)

            // set app:iconSpaceReserved="false" on all preferences
            for (i in 0 until preferenceScreen.preferenceCount) {
                val cat = preferenceScreen.getPreference(i)
                cat.isIconSpaceReserved = false
                if (cat is PreferenceGroup)
                    for (j in 0 until cat.preferenceCount) {
                        cat.getPreference(j).isIconSpaceReserved = false
                    }
            }

            // Bind the summaries of EditText/List/Dialog preferences
            bindPreferenceSummaryToValue(findPreference("maxTranslationDisplacement"))
            bindPreferenceSummaryToValue(findPreference("updateSeconds"))
            bindPreferenceSummaryToValue(findPreference("language"))
            bindPreferenceSummaryToValue(findPreference("fontFamily"))
            bindPreferenceSummaryToValue(findPreference("fontSize"))
            bindPreferenceSummaryToValue(findPreference("textAlignment"))
            bindPreferenceSummaryToValue(findPreference("notifState"))
            bindPreferenceSummaryToValue(findPreference("shadowSize"))
        }

        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->

            if (PreferenceValidator.validate(preference.key, value.toString())) {
                var stringValue = value.toString()

                // check notification access permissions and request them
                if (preference.key == "notifState" && value != "hidden") {
                    val allowedPackages =
                        NotificationManagerCompat.getEnabledListenerPackages(activity!!.applicationContext)
                    if (!allowedPackages.contains(activity?.applicationContext?.packageName)) {
                        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        activity?.applicationContext?.startActivity(intent)
                        Toast.makeText(
                            activity?.applicationContext,
                            activity?.applicationContext?.getString(R.string.msg_notificationaccess),
                            Toast.LENGTH_SHORT
                        ).show()
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
            } else {
                Toast.makeText(
                    activity?.applicationContext,
                    activity?.applicationContext?.getString(R.string.error) + ": " +
                    activity?.applicationContext?.getString(R.string.msg_validationfail),
                    Toast.LENGTH_LONG
                ).show()

                return@OnPreferenceChangeListener false
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
