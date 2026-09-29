package io.github.halilozel1903.ratingbar.core

import kotlin.math.ceil
import kotlin.math.floor

/**
 * The platform independent math behind a rating bar: snapping, mapping a touch position to a value,
 * per item fill fractions and keyboard or accessibility increments.
 *
 * Items are laid out from the start edge: `itemCount` items of `itemSize`, separated by `spacing`.
 * All sizes are in the same unit (usually pixels).
 */
public object RatingMath {

    /** Tolerance used when rounding up or down so that float noise such as `2.0000002` stays `2`. */
    internal const val EPSILON: Float = 1e-4f

    /** Clamps [value] into `min..max`; `NaN` becomes [min]. */
    public fun clamp(value: Float, max: Int, min: Float = 0f): Float {
        require(max >= 0) { "max must not be negative, was $max" }
        val upper = max.toFloat()
        val lower = min.coerceIn(0f, upper)
        if (value.isNaN()) return lower
        return value.coerceIn(lower, upper)
    }

    /**
     * Rounds [value] to the nearest value allowed by [step] (halves round up), e.g. `3.3` becomes `3`
     * for [RatingStep.Full] and `3.5` for [RatingStep.Half]. [RatingStep.Free] leaves it unchanged.
     */
    public fun snap(value: Float, step: RatingStep): Float {
        if (value.isNaN() || step.isContinuous) return value
        val d = step.divisions
        return floor(value * d + 0.5f + EPSILON) / d
    }

    /**
     * Rounds [value] up to the next value allowed by [step]. This is what a touch does: touching
     * anywhere on the third star selects 3, touching its first half selects 2.5 with half steps.
     */
    public fun snapUp(value: Float, step: RatingStep): Float {
        if (value.isNaN() || step.isContinuous) return value
        val d = step.divisions
        return ceil(value * d - EPSILON) / d
    }

    /** Width of `itemCount` items of [itemSize] separated by [spacing]. */
    public fun contentWidth(itemSize: Float, spacing: Float, itemCount: Int): Float {
        if (itemCount <= 0) return 0f
        return itemCount * itemSize + (itemCount - 1) * spacing.coerceAtLeast(0f)
    }

    /**
     * Maps a touch [position] along the bar to a rating.
     *
     * Positions before the first item give `0`, positions past the last item give [itemCount], and a
     * touch in the gap after an item selects that whole item. In right-to-left layouts the items start
     * at the right edge of a bar that is [layoutWidth] wide.
     *
     * @param position Horizontal position of the pointer, measured from the left edge of the bar.
     * @param layoutWidth Measured width of the bar. Only used when [isRtl] is `true`; it can be wider
     * than the items themselves (for example with `fillMaxWidth`).
     */
    public fun valueAt(
        position: Float,
        itemSize: Float,
        spacing: Float,
        itemCount: Int,
        step: RatingStep,
        isRtl: Boolean = false,
        layoutWidth: Float = contentWidth(itemSize, spacing, itemCount),
    ): Float {
        require(itemSize > 0f) { "itemSize must be positive, was $itemSize" }
        if (itemCount <= 0 || position.isNaN()) return 0f
        val gap = spacing.coerceAtLeast(0f)
        val content = contentWidth(itemSize, gap, itemCount)
        val x = if (isRtl) layoutWidth - position else position
        if (x <= 0f) return 0f
        if (x >= content) return itemCount.toFloat()
        val slot = itemSize + gap
        val index = floor(x / slot).toInt().coerceIn(0, itemCount - 1)
        val within = x - index * slot
        val raw = if (within >= itemSize) index + 1f else index + within / itemSize
        return clamp(snapUp(raw, step), itemCount)
    }

    /**
     * How much of the item at [index] (0 based) is filled for [value]: `1` for the items before the
     * value, the fractional part for the item the value ends in and `0` after it.
     */
    public fun fillFraction(value: Float, index: Int): Float {
        if (value.isNaN()) return 0f
        return (value - index).coerceIn(0f, 1f)
    }

    /** The fill fractions of all [itemCount] items, see [fillFraction]. */
    public fun fillFractions(value: Float, itemCount: Int): List<Float> =
        List<Float>(itemCount.coerceAtLeast(0)) { index -> fillFraction(value, index) }

    /**
     * Index of the item the value ends in (the item that was touched last), or `-1` when [value] is `0`.
     * `3` and `2.5` both end in the item at index 2.
     */
    public fun selectedIndex(value: Float): Int {
        if (value.isNaN() || value <= EPSILON) return -1
        return ceil(value - EPSILON).toInt() - 1
    }

    /** The next keyboard or accessibility value above [value], not above [max]. */
    public fun increase(value: Float, step: RatingStep, max: Int): Float {
        val d = step.keyboardDivisions
        val current = clamp(value, max)
        return clamp((floor(current * d + EPSILON) + 1f) / d, max)
    }

    /** The next keyboard or accessibility value below [value], not below `0`. */
    public fun decrease(value: Float, step: RatingStep, max: Int): Float {
        val d = step.keyboardDivisions
        val current = clamp(value, max)
        return clamp((ceil(current * d - EPSILON) - 1f) / d, max)
    }

    /**
     * The number of values strictly between `0` and [max] that can be chosen, as expected by slider
     * semantics (`ProgressBarRangeInfo.steps`). `0` means continuous.
     */
    public fun semanticSteps(step: RatingStep, max: Int): Int {
        if (step.isContinuous || max <= 0) return 0
        return max * step.divisions - 1
    }

    /**
     * A bucket that changes every half item. A haptic tick is played when it changes, so a drag
     * ticks once per half star even with [RatingStep.Free].
     */
    public fun tickBucket(value: Float): Int {
        if (value.isNaN()) return 0
        return ceil(value * 2f - EPSILON).toInt()
    }
}
