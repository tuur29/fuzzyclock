package net.tuurlievens.fuzzyclock

open class PossiblePreferences {

    open var language: String = "default"
    open var fontSize: Int = 26
    open var textAlignment: String = "center"
    open var foregroundColor: Int = 0xFFFFFFFF.toInt()
    open var removeLineBreak: Boolean = false
    open var showDate: Boolean = true
    open var simplerDate: Boolean = true
    open var showShadow: Boolean = true
    open var shadowSize: Int = 6
    open var shadowColor: Int = 0xFFFFFFFF.toInt()
    open var fontFamily: String = ""
    open var useDateFont: Boolean = false

    // TODO: convert watchface (and screensaver) to use same clockfacedrawer
    // TODO: possible to use settings activity for both screensaver and widget?
    // widget only
    // screensaver onlu
    // watchface only

    // defaults, not editable
    open var padding = 10
}
