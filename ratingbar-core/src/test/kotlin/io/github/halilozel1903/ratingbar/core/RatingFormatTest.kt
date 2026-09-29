package io.github.halilozel1903.ratingbar.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RatingFormatTest {

    @Test
    fun `decimal rounds half up with a fixed number of decimals`() {
        assertEquals("4.6", RatingFormat.decimal(4.604))
        assertEquals("4.6", RatingFormat.decimal(4.56))
        assertEquals("4.0", RatingFormat.decimal(4.0))
        assertEquals("4.05", RatingFormat.decimal(4.05, decimals = 2))
        assertEquals("5", RatingFormat.decimal(4.5, decimals = 0))
        assertEquals("0.0", RatingFormat.decimal(0.0))
        assertEquals("-1.5", RatingFormat.decimal(-1.5))
        assertEquals("0.0", RatingFormat.decimal(-0.01))
        assertEquals("NaN", RatingFormat.decimal(Double.NaN))
        assertFailsWith<IllegalArgumentException> { RatingFormat.decimal(1.0, decimals = -1) }
    }

    @Test
    fun `decimal can trim trailing zeros`() {
        assertEquals("4", RatingFormat.decimal(4.0, trimZeros = true))
        assertEquals("4.5", RatingFormat.decimal(4.5, decimals = 2, trimZeros = true))
        assertEquals("3.7", RatingFormat.decimal(3.7f, trimZeros = true))
    }

    @Test
    fun `rating text`() {
        assertEquals("4", RatingFormat.rating(4f))
        assertEquals("4.5", RatingFormat.rating(4.5f))
        assertEquals("3.7", RatingFormat.rating(3.7f))
        assertEquals("0", RatingFormat.rating(0f))
    }

    @Test
    fun `grouped adds thousands separators`() {
        assertEquals("0", RatingFormat.grouped(0))
        assertEquals("999", RatingFormat.grouped(999))
        assertEquals("1,284", RatingFormat.grouped(1284))
        assertEquals("12,840", RatingFormat.grouped(12_840))
        assertEquals("1,234,567", RatingFormat.grouped(1_234_567L))
        assertEquals("1.234.567", RatingFormat.grouped(1_234_567, separator = '.'))
        assertEquals("-1,000", RatingFormat.grouped(-1000))
    }

    @Test
    fun `compact shortens large counts`() {
        assertEquals("999", RatingFormat.compact(999))
        assertEquals("1K", RatingFormat.compact(1000))
        assertEquals("1.3K", RatingFormat.compact(1284))
        assertEquals("10K", RatingFormat.compact(9_960))
        assertEquals("12K", RatingFormat.compact(12_400))
        assertEquals("1M", RatingFormat.compact(999_950))
        assertEquals("2.4M", RatingFormat.compact(2_400_000))
        assertEquals("1.1B", RatingFormat.compact(1_100_000_000L))
        assertEquals("-1.3K", RatingFormat.compact(-1284))
    }
}
