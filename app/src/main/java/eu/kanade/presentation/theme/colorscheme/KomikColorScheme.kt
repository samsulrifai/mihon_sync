package eu.kanade.presentation.theme.colorscheme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Colors for KOMIK theme, matching the Mihon Sync logo
 *
 * Key colors:
 * Primary #FF0510
 * Tertiary #705C2E
 */
internal object KomikColorScheme : BaseColorScheme() {

    override val darkScheme = darkColorScheme(
        primary = Color(0xFFFFB4AB),
        onPrimary = Color(0xFF690005),
        primaryContainer = Color(0xFFC00010),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFFFF5449), // Unread badge
        onSecondary = Color(0xFF410002), // Unread badge text
        secondaryContainer = Color(0xFF93000A), // Navigation bar selector pill & progress indicator (remaining)
        onSecondaryContainer = Color(0xFFFFDAD6), // Navigation bar selector icon
        tertiary = Color(0xFFDFC38C), // Downloaded badge
        onTertiary = Color(0xFF3E2E04), // Downloaded badge text
        tertiaryContainer = Color(0xFF574419),
        onTertiaryContainer = Color(0xFFFCDFA6),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF1A1111),
        onBackground = Color(0xFFF1DEDC),
        surface = Color(0xFF1A1111),
        onSurface = Color(0xFFF1DEDC),
        surfaceVariant = Color(0xFF271D1C), // Navigation bar background (ThemePrefWidget)
        onSurfaceVariant = Color(0xFFD8C2BF),
        outline = Color(0xFFA08C8A),
        outlineVariant = Color(0xFF534341),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFFF1DEDC),
        inverseOnSurface = Color(0xFF392E2D),
        inversePrimary = Color(0xFFC00010),
        surfaceDim = Color(0xFF1A1111),
        surfaceBright = Color(0xFF423736),
        surfaceContainerLowest = Color(0xFF140C0B),
        surfaceContainerLow = Color(0xFF231918),
        surfaceContainer = Color(0xFF271D1C), // Navigation bar background
        surfaceContainerHigh = Color(0xFF322827),
        surfaceContainerHighest = Color(0xFF3D3231),
    )

    override val lightScheme = lightColorScheme(
        primary = Color(0xFFC00010),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFF0510),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFFC00010), // Unread badge
        onSecondary = Color(0xFFFFFFFF), // Unread badge text
        secondaryContainer = Color(0xFFFFDAD6), // Navigation bar selector pill & progress indicator (remaining)
        onSecondaryContainer = Color(0xFF410002), // Navigation bar selector icon
        tertiary = Color(0xFF705C2E), // Downloaded badge
        onTertiary = Color(0xFFFFFFFF), // Downloaded badge text
        tertiaryContainer = Color(0xFFFCDFA6),
        onTertiaryContainer = Color(0xFF251A00),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFFFF8F7),
        onBackground = Color(0xFF231918),
        surface = Color(0xFFFFF8F7),
        onSurface = Color(0xFF231918),
        surfaceVariant = Color(0xFFFCEAE7), // Navigation bar background (ThemePrefWidget)
        onSurfaceVariant = Color(0xFF534341),
        outline = Color(0xFF857371),
        outlineVariant = Color(0xFFD8C2BF),
        scrim = Color(0xFF000000),
        inverseSurface = Color(0xFF392E2D),
        inverseOnSurface = Color(0xFFFFEDEA),
        inversePrimary = Color(0xFFFFB4AB),
        surfaceDim = Color(0xFFE8D6D3),
        surfaceBright = Color(0xFFFFF8F7),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFFFF0EE),
        surfaceContainer = Color(0xFFFCEAE7), // Navigation bar background
        surfaceContainerHigh = Color(0xFFF6E4E2),
        surfaceContainerHighest = Color(0xFFF1DEDC),
    )
}
