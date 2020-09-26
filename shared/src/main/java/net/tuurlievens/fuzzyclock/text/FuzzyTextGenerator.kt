package net.tuurlievens.fuzzyclock.text

class FuzzyTextGenerator {
    companion object {

        // decide which generator to call based on locale
        fun create (hour: Int, min: Int, locale: String) : String {
            return when (locale) {

                // TODO: add more locales
                "es" -> FuzzyTextSpanish()
                "nl" -> FuzzyTextDutch()
                "en-num" -> FuzzyTextEnglishNumbered()
				"de" -> FuzzyTextGerman()
                else -> FuzzyTextEnglish()

            }.generate(hour, min)
        }

    }
}

