package net.tuurlievens.fuzzyclock.text

interface FuzzyTextInterface {
    fun generate(hour: Int, min: Int): String
}