package io.github.halilozel1903.ratingbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter

/**
 * What every item of a [RatingBar] or [RatingIndicator] looks like.
 *
 * Every style is drawn twice, once empty and once filled; the filled copy is clipped to the item's
 * fill fraction, so half and partial values work with any style.
 *
 * ```kotlin
 * RatingStyle.Star                                  // the default
 * RatingStyle.Heart
 * RatingStyle.Shaped(RatingShapes.star(points = 6))
 * RatingStyle.Painted(painterResource(R.drawable.ic_bolt))
 * RatingStyle.Emoji(RatingStyle.Emoji.Moods, selectedOnly = true)
 * RatingStyle.Custom { index, filled -> Text(if (filled) "A" else "a") }
 * ```
 */
@Stable
public sealed class RatingStyle {

    /**
     * A solid [shape] in the bar's colors: `filledColor` when filled, `emptyColor` when empty.
     */
    @Immutable
    public class Shaped(public val shape: Shape) : RatingStyle() {
        override fun equals(other: Any?): Boolean = other is Shaped && other.shape == shape
        override fun hashCode(): Int = shape.hashCode()
        override fun toString(): String = "RatingStyle.Shaped($shape)"
    }

    /**
     * An icon. With [tint] the icons are tinted with the bar's colors; without it they keep their own
     * colors and empty items are drawn desaturated and faded.
     *
     * @param filled The icon of a filled item.
     * @param empty The icon of an empty item, for example an outline. Defaults to [filled].
     */
    @Stable
    public class Painted(
        public val filled: Painter,
        public val empty: Painter = filled,
        public val tint: Boolean = true,
    ) : RatingStyle() {
        override fun equals(other: Any?): Boolean =
            other is Painted && other.filled == filled && other.empty == empty && other.tint == tint

        override fun hashCode(): Int = (filled.hashCode() * 31 + empty.hashCode()) * 31 + tint.hashCode()
        override fun toString(): String = "RatingStyle.Painted(tint=$tint)"
    }

    /**
     * Emoji (or any short text). Empty items are drawn desaturated and faded.
     *
     * @param emojis One emoji for every item, used in a cycle; a single emoji is used for all items.
     * @param selectedOnly When `true` only the item the value ends in is filled, which suits a mood
     * scale such as [Moods] better than filling every item before it.
     */
    @Immutable
    public class Emoji(
        public val emojis: List<String>,
        public val selectedOnly: Boolean = false,
    ) : RatingStyle() {

        /** The same [emoji] for every item. */
        public constructor(emoji: String) : this(listOf<String>(emoji))

        init {
            require(emojis.isNotEmpty()) { "At least one emoji is needed" }
        }

        internal fun emojiAt(index: Int): String = emojis[index % emojis.size]

        override fun equals(other: Any?): Boolean =
            other is Emoji && other.emojis == emojis && other.selectedOnly == selectedOnly

        override fun hashCode(): Int = emojis.hashCode() * 31 + selectedOnly.hashCode()
        override fun toString(): String = "RatingStyle.Emoji($emojis, selectedOnly=$selectedOnly)"

        public companion object {
            /** A five step mood scale from angry to delighted. */
            public val Moods: List<String> = listOf<String>(
                "😠", // angry face
                "🙁", // slightly frowning face
                "😐", // neutral face
                "🙂", // slightly smiling face
                "😍", // smiling face with heart eyes
            )
        }
    }

    /**
     * Your own item. [content] is called for the empty and for the filled copy of every item and is
     * placed in a box of the item's size.
     *
     * @param desaturateEmpty Draw the empty copy desaturated and faded, like [Emoji]. Leave it off
     * when [content] already draws empty items differently.
     */
    @Stable
    public class Custom(
        public val desaturateEmpty: Boolean = false,
        public val content: @Composable (index: Int, filled: Boolean) -> Unit,
    ) : RatingStyle() {
        override fun toString(): String = "RatingStyle.Custom(desaturateEmpty=$desaturateEmpty)"
    }

    public companion object {
        /** Five pointed stars, the default. */
        public val Star: RatingStyle = Shaped(RatingShapes.Star)

        /** Hearts. */
        public val Heart: RatingStyle = Shaped(RatingShapes.Heart)
    }
}
