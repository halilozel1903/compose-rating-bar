package io.github.halilozel1903.ratingbar

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.ratingbar.core.RatingFormat

/**
 * The colors of a rating bar. Only [RatingStyle.Shaped] and tinted [RatingStyle.Painted] items use
 * them; emoji and untinted icons keep their own colors.
 */
@Immutable
public class RatingBarColors(
    public val filledColor: Color,
    public val emptyColor: Color,
    public val disabledFilledColor: Color,
    public val disabledEmptyColor: Color,
) {
    /** The color of filled items. */
    public fun filled(enabled: Boolean): Color = if (enabled) filledColor else disabledFilledColor

    /** The color of empty items. */
    public fun empty(enabled: Boolean): Color = if (enabled) emptyColor else disabledEmptyColor

    /** A copy with some colors replaced; [Color.Unspecified] keeps the current one. */
    public fun copy(
        filledColor: Color = this.filledColor,
        emptyColor: Color = this.emptyColor,
        disabledFilledColor: Color = this.disabledFilledColor,
        disabledEmptyColor: Color = this.disabledEmptyColor,
    ): RatingBarColors = RatingBarColors(
        filledColor = filledColor.orIfUnspecified(this.filledColor),
        emptyColor = emptyColor.orIfUnspecified(this.emptyColor),
        disabledFilledColor = disabledFilledColor.orIfUnspecified(this.disabledFilledColor),
        disabledEmptyColor = disabledEmptyColor.orIfUnspecified(this.disabledEmptyColor),
    )

    override fun equals(other: Any?): Boolean =
        other is RatingBarColors &&
            other.filledColor == filledColor &&
            other.emptyColor == emptyColor &&
            other.disabledFilledColor == disabledFilledColor &&
            other.disabledEmptyColor == disabledEmptyColor

    override fun hashCode(): Int {
        var result = filledColor.hashCode()
        result = 31 * result + emptyColor.hashCode()
        result = 31 * result + disabledFilledColor.hashCode()
        result = 31 * result + disabledEmptyColor.hashCode()
        return result
    }
}

private fun Color.orIfUnspecified(fallback: Color): Color = if (this == Color.Unspecified) fallback else this

/** Default values used by [RatingBar], [RatingIndicator] and [RatingSummary]. */
public object RatingBarDefaults {

    /** The size of one item of a [RatingBar]. */
    public val ItemSize: Dp = 32.dp

    /** The size of one item of a [RatingIndicator]. */
    public val IndicatorItemSize: Dp = 18.dp

    /** The space between two items. */
    public val ItemSpacing: Dp = 4.dp

    /** Classic star gold, for when stars should not follow the theme. */
    public val Gold: Color = Color(0xFFFFB400)

    /** A warm red for hearts. */
    public val HeartRed: Color = Color(0xFFE5395B)

    /** Alpha of empty emoji and untinted icons, which are also desaturated. */
    public const val EmptyContentAlpha: Float = 0.35f

    /**
     * Colors that follow the Material 3 theme: `primary` for filled items and `outlineVariant` for
     * empty ones. Pass [Gold] as [filledColor] for classic stars.
     */
    @Composable
    public fun colors(
        filledColor: Color = MaterialTheme.colorScheme.primary,
        emptyColor: Color = MaterialTheme.colorScheme.outlineVariant,
        disabledFilledColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        disabledEmptyColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
    ): RatingBarColors = RatingBarColors(filledColor, emptyColor, disabledFilledColor, disabledEmptyColor)

    /** English state description read by screen readers: "Rated 4.5 out of 5" or "Not rated". */
    public fun stateDescription(value: Float, max: Int): String =
        if (value <= 0f) "Not rated" else "Rated ${RatingFormat.rating(value)} out of $max"
}
