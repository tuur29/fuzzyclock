package net.tuurlievens.fuzzyclockscreensaver

import net.tuurlievens.fuzzyclock.PossiblePreferences

class DreamData(
    override var maxTranslationDisplacement: Double = 0.0,
    override var updateSeconds: Double = 60.0,
    override var language: String = "default",
    override var fontFamily: String = "sans_serif",
    override var fontSize: Int = 35,
    override var dateFontSize: Int = 20,
    override var shadowSize: Int = 6,
    override var textAlignment: String = "center",
    override var foregroundColor: Int = 0xFFFFFFFF.toInt(),
    override var dateForegroundColor: Int = 0x57FFFFFF.toInt(),
    override var backgroundColor: Int = 0xFF000000.toInt(),
    override var shadowColor: Int = 0xFF000000.toInt(),
    override var removeLineBreak: Boolean = false,
    override var showDate: Boolean = true,
    override var brightScreen: Boolean = false,
    override var notifState: String = "hidden",
    override var showBattery: Boolean = true,
    override var simplerDate: Boolean = true,
    override var useDateFont: Boolean = false,
    override var padding: Int = 32,
    override var emphasis: String = "normal"
): PossiblePreferences() {

    companion object {
        fun loadFromMap(map: Map<String, *>): DreamData {
            val prefs = DreamData()

            prefs.maxTranslationDisplacement = (map["maxTranslationDisplacement"]?.toString() ?: prefs.maxTranslationDisplacement.toString()).toDouble()
            prefs.updateSeconds = (map["updateSeconds"]?.toString() ?: prefs.updateSeconds.toString()).toDouble()
            prefs.fontSize = (map["fontSize"]?.toString() ?: prefs.fontSize.toString()).toInt()
            prefs.dateFontSize = (map["dateFontSize"]?.toString() ?: prefs.dateFontSize.toString()).toInt()
            prefs.shadowSize = (map["shadowSize"]?.toString() ?: prefs.shadowSize.toString()).toInt()
            prefs.language = (map["language"]?.toString() ?: prefs.language.toString())
            prefs.fontFamily = (map["fontFamily"]?.toString() ?: prefs.fontFamily.toString())
            prefs.textAlignment = (map["textAlignment"]?.toString() ?: prefs.textAlignment.toString())
            prefs.removeLineBreak = (map["removeLineBreak"]?.toString() ?: prefs.removeLineBreak.toString()).toBoolean()
            prefs.showDate = (map["showDate"]?.toString() ?: prefs.showDate.toString()).toBoolean()
            prefs.brightScreen = (map["brightScreen"]?.toString() ?: prefs.brightScreen.toString()).toBoolean()
            prefs.notifState = (map["notifState"]?.toString() ?: prefs.notifState.toString())
            prefs.showBattery = (map["showBattery"]?.toString() ?: prefs.showBattery.toString()).toBoolean()
            prefs.simplerDate = (map["simplerDate"]?.toString() ?: prefs.simplerDate.toString()).toBoolean()
            prefs.useDateFont = (map["useDateFont"]?.toString() ?: prefs.useDateFont.toString()).toBoolean()
            prefs.emphasis = (map["emphasis"]?.toString() ?: prefs.emphasis.toString())

            prefs.foregroundColor = (map["foregroundColor"]?.toString() ?: prefs.foregroundColor.toString()).toInt()
            prefs.dateForegroundColor = (map["dateForegroundColor"]?.toString() ?: prefs.dateForegroundColor.toString()).toInt()
            prefs.backgroundColor = (map["backgroundColor"]?.toString() ?: prefs.backgroundColor.toString()).toInt()
            prefs.shadowColor = (map["shadowColor"]?.toString() ?: prefs.shadowColor.toString()).toInt()

            return prefs
        }
    }
}