package io.github.halilozel1903.ratingbar.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class RatingDistributionTest {

    private val hotel = RatingDistribution.of(5 to 980, 4 to 190, 3 to 58, 2 to 22, 1 to 34)

    @Test
    fun `total sum and average`() {
        assertEquals(1284, hotel.total)
        assertEquals(5912L, hotel.sum)
        assertEquals(5912.0 / 1284, hotel.average, 1e-9)
        assertEquals("4.6", hotel.averageText())
        assertEquals("4.60", hotel.averageText(2))
        assertEquals(5, hotel.maxRating)
    }

    @Test
    fun `counts are ordered from one star up`() {
        assertEquals(listOf(34, 22, 58, 190, 980), hotel.counts)
        assertEquals(980, hotel.count(5))
        assertEquals(34, hotel.count(1))
        assertEquals(0, hotel.count(0))
        assertEquals(0, hotel.count(6))
    }

    @Test
    fun `fractions relative to the total and to the largest bar`() {
        assertEquals(980f / 1284, hotel.fraction(5), 1e-6f)
        assertEquals(1f, hotel.fractionOfLargest(5))
        assertEquals(190f / 980, hotel.fractionOfLargest(4), 1e-6f)
        assertEquals(0f, hotel.fraction(9))
    }

    @Test
    fun `percentages always add up to 100`() {
        val percentages = hotel.percentages()
        assertEquals(100, percentages.sum())
        assertEquals(listOf(3, 2, 4, 15, 76), percentages)
        assertEquals(76, hotel.percent(5))
        assertEquals(0, hotel.percent(6))

        // Three equal thirds: 33.3 each, one of them gets the leftover point.
        val thirds = RatingDistribution(listOf(1, 1, 1))
        assertEquals(100, thirds.percentages().sum())
        assertEquals(listOf(34, 33, 33).sorted(), thirds.percentages().sorted())
    }

    @Test
    fun `percentages favour the larger remainder`() {
        // 1/7 = 14.29 %, 6/7 = 85.71 %: the 85.71 gets rounded up.
        val distribution = RatingDistribution(listOf(1, 6))
        assertEquals(listOf(14, 86), distribution.percentages())
    }

    @Test
    fun `an empty distribution is all zeros`() {
        val empty = RatingDistribution.empty()
        assertTrue(empty.isEmpty)
        assertEquals(0, empty.total)
        assertEquals(0.0, empty.average)
        assertEquals("0.0", empty.averageText())
        assertEquals(listOf(0, 0, 0, 0, 0), empty.percentages())
        assertEquals(0f, empty.fraction(5))
        assertEquals(0f, empty.fractionOfLargest(5))
    }

    @Test
    fun `from individual ratings`() {
        val distribution = RatingDistribution.fromRatings(listOf(5, 4, 5, 3, 5))
        assertEquals(listOf(0, 0, 1, 1, 3), distribution.counts)
        assertEquals(4.4, distribution.average, 1e-9)
        assertFailsWith<IllegalArgumentException> { RatingDistribution.fromRatings(listOf(6)) }
        assertFailsWith<IllegalArgumentException> { RatingDistribution.fromRatings(listOf(0)) }
    }

    @Test
    fun `of adds repeated values and supports other scales`() {
        val distribution = RatingDistribution.of(10 to 2, 10 to 3, 1 to 1, maxRating = 10)
        assertEquals(10, distribution.maxRating)
        assertEquals(5, distribution.count(10))
        assertEquals(6, distribution.total)
        assertFailsWith<IllegalArgumentException> { RatingDistribution.of(6 to 1) }
        assertFailsWith<IllegalArgumentException> { RatingDistribution.of(5 to -1) }
    }

    @Test
    fun `plus adds one rating without changing the original`() {
        val more = hotel + 1
        assertEquals(35, more.count(1))
        assertEquals(34, hotel.count(1))
        assertEquals(1285, more.total)
        assertFailsWith<IllegalArgumentException> { hotel + 6 }
    }

    @Test
    fun `invalid input is rejected`() {
        assertFailsWith<IllegalArgumentException> { RatingDistribution(emptyList()) }
        assertFailsWith<IllegalArgumentException> { RatingDistribution(listOf(1, -2)) }
        assertFailsWith<IllegalArgumentException> { RatingDistribution.empty(0) }
    }

    @Test
    fun `counts are copied`() {
        val source = mutableListOf(1, 2, 3)
        val distribution = RatingDistribution(source)
        source[0] = 100
        assertEquals(1, distribution.count(1))
        assertEquals(6, distribution.total)
    }

    @Test
    fun `equality is based on the counts`() {
        assertEquals(RatingDistribution(listOf(1, 2)), RatingDistribution.of(1 to 1, 2 to 2, maxRating = 2))
        assertEquals(RatingDistribution(listOf(1, 2)).hashCode(), RatingDistribution(listOf(1, 2)).hashCode())
        assertNotEquals(RatingDistribution(listOf(1, 2)), RatingDistribution(listOf(2, 1)))
        assertTrue(hotel.toString().contains("total=1284"))
    }
}
