package io.github.halilozel1903.ratingbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.ratingbar.core.RatingDistribution
import io.github.halilozel1903.ratingbar.core.RatingFormat

/**
 * A "4.6 average, 1,284 reviews" block: the average in large type, a [RatingIndicator] with it and
 * the review count on the start side, and one bar per rating on the end side.
 *
 * ```kotlin
 * RatingSummary(RatingDistribution.of(5 to 980, 4 to 190, 3 to 58, 2 to 22, 1 to 34))
 * ```
 *
 * @param distribution How many ratings each value received.
 * @param style The items of the indicator under the average.
 * @param colors The colors of the indicator.
 * @param barColor The color of the distribution bars.
 * @param trackColor The color behind the bars.
 * @param showPercentages Show the share of every rating after its bar.
 * @param animate Grow the bars in when they first appear.
 * @param reviewCountText The line under the stars, "1,284 reviews" by default.
 */
@Composable
public fun RatingSummary(
    distribution: RatingDistribution,
    modifier: Modifier = Modifier,
    style: RatingStyle = RatingStyle.Star,
    colors: RatingBarColors = RatingBarDefaults.colors(),
    barColor: Color = colors.filledColor,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showPercentages: Boolean = false,
    animate: Boolean = true,
    reviewCountText: (total: Int) -> String = { total -> RatingSummaryDefaults.reviewCountText(total) },
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) {},
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = distribution.averageText(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            RatingIndicator(
                value = distribution.average.toFloat(),
                max = distribution.maxRating,
                style = style,
                colors = colors,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = reviewCountText(distribution.total),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        RatingDistributionBars(
            distribution = distribution,
            modifier = Modifier.weight(1f),
            barColor = barColor,
            trackColor = trackColor,
            showPercentages = showPercentages,
            animate = animate,
        )
    }
}

/**
 * One horizontal bar per rating, highest first, each as long as that rating's share of all ratings.
 * The bars grow in one after another when they first appear.
 *
 * @param barHeight The thickness of a bar.
 * @param showPercentages Show the share of every rating after its bar. The shown percentages always
 * add up to 100.
 * @param animate Grow the bars in, and animate them when the distribution changes.
 * @param rowDescription What accessibility services read for one bar.
 */
@Composable
public fun RatingDistributionBars(
    distribution: RatingDistribution,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    barHeight: Dp = 8.dp,
    showPercentages: Boolean = false,
    animate: Boolean = true,
    rowDescription: (stars: Int, count: Int, percent: Int) -> String = { stars, count, percent ->
        RatingSummaryDefaults.rowDescription(stars, count, percent)
    },
) {
    val percentages = remember(distribution) { distribution.percentages() }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (stars in distribution.maxRating downTo 1) {
            key(stars) {
                val fraction = distribution.fraction(stars)
                val percent = percentages[stars - 1]
                val progress = remember { Animatable(if (animate) 0f else fraction) }
                LaunchedEffect(fraction, animate) {
                    if (animate) {
                        progress.animateTo(
                            targetValue = fraction,
                            animationSpec = tween<Float>(
                                durationMillis = 700,
                                delayMillis = (distribution.maxRating - stars) * 70,
                                easing = FastOutSlowInEasing,
                            ),
                        )
                    } else {
                        progress.snapTo(fraction)
                    }
                }
                val description = rowDescription(stars, distribution.count(stars), percent)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clearAndSetSemantics { contentDescription = description },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stars.toString(),
                        modifier = Modifier.width(16.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.width(8.dp))
                    Spacer(
                        Modifier
                            .weight(1f)
                            .height(barHeight)
                            .drawBehind {
                                val radius = CornerRadius(size.height / 2f)
                                drawRoundRect(color = trackColor, cornerRadius = radius)
                                val barWidth = size.width * progress.value.coerceIn(0f, 1f)
                                if (barWidth > 0f) {
                                    val left = if (layoutDirection == LayoutDirection.Rtl) size.width - barWidth else 0f
                                    drawRoundRect(
                                        color = barColor,
                                        topLeft = Offset(left, 0f),
                                        size = Size(barWidth, size.height),
                                        cornerRadius = radius,
                                    )
                                }
                            },
                    )
                    if (showPercentages) {
                        Text(
                            text = "$percent%",
                            modifier = Modifier.width(44.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                        )
                    }
                }
            }
        }
    }
}

/** English default texts of [RatingSummary] and [RatingDistributionBars]. */
public object RatingSummaryDefaults {

    /** "1,284 reviews", "1 review", "No reviews yet". */
    public fun reviewCountText(total: Int): String = when (total) {
        0 -> "No reviews yet"
        1 -> "1 review"
        else -> "${RatingFormat.grouped(total)} reviews"
    }

    /** "5 stars, 980 ratings, 76 percent". */
    public fun rowDescription(stars: Int, count: Int, percent: Int): String {
        val starsText = if (stars == 1) "1 star" else "$stars stars"
        val countText = if (count == 1) "1 rating" else "${RatingFormat.grouped(count)} ratings"
        return "$starsText, $countText, $percent percent"
    }
}
