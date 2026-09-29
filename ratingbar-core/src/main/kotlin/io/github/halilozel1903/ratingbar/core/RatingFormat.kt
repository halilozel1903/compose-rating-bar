package io.github.halilozel1903.ratingbar.core

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Small, locale independent formatting helpers for ratings and review counts. Use your platform's
 * number formatting instead when the text must follow the user's locale.
 */
public object RatingFormat {

    /**
     * [value] rounded half up to [decimals] with a `.` separator: `decimal(4.604) == "4.6"`.
     * With [trimZeros] trailing zeros are dropped: `decimal(4.0, trimZeros = true) == "4"`.
     */
    public fun decimal(value: Double, decimals: Int = 1, trimZeros: Boolean = false): String {
        require(decimals in 0..9) { "decimals must be in 0..9, was $decimals" }
        if (value.isNaN()) return "NaN"
        var factor = 1L
        repeat(decimals) { factor *= 10 }
        val scaled = (abs(value) * factor).roundToLong()
        val whole = scaled / factor
        var fraction = if (decimals == 0) "" else (scaled % factor).toString().padStart(decimals, '0')
        if (trimZeros) fraction = fraction.trimEnd('0')
        val sign = if (value < 0 && scaled != 0L) "-" else ""
        return if (fraction.isEmpty()) "$sign$whole" else "$sign$whole.$fraction"
    }

    /** Float overload of [decimal]. */
    public fun decimal(value: Float, decimals: Int = 1, trimZeros: Boolean = false): String =
        decimal(value.toDouble(), decimals, trimZeros)

    /** A rating value as people write it: `"4"`, `"4.5"`, `"3.7"`. */
    public fun rating(value: Float): String = decimal(value.toDouble(), decimals = 1, trimZeros = true)

    /** [count] with thousands separators: `grouped(1284) == "1,284"`. */
    public fun grouped(count: Long, separator: Char = ','): String {
        val digits = abs(count).toString()
        val builder = StringBuilder()
        digits.forEachIndexed { index, digit ->
            if (index > 0 && (digits.length - index) % 3 == 0) builder.append(separator)
            builder.append(digit)
        }
        return if (count < 0) "-$builder" else builder.toString()
    }

    /** Int overload of [grouped]. */
    public fun grouped(count: Int, separator: Char = ','): String = grouped(count.toLong(), separator)

    /**
     * A short count for tight spaces: `"999"`, `"1.3K"`, `"12K"`, `"2.4M"`, `"1.1B"`. One decimal is
     * shown below 10 of a unit.
     */
    public fun compact(count: Long): String {
        val sign = if (count < 0) "-" else ""
        val magnitude = abs(count.toDouble())
        if (magnitude < 1_000) return "$sign${abs(count)}"
        val units = listOf<Pair<Double, String>>(1e3 to "K", 1e6 to "M", 1e9 to "B", 1e12 to "T")
        for ((index, unit) in units.withIndex()) {
            val scaled = magnitude / unit.first
            val text = if (scaled < 10) decimal(scaled, 1, trimZeros = true) else scaled.roundToLong().toString()
            // 999_950 would round to "1000K"; move on to "1M" instead.
            val rounded = text.toDouble()
            if (rounded < 1_000 || index == units.lastIndex) return "$sign$text${unit.second}"
        }
        error("unreachable")
    }

    /** Int overload of [compact]. */
    public fun compact(count: Int): String = compact(count.toLong())
}
