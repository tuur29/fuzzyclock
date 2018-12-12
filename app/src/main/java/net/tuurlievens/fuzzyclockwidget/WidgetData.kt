package net.tuurlievens.fuzzyclockwidget

class WidgetData(
    var fontSize: Int = 26,
    var showDate: Boolean = true,
    var language: String = "default"
) {

    // TODO: Improve stringifing
    fun toDataString(): String {
        return  fontSize.toString() + ";" +
                showDate.toString() + ";" +
                language
    }

    companion object {

        val default = WidgetData()

        fun fromDataString(data: String) : WidgetData {
            val arr = data.split(";")
            return WidgetData(
                arr[0].toInt(),
                arr[1].toBoolean(),
                arr[2]
            )
        }
    }
}