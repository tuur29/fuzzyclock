package net.tuurlievens.fuzzyclock.text

class FuzzyTextGenerator {
    companion object {

        // decide which generator to call based on locale
        fun create (hour: Int, min: Int, locale: String) : String {
            return when (locale) {

                // TODO: add more locales
                "nl" -> FuzzyTextDutch()
                "en-num" -> FuzzyTextEnglishNumbered()
                else -> FuzzyTextEnglish()

            }.generate(hour, min)
        }

    }
}

