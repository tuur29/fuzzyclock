package net.tuurlievens.fuzzyclock

class FuzzyTextEnglish : FuzzyTextInterface {

    // hour is a 24-hour based integer
    override fun generate(hour: Int, min: Int): String {

        // convert from minute count to fuzzy text
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
                hour % 12 == 1 -> "one" // 1:15 -> quarter past one
                hour % 12 == 2 -> "two" // 2:15 -> quarter past two
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
                    12 -> "noon" // 12:15 -> quarter past noon
                    else -> "midnight"
                }
            }
        } else {
            when {
                hour % 12 == 1 -> "two" // 1:45 -> quarter to two
                hour % 12 == 2 -> "three" // 2:45 -> quarter to three
                hour % 12 == 3 -> "four"
                hour % 12 == 4 -> "five"
                hour % 12 == 5 -> "six"
                hour % 12 == 6 -> "seven"
                hour % 12 == 7 -> "eight"
                hour % 12 == 8 -> "nine"
                hour % 12 == 9 -> "ten"
                hour % 12 == 10 -> "eleven"
                else -> when (hour) {
                    11 -> "noon" // 11:45 -> quarter to noon
                    23 -> "midnight"
                    else -> "one"
                }
            }
        }

        // linebreak is later removed when the setting is active
        return "$mintext\n$hourtext"
    }

}
