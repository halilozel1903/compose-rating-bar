package io.github.halilozel1903.ratingbar.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Screenshots are taken by `scripts/screenshots.sh`, which starts the app with `--es scene <scene>`:
 *
 * - `summary`: the average and the distribution bars of the hotel's reviews (same as no extra)
 * - `review`: the "Rate your stay" card with 4.5 stars selected and a comment
 * - `styles`: stars, hearts, emoji, an icon, custom content and sizes
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val scene = SampleTab.fromScene(intent.getStringExtra(EXTRA_SCENE))
        setContent {
            val colors = if (isSystemInDarkTheme()) {
                darkColorScheme(
                    primary = Color(0xFF5EEAD4),
                    onPrimary = Color(0xFF00382F),
                    background = Color(0xFF0F1115),
                    surface = Color(0xFF1A1D23),
                    surfaceVariant = Color(0xFF2A2F37),
                    outlineVariant = Color(0xFF3A404A),
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF0F766E),
                    onPrimary = Color.White,
                    background = Color(0xFFF7F4EF),
                    surface = Color.White,
                    surfaceVariant = Color(0xFFECE7DF),
                    outlineVariant = Color(0xFFDCD5CA),
                )
            }
            MaterialTheme(colorScheme = colors) {
                // The Surface makes text default to onBackground, so it stays readable in dark mode.
                Surface(color = MaterialTheme.colorScheme.background) {
                    SampleApp(initialTab = scene)
                }
            }
        }
    }

    companion object {
        const val EXTRA_SCENE = "scene"
    }
}
