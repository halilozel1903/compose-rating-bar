package io.github.halilozel1903.ratingbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * One item: the empty copy on the part that is not filled and the filled copy clipped to [fraction],
 * starting from the start edge (the right edge in right-to-left layouts).
 */
@Composable
internal fun RatingItem(
    style: RatingStyle,
    index: Int,
    fraction: Float,
    filledColor: Color,
    emptyColor: Color,
    itemSize: Dp,
    modifier: Modifier = Modifier,
) {
    Box(modifier.clearAndSetSemantics {}, contentAlignment = Alignment.Center) {
        if (fraction < 1f) {
            ItemLayer(
                style = style,
                index = index,
                filled = false,
                color = emptyColor,
                itemSize = itemSize,
                modifier = Modifier.matchParentSize().clipToFill(fraction, filledPart = false),
            )
        }
        if (fraction > 0f) {
            ItemLayer(
                style = style,
                index = index,
                filled = true,
                color = filledColor,
                itemSize = itemSize,
                modifier = Modifier.matchParentSize().clipToFill(fraction, filledPart = true),
            )
        }
    }
}

@Composable
private fun ItemLayer(
    style: RatingStyle,
    index: Int,
    filled: Boolean,
    color: Color,
    itemSize: Dp,
    modifier: Modifier,
) {
    when (style) {
        is RatingStyle.Shaped -> Box(modifier.background(color, style.shape))

        is RatingStyle.Painted -> Image(
            painter = if (filled) style.filled else style.empty,
            contentDescription = null,
            modifier = if (!filled && !style.tint) modifier.desaturated() else modifier,
            contentScale = ContentScale.Fit,
            colorFilter = if (style.tint) ColorFilter.tint(color) else null,
        )

        is RatingStyle.Emoji -> Box(
            modifier = if (filled) modifier else modifier.desaturated(),
            contentAlignment = Alignment.Center,
        ) {
            val fontSize = with(LocalDensity.current) { (itemSize * EmojiScale).toSp() }
            BasicText(
                text = style.emojiAt(index),
                style = TextStyle(fontSize = fontSize, textAlign = TextAlign.Center),
                maxLines = 1,
                softWrap = false,
            )
        }

        is RatingStyle.Custom -> Box(
            modifier = if (!filled && style.desaturateEmpty) modifier.desaturated() else modifier,
            contentAlignment = Alignment.Center,
        ) {
            style.content(index, filled)
        }
    }
}

/** Emoji glyphs are taller than their font size; this keeps them inside the item. */
private const val EmojiScale = 0.72f

/**
 * Clips the content to the filled part (`[0, fraction]` of the width from the start edge) or to the
 * rest of it.
 */
private fun Modifier.clipToFill(fraction: Float, filledPart: Boolean): Modifier = drawWithContent {
    val whole = if (filledPart) fraction >= 1f else fraction <= 0f
    if (whole) {
        drawContent()
        return@drawWithContent
    }
    val width = size.width
    val rtl = layoutDirection == LayoutDirection.Rtl
    val boundary = if (rtl) width * (1f - fraction) else width * fraction
    // In LTR the filled part is on the left; in RTL on the right.
    val drawLeft = filledPart != rtl
    if (drawLeft) {
        clipRect(left = 0f, top = 0f, right = boundary, bottom = size.height) {
            this@drawWithContent.drawContent()
        }
    } else {
        clipRect(left = boundary, top = 0f, right = width, bottom = size.height) {
            this@drawWithContent.drawContent()
        }
    }
}

private val DesaturatedPaint: Paint by lazy {
    Paint().apply {
        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
        alpha = RatingBarDefaults.EmptyContentAlpha
    }
}

/** Draws the content grey and faded, for empty emoji and untinted icons. Works on every API level. */
private fun Modifier.desaturated(): Modifier = drawWithContent {
    drawIntoCanvas { canvas ->
        canvas.saveLayer(size.toRect(), DesaturatedPaint)
        drawContent()
        canvas.restore()
    }
}
