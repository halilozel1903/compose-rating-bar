package io.github.halilozel1903.ratingbar.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.ratingbar.RatingBar
import io.github.halilozel1903.ratingbar.RatingBarDefaults
import io.github.halilozel1903.ratingbar.RatingIndicator
import io.github.halilozel1903.ratingbar.RatingShapes
import io.github.halilozel1903.ratingbar.RatingStyle
import io.github.halilozel1903.ratingbar.core.RatingFormat
import io.github.halilozel1903.ratingbar.core.RatingStep

@Composable
fun StylesScreen() {
    val gold = RatingBarDefaults.colors(filledColor = RatingBarDefaults.Gold)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionCard {
            SectionTitle("Stars in three sizes")
            for (size in listOf<Dp>(20.dp, 30.dp, 42.dp)) {
                RatingIndicator(
                    value = 3.5f,
                    itemSize = size,
                    spacing = size / 8,
                    colors = gold,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }

        SectionCard {
            SectionTitle("Hearts, half steps")
            var hearts by rememberSaveable { mutableFloatStateOf(3.5f) }
            ValueRow(hearts) {
                RatingBar(
                    value = hearts,
                    onValueChange = { hearts = it },
                    style = RatingStyle.Heart,
                    colors = RatingBarDefaults.colors(filledColor = RatingBarDefaults.HeartRed),
                )
            }
        }

        SectionCard {
            SectionTitle("Emoji mood, one selected")
            var mood by rememberSaveable { mutableFloatStateOf(4f) }
            RatingBar(
                value = mood,
                onValueChange = { mood = it },
                step = RatingStep.Full,
                style = RatingStyle.Emoji(RatingStyle.Emoji.Moods, selectedOnly = true),
                itemSize = 40.dp,
                spacing = 10.dp,
            )
        }

        SectionCard {
            SectionTitle("Spice level, emoji and three items")
            var spice by rememberSaveable { mutableFloatStateOf(2f) }
            RatingBar(
                value = spice,
                onValueChange = { spice = it },
                max = 3,
                step = RatingStep.Full,
                style = RatingStyle.Emoji("🌶️"),
                itemSize = 36.dp,
            )
        }

        SectionCard {
            SectionTitle("Icon painter, free steps")
            var energy by rememberSaveable { mutableFloatStateOf(3.7f) }
            val bolt = rememberVectorPainter(Bolt)
            val style = remember(bolt) { RatingStyle.Painted(bolt) }
            ValueRow(energy) {
                RatingBar(
                    value = energy,
                    onValueChange = { energy = it },
                    step = RatingStep.Free,
                    style = style,
                    colors = RatingBarDefaults.colors(filledColor = Color(0xFFF59E0B)),
                )
            }
        }

        SectionCard {
            SectionTitle("Custom content and shapes")
            var level by rememberSaveable { mutableFloatStateOf(3f) }
            RatingBar(
                value = level,
                onValueChange = { level = it },
                step = RatingStep.Full,
                style = NumberedStyle,
                spacing = 8.dp,
            )
            RatingIndicator(
                value = 4.5f,
                style = RatingStyle.Shaped(RatingShapes.star(points = 6, innerRatio = 0.6f)),
                itemSize = 28.dp,
                colors = RatingBarDefaults.colors(filledColor = MaterialTheme.colorScheme.tertiary),
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        SectionCard {
            SectionTitle("Right to left and disabled")
            var rtl by rememberSaveable { mutableFloatStateOf(2.5f) }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                RatingBar(
                    value = rtl,
                    onValueChange = { rtl = it },
                    colors = gold,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            RatingBar(
                value = 4f,
                onValueChange = {},
                enabled = false,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 12.dp),
    )
}

@Composable
private fun ValueRow(value: Float, bar: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        bar()
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = RatingFormat.rating(value),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/** Numbered circles that fill with the theme color. */
private val NumberedStyle: RatingStyle = RatingStyle.Custom { index, filled ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${index + 1}",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** The Material "bolt" icon, built in code so the sample needs no icon dependency. */
private val Bolt: ImageVector = ImageVector.Builder(
    name = "Bolt",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    path(fill = SolidColor(Color.Black)) {
        moveTo(7f, 2f)
        verticalLineTo(13f)
        horizontalLineTo(10f)
        verticalLineTo(22f)
        lineTo(17f, 10f)
        horizontalLineTo(13f)
        lineTo(17f, 2f)
        close()
    }
}.build()
