package io.github.halilozel1903.ratingbar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import io.github.halilozel1903.ratingbar.core.RatingMath
import io.github.halilozel1903.ratingbar.core.RatingStep
import kotlinx.coroutines.launch

/**
 * An interactive rating bar. Tap an item or drag across the bar to choose a value.
 *
 * ```kotlin
 * var rating by rememberSaveable { mutableFloatStateOf(0f) }
 * RatingBar(value = rating, onValueChange = { rating = it })
 * ```
 *
 * The bar is a slider for accessibility services: TalkBack announces [valueDescription] and can
 * increase or decrease the value by one step, and keyboard arrows do the same.
 *
 * @param value The current rating, between `0` and [max].
 * @param onValueChange Called with the new rating while the user taps or drags.
 * @param modifier Modifier for the bar.
 * @param max The number of items, which is also the highest rating.
 * @param step Whether the rating moves in whole items, half items or freely.
 * @param style What the items look like: stars, hearts, shapes, icons, emoji or your own content.
 * @param enabled When `false` the bar ignores input and uses the disabled colors.
 * @param itemSize The size of one item.
 * @param spacing The space between two items. Touching a gap selects the item before it.
 * @param colors The colors of filled and empty items.
 * @param animateSelection Bounce the chosen item and animate the fill when the value changes.
 * @param hapticFeedback Tick once per half item while the value changes.
 * @param onValueChangeFinished Called when a tap, drag, key press or accessibility action ends.
 * @param valueDescription The text accessibility services read for a value.
 * @param increaseActionLabel Label of the accessibility action that raises the rating by one step.
 * @param decreaseActionLabel Label of the accessibility action that lowers the rating by one step.
 */
@Composable
public fun RatingBar(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    max: Int = 5,
    step: RatingStep = RatingStep.Half,
    style: RatingStyle = RatingStyle.Star,
    enabled: Boolean = true,
    itemSize: Dp = RatingBarDefaults.ItemSize,
    spacing: Dp = RatingBarDefaults.ItemSpacing,
    colors: RatingBarColors = RatingBarDefaults.colors(),
    animateSelection: Boolean = true,
    hapticFeedback: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
    valueDescription: (value: Float, max: Int) -> String = { v, m -> RatingBarDefaults.stateDescription(v, m) },
    increaseActionLabel: String = "Increase rating",
    decreaseActionLabel: String = "Decrease rating",
) {
    require(max > 0) { "max must be positive, was $max" }
    val rating = RatingMath.clamp(value, max)
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val density = LocalDensity.current
    val itemPx = with(density) { itemSize.toPx() }
    val spacingPx = with(density) { spacing.toPx() }
    val scope = rememberCoroutineScope()

    val currentRating by rememberUpdatedState(rating)
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnFinished by rememberUpdatedState(onValueChangeFinished)
    val currentAnimate by rememberUpdatedState(animateSelection)
    val currentHaptics by rememberUpdatedState(hapticFeedback)
    val currentHapticFeedback by rememberUpdatedState(LocalHapticFeedback.current)

    val scales = remember(max) { List<Animatable<Float, AnimationVector1D>>(max) { Animatable(1f) } }
    val gesture = remember { GestureTracker() }
    var dragging by remember { mutableStateOf(false) }

    // Reports a value picked by the user, once per distinct value, with a tick and a bounce whenever
    // it crosses into another half item.
    val select: (Float) -> Unit = select@{ newValue ->
        val previous = if (gesture.lastValue.isNaN()) currentRating else gesture.lastValue
        if (newValue == previous) return@select
        gesture.lastValue = newValue
        currentOnValueChange(newValue)
        if (RatingMath.tickBucket(newValue) == RatingMath.tickBucket(previous)) return@select
        if (currentHaptics) currentHapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentTick)
        val index = RatingMath.selectedIndex(newValue)
        if (currentAnimate && index in scales.indices) {
            val scale = scales[index]
            scope.launch {
                scale.animateTo(1.3f, tween<Float>(durationMillis = 90))
                scale.animateTo(1f, spring<Float>(dampingRatio = 0.35f, stiffness = Spring.StiffnessMediumLow))
            }
        }
    }
    val finish: () -> Unit = {
        gesture.lastValue = Float.NaN
        currentOnFinished?.invoke()
    }
    val stepBy: (Int) -> Boolean = { direction ->
        val next = if (direction > 0) {
            RatingMath.increase(currentRating, step, max)
        } else {
            RatingMath.decrease(currentRating, step, max)
        }
        gesture.lastValue = Float.NaN
        select(next)
        finish()
        true
    }

    val shown by animateFloatAsState(
        targetValue = rating,
        animationSpec = if (animateSelection && !dragging) tween<Float>(durationMillis = 180) else snap<Float>(),
        label = "rating fill",
    )

    val description = valueDescription(rating, max)
    val semanticSteps = RatingMath.semanticSteps(step, max)
    val gestures = if (enabled) {
        Modifier
            .pointerInput(max, step, itemPx, spacingPx, isRtl) {
                val toValue = { x: Float ->
                    RatingMath.valueAt(x, itemPx, spacingPx, max, step, isRtl, layoutWidth = size.width.toFloat())
                }
                detectTapGestures { offset ->
                    gesture.lastValue = Float.NaN
                    select(toValue(offset.x))
                    finish()
                }
            }
            .pointerInput(max, step, itemPx, spacingPx, isRtl) {
                val toValue = { x: Float ->
                    RatingMath.valueAt(x, itemPx, spacingPx, max, step, isRtl, layoutWidth = size.width.toFloat())
                }
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        dragging = true
                        gesture.lastValue = Float.NaN
                        select(toValue(offset.x))
                    },
                    onDragEnd = {
                        dragging = false
                        finish()
                    },
                    onDragCancel = {
                        dragging = false
                        finish()
                    },
                ) { change, _ ->
                    change.consume()
                    select(toValue(change.position.x))
                }
            }
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                this.stateDescription = description
                this.progressBarRangeInfo = ProgressBarRangeInfo(
                    current = rating,
                    range = 0f..max.toFloat(),
                    steps = semanticSteps,
                )
                if (enabled) {
                    setProgress { target ->
                        val next = RatingMath.clamp(RatingMath.snap(target, step), max)
                        if (next != currentRating) {
                            gesture.lastValue = Float.NaN
                            select(next)
                            finish()
                        }
                        true
                    }
                    this.customActions = listOf<CustomAccessibilityAction>(
                        CustomAccessibilityAction(increaseActionLabel) { stepBy(1) },
                        CustomAccessibilityAction(decreaseActionLabel) { stepBy(-1) },
                    )
                } else {
                    disabled()
                }
            }
            .onKeyEvent { event ->
                if (!enabled || event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.DirectionRight -> stepBy(if (isRtl) -1 else 1)
                    Key.DirectionLeft -> stepBy(if (isRtl) 1 else -1)
                    else -> false
                }
            }
            .focusable(enabled = enabled)
            .then(gestures),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val filledColor = colors.filled(enabled)
        val emptyColor = colors.empty(enabled)
        for (index in 0 until max) {
            val scale = scales[index]
            RatingItem(
                style = style,
                index = index,
                fraction = itemFraction(style, rating, shown, index),
                filledColor = filledColor,
                emptyColor = emptyColor,
                itemSize = itemSize,
                modifier = Modifier
                    .size(itemSize)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                    },
            )
        }
    }
}

