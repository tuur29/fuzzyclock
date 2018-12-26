package net.tuurlievens.fuzzyclock

class FuzzyTextGenerator {
    companion object {

        // decide which generator to call based on locale
        fun create (hour: Int, min: Int, locale: String) : String {
            return when (locale) {

                // TODO: add more locales
                "nl" -> FuzzyTextDutch()
                else -> FuzzyTextEnglish()

            }.generate(hour, min)
        }

    }
}

