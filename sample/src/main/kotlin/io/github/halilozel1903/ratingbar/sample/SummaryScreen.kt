package io.github.halilozel1903.ratingbar.sample

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.halilozel1903.ratingbar.RatingBarDefaults
import io.github.halilozel1903.ratingbar.RatingIndicator
import io.github.halilozel1903.ratingbar.RatingSummary
import io.github.halilozel1903.ratingbar.core.RatingDistribution

/** 1,284 reviews averaging 4.6. */
val HotelRatings: RatingDistribution = RatingDistribution.of(5 to 980, 4 to 190, 3 to 58, 2 to 22, 1 to 34)

data class GuestReview(
    val name: String,
    val avatarColor: Color,
    val rating: Float,
    val date: String,
    val text: String,
)

val GuestReviews: List<GuestReview> = listOf<GuestReview>(
    GuestReview(
        name = "Marta Silva",
        avatarColor = Color(0xFFE76F51),
        rating = 5f,
        date = "September 2026",
        text = "Spotless room with a view over the rooftops, and the terrace breakfast was worth waking up for.",
    ),
    GuestReview(
        name = "Jonas Keller",
        avatarColor = Color(0xFF2A9D8F),
        rating = 4f,
        date = "September 2026",
        text = "Perfect location for walking everywhere. A little noisy on Friday night, earplugs were on the pillow.",
    ),
    GuestReview(
        name = "Aiko Tanaka",
        avatarColor = Color(0xFF6D597A),
        rating = 4.5f,
        date = "August 2026",
        text = "The staff remembered our names by the second day and booked us a great fado dinner.",
    ),
)

/** A white rounded card that works in both themes. */
@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
    ) {
        Column(Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun SummaryScreen() {
    val starColors = RatingBarDefaults.colors(filledColor = RatingBarDefaults.Gold)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionCard {
            Text(
                text = "Guest reviews",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.size(16.dp))
            RatingSummary(
                distribution = HotelRatings,
                colors = starColors,
                barColor = MaterialTheme.colorScheme.primary,
                showPercentages = true,
            )
        }
        for (review in GuestReviews) {
            ReviewCard(review)
        }
    }
}

@Composable
private fun ReviewCard(review: GuestReview) {
    SectionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(review.avatarColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = review.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(review.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    text = review.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            RatingIndicator(
                value = review.rating,
                itemSize = 16.dp,
                colors = RatingBarDefaults.colors(filledColor = RatingBarDefaults.Gold),
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(review.text, style = MaterialTheme.typography.bodyMedium)
    }
}
