package me.vanguard.hyperdeck

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import me.vanguard.hyperdeck.data.api.InvidiousClient
import me.vanguard.hyperdeck.ui.screens.DeckMainScreen
import me.vanguard.hyperdeck.ui.theme.HyperDeckTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedVideoId = extractVideoIdFromIntent(intent)

        setContent {
            HyperDeckTheme {
                DeckMainScreen(initialVideoId = sharedVideoId)
            }
        }
    }

    private fun extractVideoIdFromIntent(intent: Intent?): String? {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null
            return InvidiousClient.extractVideoId(text)
        }
        return null
    }
}
