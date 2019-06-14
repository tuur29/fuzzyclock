package net.tuurlievens.fuzzyclockwidget

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.preference.*
import com.jaredrummler.android.colorpicker.ColorPreferenceCompat
import net.tuurlievens.fuzzyclock.PreferenceValidator
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

class AllPreferencesFragment : PreferenceFragmentCompat() {

    var parent: FuzzyClockWidgetConfigureActivity? = null

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {

        parent = activity as FuzzyClockWidgetConfigureActivity
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


        // Bind preferences to parent prefs and their summaries (all preferences)
        val array = arrayOf("language", "fontSize", "textAlignment", "foregroundColor", "removeLineBreak", "showDate","simplerDate", "shadowSize", "fontFamily", "useDateFont", "shadowColor")
        for (item in array) {
            val pref = findPreference<Preference>(item)
            pref.onPreferenceChangeListener = getListener()

            // Update sharedpreferences to actual preferences
            if (parent?.prefs != null) {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context).edit()
                when (pref::class.java) {
                    SwitchPreference::class.java -> prefs.putBoolean(item, readPropery(parent!!.prefs!!, item))
                    EditTextPreference::class.java -> prefs.putString(item, (readPropery(parent!!.prefs!!, item) as Int).toString())
                    ListPreference::class.java -> prefs.putString(item, readPropery(parent!!.prefs!!, item))
                    ColorPreferenceCompat::class.java -> prefs.putInt(item, readPropery(parent!!.prefs!!, item))
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
                "int" -> try { value.toString().toInt() } catch (e:IllegalArgumentException) { readPropery<Int>(WidgetData.default, propertyName) }
                "boolean" -> try { value.toString().toBoolean() } catch (e:IllegalArgumentException) { readPropery<Boolean>(WidgetData.default, propertyName) }
                else -> value.toString()
            }
            property.setter.call(instance, newValue)
        }
    }

    private fun getListener() : Preference.OnPreferenceChangeListener {
        return Preference.OnPreferenceChangeListener { preference, value ->

            if (PreferenceValidator.validate(preference.key, value.toString())) {

                // sync with parent prefs
                updateProperty(parent!!.prefs!!, preference.key, value)

                // update summary (and values on load)

                if (preference is SwitchPreference) {
                    preference.isChecked = value.toString().toBoolean()

                } else if (preference is ColorPreferenceCompat) {
                    preference.setDefaultValue(value as Int)

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

    }
}