/**
 * A read-only rating, for example the average next to a product or the stars of a review.
 *
 * ```kotlin
 * RatingIndicator(value = 4.6f)
 * ```
 *
 * Accessibility services read it as a progress indicator with [valueDescription].
 *
 * @param value The rating to show, between `0` and [max]. Any fraction is drawn, e.g. 4.6 fills
 * 60 % of the fifth star.
 */
@Composable
public fun RatingIndicator(
    value: Float,
    modifier: Modifier = Modifier,
    max: Int = 5,
    style: RatingStyle = RatingStyle.Star,
    itemSize: Dp = RatingBarDefaults.IndicatorItemSize,
    spacing: Dp = RatingBarDefaults.ItemSpacing / 2,
    colors: RatingBarColors = RatingBarDefaults.colors(),
    valueDescription: (value: Float, max: Int) -> String = { v, m -> RatingBarDefaults.stateDescription(v, m) },
) {
    require(max > 0) { "max must be positive, was $max" }
    val rating = RatingMath.clamp(value, max)
    val description = valueDescription(rating, max)
    Row(
        modifier = modifier.clearAndSetSemantics {
            this.stateDescription = description
            this.progressBarRangeInfo = ProgressBarRangeInfo(current = rating, range = 0f..max.toFloat())
        },
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (index in 0 until max) {
            RatingItem(
                style = style,
                index = index,
                fraction = itemFraction(style, rating, rating, index),
                filledColor = colors.filledColor,
                emptyColor = colors.emptyColor,
                itemSize = itemSize,
                modifier = Modifier.size(itemSize),
            )
        }
    }
}

/** Mood scales fill only the chosen item, and jump instead of animating through the others. */
private fun itemFraction(style: RatingStyle, rating: Float, shown: Float, index: Int): Float =
    if (style is RatingStyle.Emoji && style.selectedOnly) {
        if (index == RatingMath.selectedIndex(rating)) 1f else 0f
    } else {
        RatingMath.fillFraction(shown, index)
    }

/** The last value reported during the current gesture; not state, it never drives composition. */
private class GestureTracker {
    var lastValue: Float = Float.NaN
}
