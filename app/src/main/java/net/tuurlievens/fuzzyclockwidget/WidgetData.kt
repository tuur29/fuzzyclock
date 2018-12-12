package net.tuurlievens.fuzzyclockwidget

class WidgetData(
    var language: String = "default",
    var fontSize: Int = 26,
    var textAlignment: String = "center",
    var foregroundColor: String = "#ffffff",
    var removeLineBreak: Boolean = false,
    var showDate: Boolean = true,
    var showBattery: Boolean = false
) {

    // TODO: never change the order of these datastring parsers! (compatiblity)

    fun toDataString(): String {
        return language.toString() + ";" +
                fontSize.toString() + ";" +
                textAlignment.toString() + ";" +
                foregroundColor.toString() + ";" +
                removeLineBreak.toString() + ";" +
                showDate.toString() + ";" +
                showBattery.toString()
    }

    companion object {

        val default = WidgetData()

        fun fromDataString(data: String) : WidgetData {
            val arr = data.split(";")
            return WidgetData(
                arr[0].toString(),
                arr[1].toInt(),
                arr[2].toString(),
                arr[3].toString(),
                arr[4].toBoolean(),
                arr[5].toBoolean(),
                arr[6].toBoolean()
            )
        }
    }
}