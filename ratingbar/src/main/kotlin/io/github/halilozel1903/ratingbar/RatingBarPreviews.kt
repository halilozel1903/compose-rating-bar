package io.github.halilozel1903.ratingbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.ratingbar.core.RatingDistribution
import io.github.halilozel1903.ratingbar.core.RatingStep

@Preview(showBackground = true)
@Composable
private fun RatingBarPreview() {
    MaterialTheme {
        Surface {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                var stars by remember { mutableFloatStateOf(3.5f) }
                RatingBar(
                    value = stars,
                    onValueChange = { stars = it },
                    colors = RatingBarDefaults.colors(filledColor = RatingBarDefaults.Gold),
                )
                var hearts by remember { mutableFloatStateOf(4f) }
                RatingBar(value = hearts, onValueChange = { hearts = it }, style = RatingStyle.Heart, step = RatingStep.Full)
                var mood by remember { mutableFloatStateOf(4f) }
                RatingBar(
                    value = mood,
                    onValueChange = { mood = it },
                    style = RatingStyle.Emoji(RatingStyle.Emoji.Moods, selectedOnly = true),
                    step = RatingStep.Full,
                )
                RatingIndicator(value = 4.6f)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun RatingSummaryPreview() {
    MaterialTheme {
        Surface {
            RatingSummary(
                distribution = RatingDistribution.of(5 to 980, 4 to 190, 3 to 58, 2 to 22, 1 to 34),
                modifier = Modifier.padding(16.dp),
                animate = false,
            )
        }
    }
}
