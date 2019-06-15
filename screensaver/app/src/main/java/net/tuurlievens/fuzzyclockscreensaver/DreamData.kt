package net.tuurlievens.fuzzyclockscreensaver

import net.tuurlievens.fuzzyclock.PossiblePreferences

class DreamData(
    override var maxTranslationDisplacement: Double = 0.0,
    override var updateSeconds: Double = 60.0,
    override var language: String = "default",
    override var fontFamily: String = "",
    override var fontSize: Int = 36,
    override var shadowSize: Int = 6,
    override var textAlignment: String = "center",
    override var foregroundColor: Int = 0xFFFFFFFF.toInt(),
    override var backgroundColor: Int = 0xFF000000.toInt(),
    override var shadowColor: Int = 0xFF000000.toInt(),
    override var removeLineBreak: Boolean = false,
    override var showDate: Boolean = true,
    override var brightScreen: Boolean = false,
    override var notifState: String = "hidden",
    override var showBattery: Boolean = true,
    override var simplerDate: Boolean = true,
    override var useDateFont: Boolean = false,
    override var padding: Int = 32
): PossiblePreferences() {

    companion object {
        val default = DreamData()

        fun loadFromMap(map: Map<String, *>): DreamData {
            val prefs = DreamData()

            try { prefs.maxTranslationDisplacement = map["maxTranslationDisplacement"].toString().toDouble()  } catch(e: Exception) {}
            try { prefs.updateSeconds = map["updateSeconds"].toString().toDouble()                            } catch(e: Exception) {}
            try { prefs.fontSize = map["fontSize"].toString().toInt()                                         } catch(e: Exception) {}
            try { prefs.shadowSize = map["shadowSize"].toString().toInt()                                     } catch(e: Exception) {}
            try { prefs.language = map["language"].toString()                                                 } catch(e: Exception) {}
            try { prefs.fontFamily = map["fontFamily"].toString()                                             } catch(e: Exception) {}
            try { prefs.textAlignment = map["textAlignment"].toString()                                       } catch(e: Exception) {}
            try { prefs.foregroundColor = map["foregroundColor"].toString().toInt()                           } catch(e: Exception) {}
            try { prefs.backgroundColor = map["backgroundColor"].toString().toInt()                           } catch(e: Exception) {}
            try { prefs.shadowColor = map["shadowColor"].toString().toInt()                                   } catch(e: Exception) {}
            try { prefs.removeLineBreak = map["removeLineBreak"].toString().toBoolean()                       } catch(e: Exception) {}
            try { prefs.showDate = map["showDate"].toString().toBoolean()                                     } catch(e: Exception) {}
            try { prefs.brightScreen = map["brightScreen"].toString().toBoolean()                             } catch(e: Exception) {}
            try { prefs.notifState = map["notifState"].toString()                                             } catch(e: Exception) {}
            try { prefs.showBattery = map["showBattery"].toString().toBoolean()                               } catch(e: Exception) {}
            try { prefs.simplerDate = map["simplerDate"].toString().toBoolean()                               } catch(e: Exception) {}
            try { prefs.useDateFont = map["useDateFont"].toString().toBoolean()                               } catch(e: Exception) {}

            return prefs
        }
    }
}