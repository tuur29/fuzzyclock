package net.tuurlievens.fuzzyclock

open class PossiblePreferences {

    // display
    open var removeLineBreak: Boolean = false
    open var showDate: Boolean = true
    open var simplerDate: Boolean = true

    // style
    open var fontFamily: String = "default"
    open var emphasis: String = "sans_serif"
    open var textAlignment: String = "center"
    open var foregroundColor: Int = 0xFFFFFFFF.toInt()
    open var backgroundColor: Int = 0xFF000000.toInt()
    open var shadowColor: Int = 0xFF000000.toInt()
    open var fontSize: Int = 26
    open var shadowSize: Int = 6
    open var useDateFont: Boolean = false

    // config
    open var language: String = "default"

    // TODO: convert watchface (and screensaver) to use same clockfacedrawer
    // TODO: refactor all projects to use this class

    // defaults, not editable
    open var padding: Int = 16
}
