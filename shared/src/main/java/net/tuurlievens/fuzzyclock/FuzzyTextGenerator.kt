package net.tuurlievens.fuzzyclock

class FuzzyTextGenerator {
    companion object {

        // decide which generator to call based on locale
        fun create (hour: Int, min: Int, locale: String) : String {
            return when (locale) {
                // TODO: add more locales
                "nl" -> createDutch(hour, min)
                else -> createEnglish(hour, min)
            }
        }

        private fun createEnglish(hour: Int, min: Int): String {

            // pretty straightforward conversion from minute count to fuzzy text
            val mintext = when {
                min < 2 -> "around"
                min < 10 -> "just past"
                min < 20 -> "quarter past"
                min < 40 -> "half past" // hour switches here
                min < 50 -> "quarter to"
                min < 58 -> "almost"
                else -> "around"
            }

            // change the displayed hour to the next one (instead of the current one) because english uses "Quarter to <hour+1>"
            var hourtext = if (min < 40) {
                when {
			        hour % 12 == 1 -> "one"
			        hour % 12 == 2 -> "two"
			        hour % 12 == 3 -> "three"
			        hour % 12 == 4 -> "four"
			        hour % 12 == 5 -> "five"
			        hour % 12 == 6 -> "six"
			        hour % 12 == 7 -> "seven"
			        hour % 12 == 8 -> "eight"
			        hour % 12 == 9 -> "nine"
			        hour % 12 == 10 -> "ten"
			        hour % 12 == 11 -> "eleven"
                    else -> when (hour) {
                        12 -> "noon"
                        else -> "midnight"
                    }
                }
            } else {
                when {
                    hour % 12 == 1 -> "two"
                    hour % 12 == 2 -> "three"
                    hour % 12 == 3 -> "four"
                    hour % 12 == 4 -> "five"
                    hour % 12 == 5 -> "six"
                    hour % 12 == 6 -> "seven"
                    hour % 12 == 7 -> "eight"
                    hour % 12 == 8 -> "nine"
                    hour % 12 == 9 -> "ten"
                    hour % 12 == 10 -> "eleven"
                    else -> when (hour) {
                        11 -> "noon"
                        23 -> "midnight"
                        else -> "one"
                    }
                }
            }

            return "$mintext\n$hourtext"
        }

        private fun createDutch(hour: Int, min: Int): String {
            // pretty straightforward conversion from minute count to fuzzy text
            val mintext = when {
                min < 2 -> "rond"
                min < 10 -> "iets na"
                min < 20 -> "kwart na" // hour switches here
                min < 40 -> "half"
                min < 50 -> "kwart voor"
                min < 58 -> "bijna"
                else -> "rond"
            }

            // change the displayed hour to the next one (instead of the current one) because dutch uses "half <hour+1>"
            var hourtext = if (min < 20) {
                when {
                    hour % 12 == 1 -> "een"
                    hour % 12 == 2 -> "twee"
                    hour % 12 == 3 -> "drie"
                    hour % 12 == 4 -> "vier"
                    hour % 12 == 5 -> "vijf"
                    hour % 12 == 6 -> "zes"
                    hour % 12 == 7 -> "zeven"
                    hour % 12 == 8 -> "acht"
                    hour % 12 == 9 -> "negen"
                    hour % 12 == 10 -> "tien"
                    hour % 12 == 11 -> "elf"
                    else -> when (hour) {
                        12 -> "twaalf"
                        else -> "middernacht"
                    }
                }
            } else {
                when {
                    hour % 12 == 1 -> "twee"
                    hour % 12 == 2 -> "drie"
                    hour % 12 == 3 -> "vier"
                    hour % 12 == 4 -> "vijf"
                    hour % 12 == 5 -> "zes"
                    hour % 12 == 6 -> "zeven"
                    hour % 12 == 7 -> "acht"
                    hour % 12 == 8 -> "negen"
                    hour % 12 == 9 -> "tien"
                    hour % 12 == 10 -> "elf"
                    else -> when (hour) {
                        11 -> "twaalf"
                        23 -> "middernacht"
                        else -> "een"
                    }
                }
            }

            return "$mintext\n$hourtext"
        }

    }
}

