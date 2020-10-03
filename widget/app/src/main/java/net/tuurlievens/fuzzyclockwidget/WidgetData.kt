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
    override var fontFamily: String = "sans_serif",
    override var useDateFont: Boolean = false,
    override var emphasis: String = "normal",
    override var scaling: Double = 1.5,
    override var dateFontSize: Int = 17,
    override var dateForegroundColor: Int = 0x57FFFFFF.toInt()
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
                useDateFont.toString() + ";" +
                emphasis.toString() + ";" +
                scaling.toString() + ";" +
                dateFontSize.toString() + ";" +
                dateForegroundColor.toString()
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
                arr.elementAtOrElse(10) { defaults.useDateFont.toString() }.toBoolean(),
                arr.elementAtOrElse(11) { defaults.emphasis.toString() }.toString(),
                arr.elementAtOrElse(12) { defaults.scaling.toString() }.toDouble(),
                arr.elementAtOrElse(13) { defaults.dateFontSize.toString() }.toInt(),
                arr.elementAtOrElse(14) { defaults.dateForegroundColor.toString() }.toInt()
            )
        }
    }
}