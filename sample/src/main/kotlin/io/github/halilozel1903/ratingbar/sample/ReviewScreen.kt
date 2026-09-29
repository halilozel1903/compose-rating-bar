package io.github.halilozel1903.ratingbar.sample

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.ratingbar.RatingBar
import io.github.halilozel1903.ratingbar.RatingBarDefaults
import io.github.halilozel1903.ratingbar.core.RatingFormat
import io.github.halilozel1903.ratingbar.core.RatingStep

@Composable
fun ReviewScreen() {
    var overall by rememberSaveable { mutableFloatStateOf(4.5f) }
    var cleanliness by rememberSaveable { mutableFloatStateOf(5f) }
    var location by rememberSaveable { mutableFloatStateOf(5f) }
    var service by rememberSaveable { mutableFloatStateOf(4f) }
    var comment by rememberSaveable {
        mutableStateOf("Lovely room with a view over the river. Breakfast on the terrace was the highlight of our trip!")
    }
    var posted by rememberSaveable { mutableStateOf(false) }
    val gold = RatingBarDefaults.colors(filledColor = RatingBarDefaults.Gold)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
    ) {
        SectionCard {
            Text(
                text = "Rate your stay",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Sea View Double, 26 to 29 September",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                RatingBar(
                    value = overall,
                    onValueChange = { overall = it; posted = false },
                    step = RatingStep.Half,
                    itemSize = 44.dp,
                    spacing = 8.dp,
                    colors = gold,
                )
                Spacer(Modifier.height(8.dp))
                AnimatedContent(targetState = overall, label = "rating label") { value ->
                    Text(
                        text = if (value > 0f) "${RatingFormat.rating(value)}  ${labelFor(value)}" else "Tap a star to rate",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AspectRow("Cleanliness", cleanliness) { cleanliness = it }
                AspectRow("Location", location) { location = it }
                AspectRow("Service", service) { service = it }
            }
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it; posted = false },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Tell other guests about your stay") },
                minLines = 3,
                shape = RoundedCornerShape(16.dp),
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { posted = true },
                enabled = overall > 0f && !posted,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (posted) "Thanks for your review" else "Post review")
            }
        }
    }
}

@Composable
private fun AspectRow(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        RatingBar(
            value = value,
            onValueChange = onValueChange,
            step = RatingStep.Full,
            itemSize = 22.dp,
            spacing = 4.dp,
            valueDescription = { v, max -> "$label: ${RatingFormat.rating(v)} of $max" },
        )
    }
}

private fun labelFor(value: Float): String = when {
    value >= 4.5f -> "Excellent"
    value >= 3.5f -> "Very good"
    value >= 2.5f -> "Good"
    value >= 1.5f -> "Fair"
    else -> "Poor"
}
