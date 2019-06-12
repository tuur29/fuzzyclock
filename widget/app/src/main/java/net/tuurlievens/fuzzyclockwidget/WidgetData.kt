package net.tuurlievens.fuzzyclockwidget

class WidgetData(
    var language: String = "default",
    var fontSize: Int = 26,
    var textAlignment: String = "center",
    var foregroundColorInt: Int = 0xFFFFFFFF.toInt(),
    var removeLineBreak: Boolean = false,
    var showDate: Boolean = true,
    var simplerDate: Boolean = true,
    var showShadow: Boolean = true
) {

    // Never change the order of these datastring parsers! (compatiblity)

    fun toDataString(): String {
        return language.toString() + ";" +
                fontSize.toString() + ";" +
                textAlignment.toString() + ";" +
                foregroundColorInt.toString() + ";" +
                removeLineBreak.toString() + ";" +
                showDate.toString() + ";" +
                simplerDate.toString() + ";" +
                showShadow.toString()
    }

    companion object {

        val default = WidgetData()

        fun fromDataString(data: String) : WidgetData {
            val arr = data.split(";")
            val defaults = WidgetData()

            // for compatibility from 1.0 to 1.1
            var element3Temp = arr.elementAtOrElse(3) { defaults.foregroundColorInt.toString() }
            if (element3Temp.contains("#")) {
                element3Temp = defaults.foregroundColorInt.toString()
            }

            return WidgetData(
                arr.elementAtOrElse(0) { defaults.language.toString() }.toString(),
                arr.elementAtOrElse(1) { defaults.fontSize.toString() }.toInt(),
                arr.elementAtOrElse(2) { defaults.textAlignment.toString() }.toString(),
                element3Temp.toInt(),
                arr.elementAtOrElse(4) { defaults.removeLineBreak.toString() }.toBoolean(),
                arr.elementAtOrElse(5) { defaults.showDate.toString() }.toBoolean(),
                arr.elementAtOrElse(6) { defaults.simplerDate.toString() }.toBoolean(),
                arr.elementAtOrElse(7) { defaults.showShadow.toString() }.toBoolean()
            )
        }
    }
}