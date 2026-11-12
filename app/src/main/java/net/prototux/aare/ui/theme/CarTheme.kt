package net.prototux.aare.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The in-car look: calm dark surfaces, one warm accent, big type and big targets.
 *
 * Car screens are viewed from ~70 cm at a glance, so sizes follow the car-UI rule of thumb:
 * touch targets ≥ 76 dp, body text ≥ 24 sp, and no more than two text styles per card.
 */
@Immutable
data class CarDimens(
    val touchTarget: Dp = 76.dp,
    val railWidth: Dp = 96.dp,
    val gutter: Dp = 12.dp,
    val cardRadius: Dp = 20.dp,
    val iconSize: Dp = 36.dp,
    val isCompact: Boolean = false,
)

/** Responsive sizing: tunes rail, gutters and touch targets for 7" / low-res car panels. */
fun carDimensFor(width: Dp, height: Dp): CarDimens {
    val isCompact = minOf(width, height) < 560.dp || width < 760.dp
    return if (isCompact) {
        CarDimens(
            touchTarget = 56.dp,
            railWidth = 68.dp,
            gutter = 8.dp,
            cardRadius = 14.dp,
            iconSize = 28.dp,
            isCompact = true,
        )
    } else {
        CarDimens(
            touchTarget = 74.dp,
            railWidth = 92.dp,
            gutter = 12.dp,
            cardRadius = 18.dp,
            iconSize = 34.dp,
            isCompact = false,
        )
    }
}

val LocalCarDimens = staticCompositionLocalOf { CarDimens() }

/** Electric Cyan cockpit accent: high visibility against dark cabin environments. */
private val CyanElectric = Color(0xFF00D1B2)
private val CyanDeep = Color(0xFF005649)

private val NightColors: ColorScheme = darkColorScheme(
    primary = CyanElectric,
    onPrimary = Color(0xFF001F1A),
    primaryContainer = Color(0xFF0C3831),
    onPrimaryContainer = Color(0xFF86F8E7),
    secondary = Color(0xFFB0C5C0),
    onSecondary = Color(0xFF1B312E),
    secondaryContainer = Color(0xFF1B242C),
    onSecondaryContainer = Color(0xFFD4E8E3),
    tertiary = Color(0xFFFFB25B),
    onTertiary = Color(0xFF331600),
    background = Color(0xFF060709),
    onBackground = Color(0xFFEDF1F5),
    surface = Color(0xFF0B0D12),
    onSurface = Color(0xFFEDF1F5),
    surfaceVariant = Color(0xFF151922),
    onSurfaceVariant = Color(0xFFA1AAB7),
    surfaceContainerLowest = Color(0xFF040507),
    surfaceContainerLow = Color(0xFF0E1117),
    surfaceContainer = Color(0xFF131720),
    surfaceContainerHigh = Color(0xFF1A202B),
    surfaceContainerHighest = Color(0xFF232A39),
    outline = Color(0xFF303B4F),
    outlineVariant = Color(0xFF1E2533),
    error = Color(0xFFFF5252),
)

private val DayColors: ColorScheme = lightColorScheme(
    primary = CyanDeep,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB9F5EA),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = Color(0xFF4A635F),
    secondaryContainer = Color(0xFFCCE8E2),
    onSecondaryContainer = Color(0xFF06201C),
    tertiary = Color(0xFF8B5000),
    background = Color(0xFFF3F7F6),
    onBackground = Color(0xFF161D1C),
    surface = Color(0xFFF3F7F6),
    onSurface = Color(0xFF161D1C),
    surfaceVariant = Color(0xFFDAE5E2),
    onSurfaceVariant = Color(0xFF3F4947),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFECF2F0),
    surfaceContainer = Color(0xFFE6ECEA),
    surfaceContainerHigh = Color(0xFFE0E7E5),
    surfaceContainerHighest = Color(0xFFD9E1DF),
    outline = Color(0xFF6F7977),
    outlineVariant = Color(0xFFBEC9C6),
)

private val CarType = Typography(
    displaySmall = TextStyle(fontSize = 44.sp, lineHeight = 52.sp, fontWeight = FontWeight.Medium),
    headlineMedium = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Medium),
    headlineSmall = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Medium),
    titleLarge = TextStyle(fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.Medium),
    titleMedium = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 24.sp, lineHeight = 32.sp),
    bodyMedium = TextStyle(fontSize = 22.sp, lineHeight = 30.sp),
    labelLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
)

private val CarShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun CarTheme(night: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (night) NightColors else DayColors,
        typography = CarType,
        shapes = CarShapes,
        content = content,
    )
}

/** Phone UI theme: same palette, normal phone type scale. */
@Composable
fun PhoneTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) NightColors else DayColors, content = content)
}
