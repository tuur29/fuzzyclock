package net.tuurlievens.fuzzyclockwidget

import net.tuurlievens.fuzzyclock.PossiblePreferences

class WidgetData(
    override var language: String = "default",
    override var fontSize: Int = 26,
    override var textAlignment: String = "center",
    override var foregroundColor: Int = 0xFFFFFFFF.toInt(),
    override var removeLineBreak: Boolean = false,
    override var showDate: Boolean = true,
    override var simplerDate: Boolean = true,
    override var shadowSize: Int = 4,
    override var shadowColor: Int = 0xFF000000.toInt(),
    override var fontFamily: String = "default",
    override var useDateFont: Boolean = false
): PossiblePreferences() {

    // Never change the order of these datastring parsers! (compatiblity)

    fun toDataString(): String {
        return language.toString() + ";" +
                fontSize.toString() + ";" +
                textAlignment.toString() + ";" +
                foregroundColor.toString() + ";" +
                removeLineBreak.toString() + ";" +
                showDate.toString() + ";" +
                simplerDate.toString() + ";" +
                shadowSize.toString() + ";" +
                shadowColor.toString() + ";" +
                fontFamily.toString() + ";" +
                useDateFont.toString()
    }

    companion object {

        val default = WidgetData()

        fun fromDataString(data: String) : WidgetData {
            val arr = data.split(";")
            val defaults = WidgetData()

            // for compatibility from 1.0 to 1.1
            var element3Temp = arr.elementAtOrElse(3) { defaults.foregroundColor.toString() }
            if (element3Temp.contains("#")) {
                element3Temp = defaults.foregroundColor.toString()
            }

            return WidgetData(
                arr.elementAtOrElse(0) { defaults.language.toString() }.toString(),
                arr.elementAtOrElse(1) { defaults.fontSize.toString() }.toInt(),
                arr.elementAtOrElse(2) { defaults.textAlignment.toString() }.toString(),
                element3Temp.toInt(),
                arr.elementAtOrElse(4) { defaults.removeLineBreak.toString() }.toBoolean(),
                arr.elementAtOrElse(5) { defaults.showDate.toString() }.toBoolean(),
                arr.elementAtOrElse(6) { defaults.simplerDate.toString() }.toBoolean(),
                arr.elementAtOrElse(7) { defaults.shadowSize.toString() }.toInt(),
                arr.elementAtOrElse(8) { defaults.shadowColor.toString() }.toInt(),
                arr.elementAtOrElse(9) { defaults.fontFamily.toString() }.toString(),
                arr.elementAtOrElse(10) { defaults.useDateFont.toString() }.toBoolean()
            )
        }
    }
}