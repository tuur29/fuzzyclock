package net.tuurlievens.fuzzyclock.text

class FuzzyTextGerman : FuzzyTextInterface {

    // hour is a 24-hour based integer
    override fun generate(hour: Int, min: Int): String {

        // convert from minute count to fuzzy text
        val mintext = when {
            min < 2 -> "gerade"
            min < 10 -> "kurz nach"
            min < 20 -> "viertel nach"
            min < 40 -> "halb" // hour switches here
            min < 50 -> "viertel vor"
            min < 58 -> "kurz vor"
            else -> "gerade"
        }

        // german uses "half <hour+1>"
        val hourtext = if (min < 20) {
            when {
                hour % 12 == 1 -> "eins"
                hour % 12 == 2 -> "zwei"
                hour % 12 == 3 -> "drei"
                hour % 12 == 4 -> "vier"
                hour % 12 == 5 -> "fünf"
                hour % 12 == 6 -> "sechs"
                hour % 12 == 7 -> "sieben"
                hour % 12 == 8 -> "acht"
                hour % 12 == 9 -> "neun"
                hour % 12 == 10 -> "zehn"
                hour % 12 == 11 -> "elf"
                else -> when (hour) {
                    12 -> "zwölf"
                    else -> "zwölf"
                }
            }
        } else {
            when {
                hour % 12 == 1 -> "zwei"
                hour % 12 == 2 -> "drei"
                hour % 12 == 3 -> "vier"
                hour % 12 == 4 -> "fünf"
                hour % 12 == 5 -> "sechs"
                hour % 12 == 6 -> "sieben"
                hour % 12 == 7 -> "acht"
                hour % 12 == 8 -> "neun"
                hour % 12 == 9 -> "zehn"
                hour % 12 == 10 -> "elf"
                else -> when (hour) {
                    11 -> "zwölf"
                    23 -> "zwölf"
                    else -> "eins"
                }
            }
        }

        return "$mintext\n$hourtext"
    }

}
