package me.vanguard.hyperdeck.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val HyperBlack = Color(0xFF0D0D11)
val HyperSurface = Color(0xFF16161D)
val HyperCard = Color(0xFF1E1E28)
val HyperCardBorder = Color(0xFF2E2E3C)
val HyperAccent = Color(0xFF007AFE) // HyperOS signature blue
val HyperAccentGlow = Color(0x33007AFE)
val HyperTextPrimary = Color(0xFFF5F5F7)
val HyperTextSecondary = Color(0xFF8E8E93)
val HyperRed = Color(0xFFFF453A)

private val DarkColorScheme = darkColorScheme(
    primary = HyperAccent,
    background = HyperBlack,
    surface = HyperSurface,
    surfaceVariant = HyperCard,
    onPrimary = Color.White,
    onBackground = HyperTextPrimary,
    onSurface = HyperTextPrimary,
    outline = HyperCardBorder
)

val HyperShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),  // Signature HyperOS squircle
    large = RoundedCornerShape(26.dp)
)

@Composable
fun HyperDeckTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        shapes = HyperShapes,
        content = content
    )
}
