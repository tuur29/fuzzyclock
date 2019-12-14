package net.tuurlievens.fuzzyclock.text

class FuzzyTextSpanish : FuzzyTextInterface {

    // hour is a 24-hour based integer
    override fun generate(hour: Int, min: Int): String {

        // convert from minute count to fuzzy text
        val mintext = when {
            min in 0..5 -> ""
            min in 5..15 -> "y diez"
            min in 15..25 -> "y veinte"
            min in 25..35 -> "y media"
            min in 35..45 -> "menos veinte" // hour switches here
            min in 45..55 -> "menos diez"
			else -> ""

        // change the displayed hour to the next one (instead of the current one) because spanish uses "<hour+1> menos cuarto"
        var hourtext = if (min < 36) { // see line 14 for switch
            when {
                hour % 12 == 1 -> "una" // 1:15 -> una y cuarto
                hour % 12 == 2 -> "dos" // 2:15 -> dos y cuarto
                hour % 12 == 3 -> "tres"
                hour % 12 == 4 -> "cuatro"
                hour % 12 == 5 -> "cinco"
                hour % 12 == 6 -> "seis"
                hour % 12 == 7 -> "siete"
                hour % 12 == 8 -> "ocho"
                hour % 12 == 9 -> "nueve"
                hour % 12 == 10 -> "diez"
                hour % 12 == 11 -> "once"
                else -> when (hour) {
                    12 -> "doce" 
                    else -> "doce"
                }
            }
        } else {
            when {
                hour % 12 == 1 -> "dos" // 1:45 -> dos menos cuarto
                hour % 12 == 2 -> "tres" // 2:45 -> tres menos cuarto
                hour % 12 == 3 -> "cuatro"
                hour % 12 == 4 -> "cinco"
                hour % 12 == 5 -> "seis"
                hour % 12 == 6 -> "siete"
                hour % 12 == 7 -> "ocho"
                hour % 12 == 8 -> "nueve"
                hour % 12 == 9 -> "diez"
                hour % 12 == 10 -> "once"
                else -> when (hour) {
                    11 -> "doce" 
                    23 -> "doce"
                    else -> "una"
                }
            }
        }

        // linebreak is later removed when the setting is active
        return "$hourtext${if(mintext == ""){""}else{"\n"}}$mintext" // spanish uses the "hour & x minutes" format
    }

}
