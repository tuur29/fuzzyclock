package net.tuurlievens.fuzzyclock


// With this class you can easily generate a bunch of different fuzzy clock converrsions,
// Simply open  Kotlin REPL under Tools > Kotlin in Android studio and call TestGenerator.test() with a locale:
/* Example:

import net.tuurlievens.fuzzyclock.TestGenerator

TestGenerator.test()

*/
class TestGenerator {
    companion object {

        // decide which generator to call based on locale
        fun test(locale: String = "en") {
            val times = arrayOf( // times are 24-hour based
                intArrayOf(0,0),
                intArrayOf(0,1),
                intArrayOf(1,3),
                intArrayOf(2,5),
                intArrayOf(3,7),
                intArrayOf(4,10),
                intArrayOf(5,14),
                intArrayOf(6,15),
                intArrayOf(7,16),
                intArrayOf(8,20),
                intArrayOf(9,29),
                intArrayOf(9,30),
                intArrayOf(9,31),
                intArrayOf(9,35),
                intArrayOf(9,36),
                intArrayOf(10,41),
                intArrayOf(11,50),
                intArrayOf(12,53),
                intArrayOf(12,55),
                intArrayOf(13,57),
                intArrayOf(14,59),
                intArrayOf(15,0),
                intArrayOf(23,50)
            )

            times.forEach {
                val (hour, min) = it
                val text = FuzzyTextGenerator.create(hour, min, locale).replace("\n", " ")
                println("${hour.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')} -> $text\n")
            }
        }

    }
}
