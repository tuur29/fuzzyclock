package net.tuurlievens.fuzzyclockwatchface

import android.graphics.Color
import android.util.Log
import net.tuurlievens.fuzzyclock.PossiblePreferences

class WatchfaceData(
    override var language: String = "en",
    override var fontSize: Int = 35,
    override var dateFontSize: Int = 20,
    override var shadowSize: Int = 4,
    override var textAlignment: String = "center",
    override var fontFamily: String = "sans_serif",
    override var showDigitalClock: Boolean = false,
    override var simplerDate: Boolean = true,
    override var useDateFont: Boolean = false,
    override var notifState: String = "hidden",
    override var showStatusbar: Boolean = true,
    override var emphasis: String = "normal"
): PossiblePreferences() {

    var showDigitalClockString: String = "never"
    var showDateString: String = "interactive"
    var foregroundColorString: String = "#FFFFFF"
    var dateForegroundColorString: String = "#616161"
    var backgroundColorString: String = "#000000"
    var shadowColorString: String = "#000000"

    companion object {
        fun loadFromMap(map: Map<String, *>): WatchfaceData {
            val prefs = WatchfaceData()

            prefs.language = (map["language"]?.toString() ?: prefs.language.toString())
            prefs.fontSize = (map["fontSize"]?.toString() ?: prefs.fontSize.toString()).toInt()
            prefs.dateFontSize = (map["dateFontSize"]?.toString() ?: prefs.dateFontSize.toString()).toInt()
            prefs.shadowSize = (map["shadowSize"]?.toString() ?: prefs.shadowSize.toString()).toInt()
            prefs.textAlignment = (map["textAlignment"]?.toString() ?: prefs.textAlignment.toString())
            prefs.notifState = (map["notifState"]?.toString() ?: prefs.notifState.toString())
            prefs.showStatusbar = (map["showStatusbar"]?.toString() ?: prefs.showStatusbar.toString()).toBoolean()
            prefs.simplerDate = (map["simplerDate"]?.toString() ?: prefs.simplerDate.toString()).toBoolean()
            prefs.useDateFont = (map["useDateFont"]?.toString() ?: prefs.useDateFont.toString()).toBoolean()
            prefs.fontFamily = (map["fontFamily"]?.toString() ?: prefs.fontFamily.toString())
            prefs.emphasis = (map["emphasis"]?.toString() ?: prefs.emphasis.toString())

            // load intermediary preferences (dependant on eg ambient mode)
            prefs.foregroundColorString = map["foregroundColorString"]?.toString() ?: prefs.foregroundColorString
            prefs.dateForegroundColorString = map["dateForegroundColorString"]?.toString() ?: prefs.dateForegroundColorString
            prefs.backgroundColorString = map["backgroundColorString"]?.toString() ?: prefs.backgroundColorString
            prefs.shadowColorString = map["shadowColorString"]?.toString() ?: prefs.shadowColorString
            prefs.showDateString = map["showDateString"]?.toString() ?: prefs.showDateString
            prefs.showDigitalClockString = map["showDigitalClockString"]?.toString() ?: prefs.showDigitalClockString

            // Convert colors to int (listpreferences only work with strings)
            prefs.foregroundColor = Color.parseColor(prefs.foregroundColorString)
            prefs.dateForegroundColor = Color.parseColor(prefs.dateForegroundColorString)
            prefs.backgroundColor = Color.parseColor(prefs.backgroundColorString)
            prefs.shadowColor = Color.parseColor(prefs.shadowColorString)

            return prefs
        }
    }

}
