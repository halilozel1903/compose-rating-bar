package io.github.halilozel1903.ratingbar

import androidx.compose.foundation.shape.GenericShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** The shapes behind [RatingStyle.Star] and [RatingStyle.Heart], usable with [RatingStyle.Shaped] or anywhere else. */
public object RatingShapes {

    /** A five pointed star with softly rounded tips, centered in its bounds. */
    public val Star: Shape = GenericShape { size, _ -> addStar(size, points = 5, innerRatio = 0.48f, rounding = 0.14f) }

    /** A heart, centered in its bounds. */
    public val Heart: Shape = GenericShape { size, _ -> addHeart(size) }

    /**
     * A star with any number of [points]. [innerRatio] is the inner radius relative to the outer one
     * (smaller is spikier) and [rounding] how much of every edge is used to round the corners.
     */
    public fun star(points: Int = 5, innerRatio: Float = 0.48f, rounding: Float = 0.14f): Shape {
        require(points >= 3) { "A star needs at least 3 points, was $points" }
        require(innerRatio > 0f && innerRatio < 1f) { "innerRatio must be in (0, 1), was $innerRatio" }
        require(rounding >= 0f && rounding < 0.5f) { "rounding must be in [0, 0.5), was $rounding" }
        return GenericShape { size, _ -> addStar(size, points, innerRatio, rounding) }
    }
}

private fun Path.addStar(size: Size, points: Int, innerRatio: Float, rounding: Float) {
    val side = min(size.width, size.height)
    if (side <= 0f) return
    val step = Math.PI / points
    // Largest outer radius whose star fits into the square, and the vertical shift that centers it
    // (the bottom tips sit higher than the top tip is far from the center).
    var widest = 0.0
    var lowest = 0.0
    for (i in 0 until points) {
        val angle = -Math.PI / 2 + 2 * step * i
        widest = maxOf(widest, cos(angle))
        lowest = maxOf(lowest, sin(angle))
    }
    val radius = (side / maxOf(2 * widest, 1 + lowest)).toFloat()
    val cx = size.width / 2f
    val cy = size.height / 2f + radius * (1f - lowest.toFloat()) / 2f
    val vertices = Array<Offset>(points * 2) { i ->
        val r = if (i % 2 == 0) radius else radius * innerRatio
        val angle = -Math.PI / 2 + step * i
        Offset(cx + r * cos(angle).toFloat(), cy + r * sin(angle).toFloat())
    }
    roundedPolygon(vertices, rounding)
}

/** Draws a closed polygon whose corners are replaced by curves that start [rounding] along each edge. */
private fun Path.roundedPolygon(vertices: Array<Offset>, rounding: Float) {
    val n = vertices.size
    for (i in 0 until n) {
        val corner = vertices[i]
        val previous = vertices[(i + n - 1) % n]
        val next = vertices[(i + 1) % n]
        val start = corner + (previous - corner) * rounding
        val end = corner + (next - corner) * rounding
        if (i == 0) moveTo(start.x, start.y) else lineTo(start.x, start.y)
        // A quadratic curve through the corner, written as a cubic.
        val c1 = start + (corner - start) * (2f / 3f)
        val c2 = end + (corner - end) * (2f / 3f)
        cubicTo(c1.x, c1.y, c2.x, c2.y, end.x, end.y)
    }
    close()
}

/** The Material "favorite" heart, scaled from its 24 x 24 viewport and centered. */
private fun Path.addHeart(size: Size) {
    val side = min(size.width, size.height)
    if (side <= 0f) return
    val scale = side / 24f
    val left = (size.width - side) / 2f
    // The heart spans y 3..21.35 of the viewport; shift it so it is vertically centered.
    val top = (size.height - side) / 2f - 0.175f * scale
    fun x(value: Float) = left + value * scale
    fun y(value: Float) = top + value * scale
    moveTo(x(12f), y(21.35f))
    lineTo(x(10.55f), y(20.03f))
    cubicTo(x(5.4f), y(15.36f), x(2f), y(12.28f), x(2f), y(8.5f))
    cubicTo(x(2f), y(5.42f), x(4.42f), y(3f), x(7.5f), y(3f))
    cubicTo(x(9.24f), y(3f), x(10.91f), y(3.81f), x(12f), y(5.09f))
    cubicTo(x(13.09f), y(3.81f), x(14.76f), y(3f), x(16.5f), y(3f))
    cubicTo(x(19.58f), y(3f), x(22f), y(5.42f), x(22f), y(8.5f))
    cubicTo(x(22f), y(12.28f), x(18.6f), y(15.36f), x(13.45f), y(20.04f))
    lineTo(x(12f), y(21.35f))
    close()
}
