package io.github.halilozel1903.ratingbar.core

/**
 * How many ratings each star value received, for a "4.6 average, 1,284 reviews" summary with bars.
 *
 * ```kotlin
 * val distribution = RatingDistribution.of(5 to 980, 4 to 190, 3 to 58, 2 to 22, 1 to 34)
 * distribution.total          // 1284
 * distribution.averageText()  // "4.6"
 * distribution.percent(5)     // 76
 * ```
 *
 * @param counts The counts from one star upwards: `counts[0]` is the number of one star ratings and
 * `counts.size` is the highest rating.
 */
public class RatingDistribution(counts: List<Int>) {

    /** The counts from one star upwards; `counts[0]` is the number of one star ratings. */
    public val counts: List<Int> = counts.toList()

    init {
        require(this.counts.isNotEmpty()) { "A distribution needs at least one rating value" }
        require(this.counts.all { it >= 0 }) { "Counts must not be negative: $counts" }
    }

    /** The highest rating, e.g. 5. */
    public val maxRating: Int get() = counts.size

    /** The number of ratings. */
    public val total: Int = counts.sum()

    /** The sum of all ratings, e.g. 5 912 for 980 five star ratings, 190 four star ratings... */
    public val sum: Long = counts.foldIndexed(0L) { index, acc, count -> acc + (index + 1L) * count }

    /** The mean rating, or `0` when there are no ratings. */
    public val average: Double = if (total == 0) 0.0 else sum.toDouble() / total

    /** `true` when there are no ratings. */
    public val isEmpty: Boolean get() = total == 0

    /** The number of ratings of exactly [stars] (1 based); `0` outside `1..maxRating`. */
    public fun count(stars: Int): Int = counts.getOrElse(stars - 1) { 0 }

    /** The share of ratings that are exactly [stars], between `0` and `1`. */
    public fun fraction(stars: Int): Float = if (total == 0) 0f else count(stars).toFloat() / total

    /**
     * The share of [stars] relative to the most common rating, between `0` and `1`. Useful when the
     * longest bar should fill the whole width.
     */
    public fun fractionOfLargest(stars: Int): Float {
        val largest = counts.max()
        return if (largest == 0) 0f else count(stars).toFloat() / largest
    }

    /**
     * Whole percentages for every rating (index 0 is one star) that always add up to exactly 100
     * (largest remainder method), so a summary never shows 76 % + 15 % + 5 % + 2 % + 3 % = 101 %.
     * All zeros when there are no ratings.
     */
    public fun percentages(): List<Int> {
        if (total == 0) return List<Int>(maxRating) { 0 }
        val exact = counts.map { it * 100.0 / total }
        val result = exact.map { it.toInt() }.toMutableList()
        var missing = 100 - result.sum()
        val byRemainder = exact.indices.sortedWith(
            compareByDescending<Int> { exact[it] - result[it] }.thenByDescending { counts[it] },
        )
        for (index in byRemainder) {
            if (missing <= 0) break
            result[index] += 1
            missing--
        }
        return result
    }

    /** The whole percentage of [stars], consistent with [percentages]. */
    public fun percent(stars: Int): Int = percentages().getOrElse(stars - 1) { 0 }

    /** The average rounded to [decimals], always with that many decimals: `"4.6"`, `"4.0"`. */
    public fun averageText(decimals: Int = 1): String =
        RatingFormat.decimal(average, decimals, trimZeros = false)

    /** A copy with one more rating of [stars]. */
    public operator fun plus(stars: Int): RatingDistribution {
        require(stars in 1..maxRating) { "stars must be in 1..$maxRating, was $stars" }
        return RatingDistribution(counts.mapIndexed { index, count -> if (index == stars - 1) count + 1 else count })
    }

    override fun equals(other: Any?): Boolean = other is RatingDistribution && other.counts == counts

    override fun hashCode(): Int = counts.hashCode()

    override fun toString(): String = "RatingDistribution(counts=$counts, average=${averageText(2)}, total=$total)"

    public companion object {
        /** No ratings yet, for a scale up to [maxRating]. */
        public fun empty(maxRating: Int = 5): RatingDistribution {
            require(maxRating > 0) { "maxRating must be positive, was $maxRating" }
            return RatingDistribution(List<Int>(maxRating) { 0 })
        }

        /**
         * Builds a distribution from `stars to count` pairs, e.g. `of(5 to 980, 4 to 190)`. Missing
         * values count as zero; repeated values are added up.
         */
        public fun of(vararg counts: Pair<Int, Int>, maxRating: Int = 5): RatingDistribution {
            require(maxRating > 0) { "maxRating must be positive, was $maxRating" }
            val result = IntArray(maxRating)
            for ((stars, count) in counts) {
                require(stars in 1..maxRating) { "stars must be in 1..$maxRating, was $stars" }
                require(count >= 0) { "Counts must not be negative, was $count for $stars" }
                result[stars - 1] += count
            }
            return RatingDistribution(result.toList())
        }

        /** Counts individual ratings such as `listOf(5, 4, 5, 3)`. */
        public fun fromRatings(ratings: Iterable<Int>, maxRating: Int = 5): RatingDistribution {
            require(maxRating > 0) { "maxRating must be positive, was $maxRating" }
            val result = IntArray(maxRating)
            for (stars in ratings) {
                require(stars in 1..maxRating) { "stars must be in 1..$maxRating, was $stars" }
                result[stars - 1]++
            }
            return RatingDistribution(result.toList())
        }
    }
}
