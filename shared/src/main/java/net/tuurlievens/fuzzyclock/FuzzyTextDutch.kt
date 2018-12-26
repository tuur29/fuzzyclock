package net.tuurlievens.fuzzyclock

class FuzzyTextDutch : FuzzyTextInterface {

    override fun generate(hour: Int, min: Int): String {

        val mintext = when {
            min < 2 -> "rond"
            min < 10 -> "iets na"
            min < 20 -> "kwart na" // hour switches here
            min < 40 -> "half"
            min < 50 -> "kwart voor"
            min < 58 -> "bijna"
            else -> "rond"
        }

        // dutch uses "half <hour+1>"
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
