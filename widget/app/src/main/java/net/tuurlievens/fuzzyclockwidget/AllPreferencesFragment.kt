package net.tuurlievens.fuzzyclockwidget

import android.os.Bundle
import android.preference.*
import android.preference.Preference
import android.support.v7.preference.Preference.OnPreferenceChangeListener
import android.support.v7.preference.PreferenceFragmentCompat
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

class AllPreferencesFragment : PreferenceFragmentCompat() {

    var parent: FuzzyClockWidgetConfigureActivity? = null

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {

        parent = activity as FuzzyClockWidgetConfigureActivity
        addPreferencesFromResource(R.xml.prefs)

        // Bind preferences to parent prefs and their summaries
        val array = arrayOf("language", "fontSize", "textAlignment", "foregroundColor", "removeLineBreak", "showDate","simplerDate")
        for (item in array) {
            var pref = findPreference(item)
            pref.onPreferenceChangeListener = getListener()

            // Update sharedpreferences to actual preferences
            if (parent?.prefs != null) {
                val prefs = PreferenceManager.getDefaultSharedPreferences(parent).edit()
                when (pref::class.java) {
                    SwitchPreference::class.java -> prefs.putBoolean(item, readPropery(parent!!.prefs!!, item))
                    EditTextPreference::class.java -> prefs.putString(item, (readPropery(parent!!.prefs!!, item) as Int).toString())
                    ListPreference::class.java -> prefs.putString(item, readPropery(parent!!.prefs!!, item))
                }
                prefs.apply()
                pref.onPreferenceChangeListener.onPreferenceChange(pref, readPropery(parent!!.prefs!!, item))
            }
        }
    }

    fun <R: Any?> readPropery(instance: Any, propertyName: String): R {
        return (instance::class.memberProperties.first { it.name == propertyName } as KProperty<R>).getter.call(instance)
    }

    fun updateProperty(instance: Any, propertyName: String, value: Any) {
        val property = instance::class.memberProperties.find { it.name == propertyName }
        if (property is KMutableProperty<*>) {
            val newValue = when (property.returnType.javaType.toString()) {
                "int" -> value.toString().toInt()
                "boolean" -> value.toString().toBoolean()
                else -> value.toString()
            }
            property.setter.call(instance, newValue)
        }
    }

    private fun getListener() : OnPreferenceChangeListener {
        return OnPreferenceChangeListener { preference, value ->

            // sync with parent prefs
            updateProperty(parent!!.prefs!!, preference.key, value)

            // update summary (and values on load)

            if (preference is SwitchPreference) {
                preference.isChecked = value.toString().toBoolean()

            } else {

                val stringValue = value.toString()

                if (preference is ListPreference) {
                    val index = preference.findIndexOfValue(stringValue)
                    preference.setSummary(
                        if (index >= 0) preference.entries[index] else null
                    )
                    preference.setValueIndex(index)
                } else if (preference is EditTextPreference) {
                    preference.setSummary(stringValue)
                    preference.text = stringValue
                }
            }

            true
        }

    }
}
