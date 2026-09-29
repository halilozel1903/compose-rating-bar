package io.github.halilozel1903.ratingbar.core

/**
 * How finely a rating can be chosen.
 *
 * @property divisions How many selectable values each item is split into, or `0` for a continuous value.
 * @property keyboardDivisions The grid used by keyboard arrows and accessibility actions. Equal to
 * [divisions] for [Full] and [Half]; a tenth of an item for [Free].
 */
public enum class RatingStep(public val divisions: Int, public val keyboardDivisions: Int) {
    /** Whole items only: 1, 2, 3... */
    Full(divisions = 1, keyboardDivisions = 1),

    /** Half items: 0.5, 1, 1.5... */
    Half(divisions = 2, keyboardDivisions = 2),

    /** Any value, such as 3.7. */
    Free(divisions = 0, keyboardDivisions = 10),
    ;

    /** `true` when every value between 0 and the maximum can be chosen. */
    public val isContinuous: Boolean get() = divisions == 0

    /** The distance between two neighbouring keyboard or accessibility values, e.g. `0.5` for [Half]. */
    public val increment: Float get() = 1f / keyboardDivisions
}
