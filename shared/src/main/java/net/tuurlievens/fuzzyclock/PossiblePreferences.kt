package net.tuurlievens.fuzzyclock

// Defaults overwritten per app in DreamData, WidgetData & WatchfaceData
open class PossiblePreferences {

    // display
    open var removeLineBreak: Boolean = false
    open var showDate: Boolean = true
    open var simplerDate: Boolean = true
    open var showBattery: Boolean = true
    open var showDigitalClock: Boolean = false

    // style
    open var fontFamily: String = "sans_serif"
    open var emphasis: String = "normal"
    open var textAlignment: String = "center"
    open var foregroundColor: Int = 0xFFFFFFFF.toInt()
    open var dateForegroundColor: Int = 0x57FFFFFF.toInt() // ARGB
    open var backgroundColor: Int = 0xFF000000.toInt()
    open var shadowColor: Int = 0xFF000000.toInt()
    open var fontSize: Int = 26 // widget=26,watchface&screensaver=35
    open var dateFontSize: Int = 17 // widget=17,watchface&screensaver=20
    open var shadowSize: Int = 6
    open var useDateFont: Boolean = false

    // config
    open var language: String = "default"
    open var notifState: String = "hidden"
    open var maxTranslationDisplacement: Double = 0.0
    open var updateSeconds: Double = 60.0
    open var brightScreen: Boolean = false
    open var showStatusbar: Boolean = true
    open var scaling: Double = 1.0

    // defaults, not editable
    open var padding: Int = 16 // differs between apps
    open var digitalClockScaling: Float = 1.85F
    open var antialiasing = true
}
