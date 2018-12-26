package net.tuurlievens.fuzzyclock

interface FuzzyTextInterface {
    fun generate(hour: Int, min: Int): String
}