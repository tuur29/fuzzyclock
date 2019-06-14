package net.tuurlievens.fuzzyclock

enum class Status {
    NOT,
    PASSED,
    FAILED
}

class PreferenceValidator {
    companion object {
        fun validate(name: String, value: String): Boolean {
            var validated = Status.NOT

            try {

                when (name) {
                    "maxTranslationDisplacement" -> validated = if (value.toDouble() in 0.0..1.0) { Status.PASSED } else { Status.FAILED }
                    "updateSeconds" -> validated = if (value.toInt() in 0..86400) { Status.PASSED } else { Status.FAILED }
                    "fontSize" -> validated = if (value.toInt() in 1..500) { Status.PASSED } else { Status.FAILED }
                    "shadowSize" -> validated = if (value.toInt() in 0..500) { Status.PASSED } else { Status.FAILED }

                    // these preferences are set with controls that can't be easily tampered with
                    "language" -> validated = Status.PASSED
                    "textAlignment" -> validated = Status.PASSED
                    "foregroundColor" -> validated = Status.PASSED
                    "backgroundColor" -> validated = Status.PASSED
                    "removeLineBreak" -> validated = Status.PASSED
                    "brightScreen" -> validated = Status.PASSED
                    "notifState" -> validated = Status.PASSED
                    "showBattery" -> validated = Status.PASSED
                    "showDate" -> validated = Status.PASSED
                    "simplerDate" -> validated = Status.PASSED
                    "showStatusbar" -> validated = Status.PASSED
                    "showDigitalClock" -> validated = Status.PASSED
                    "fontFamily" -> validated = Status.PASSED
                    "useDateFont" -> validated = Status.PASSED
                    "showShadow" -> validated = Status.PASSED
                    "shadowColor" -> validated = Status.PASSED
                }

            } catch (e: Exception) {
                return false
            }

            if (validated == Status.PASSED) return true
            if (validated == Status.NOT) throw Exception("Preference "+ name +" not validated!")

            return false
        }
    }
}