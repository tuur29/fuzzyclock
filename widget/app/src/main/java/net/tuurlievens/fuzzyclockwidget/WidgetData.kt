package net.tuurlievens.fuzzyclockwidget

class WidgetData(
    var language: String = "default",
    var fontSize: Int = 26,
    var textAlignment: String = "center",
    var foregroundColor: Int = 0xFFFFFFFF.toInt(),
    var removeLineBreak: Boolean = false,
    var showDate: Boolean = true,
    var simplerDate: Boolean = true
) {

    // Never change the order of these datastring parsers! (compatiblity)

    fun toDataString(): String {
        return language.toString() + ";" +
                fontSize.toString() + ";" +
                textAlignment.toString() + ";" +
                foregroundColor.toString() + ";" +
                removeLineBreak.toString() + ";" +
                showDate.toString() + ";" +
                simplerDate.toString()
    }

    companion object {

        val default = WidgetData()

        fun fromDataString(data: String) : WidgetData {
            val arr = data.split(";")
            val defaults = WidgetData()
            return WidgetData(
                arr.elementAtOrElse(0) { defaults.language.toString() }.toString(),
                arr.elementAtOrElse(1) { defaults.fontSize.toString() }.toInt(),
                arr.elementAtOrElse(2) { defaults.textAlignment.toString() }.toString(),
                arr.elementAtOrElse(3) { defaults.foregroundColor.toString() }.toInt(),
                arr.elementAtOrElse(4) { defaults.removeLineBreak.toString() }.toBoolean(),
                arr.elementAtOrElse(5) { defaults.showDate.toString() }.toBoolean(),
                arr.elementAtOrElse(6) { defaults.simplerDate.toString() }.toBoolean()
            )
        }
    }
}