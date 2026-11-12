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
import androidx.compose.ui.graphics.luminance
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

private val ObsidianColors: ColorScheme = darkColorScheme(
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

/** Porsche Minimalist: refined monochrome platinum typography, stark deep contrast. */
private val PorscheColors: ColorScheme = darkColorScheme(
    primary = Color(0xFFF0F3F7),
    onPrimary = Color(0xFF0C0E12),
    primaryContainer = Color(0xFF2B313D),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFFBAC2CF),
    onSecondary = Color(0xFF181B22),
    secondaryContainer = Color(0xFF1E222B),
    onSecondaryContainer = Color(0xFFDEE3ED),
    tertiary = Color(0xFFC7CBD1),
    onTertiary = Color(0xFF14171C),
    background = Color(0xFF060608),
    onBackground = Color(0xFFF5F7FA),
    surface = Color(0xFF0C0E12),
    onSurface = Color(0xFFF5F7FA),
    surfaceVariant = Color(0xFF161920),
    onSurfaceVariant = Color(0xFFA0A7B5),
    surfaceContainerLowest = Color(0xFF040405),
    surfaceContainerLow = Color(0xFF0F1116),
    surfaceContainer = Color(0xFF15181F),
    surfaceContainerHigh = Color(0xFF1D212B),
    surfaceContainerHighest = Color(0xFF272D3A),
    outline = Color(0xFF3B4456),
    outlineVariant = Color(0xFF212733),
    error = Color(0xFFFF5252),
)

/** Cyber Amber: BMW/Polestar warm cockpit illumination for fatigue-free night driving. */
private val AmberColors: ColorScheme = darkColorScheme(
    primary = Color(0xFFFF9500),
    onPrimary = Color(0xFF261300),
    primaryContainer = Color(0xFF472600),
    onPrimaryContainer = Color(0xFFFFD199),
    secondary = Color(0xFFD4C2B0),
    onSecondary = Color(0xFF2E2317),
    secondaryContainer = Color(0xFF261F17),
    onSecondaryContainer = Color(0xFFF5E4D3),
    tertiary = Color(0xFFFFB86B),
    onTertiary = Color(0xFF331600),
    background = Color(0xFF070605),
    onBackground = Color(0xFFF5EDE6),
    surface = Color(0xFF0D0B09),
    onSurface = Color(0xFFF5EDE6),
    surfaceVariant = Color(0xFF1B1612),
    onSurfaceVariant = Color(0xFFBAAEA2),
    surfaceContainerLowest = Color(0xFF050403),
    surfaceContainerLow = Color(0xFF120F0C),
    surfaceContainer = Color(0xFF181410),
    surfaceContainerHigh = Color(0xFF211C17),
    surfaceContainerHighest = Color(0xFF2C251F),
    outline = Color(0xFF4C3E33),
    outlineVariant = Color(0xFF29211A),
    error = Color(0xFFFF5252),
)

/** M-Sport Racing Red: high-performance dark theme with track red indicators. */
private val RacingColors: ColorScheme = darkColorScheme(
    primary = Color(0xFFFF3B30),
    onPrimary = Color(0xFF290200),
    primaryContainer = Color(0xFF4D0904),
    onPrimaryContainer = Color(0xFFFFB4AB),
    secondary = Color(0xFFC9BDBC),
    onSecondary = Color(0xFF2A201F),
    secondaryContainer = Color(0xFF241C1B),
    onSecondaryContainer = Color(0xFFECDFDE),
    tertiary = Color(0xFF007AFF),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFF070708),
    onBackground = Color(0xFFF2EDED),
    surface = Color(0xFF0D0D10),
    onSurface = Color(0xFFF2EDED),
    surfaceVariant = Color(0xFF1A181A),
    onSurfaceVariant = Color(0xFFB5AAA9),
    surfaceContainerLowest = Color(0xFF050506),
    surfaceContainerLow = Color(0xFF111114),
    surfaceContainer = Color(0xFF18171B),
    surfaceContainerHigh = Color(0xFF201F25),
    surfaceContainerHighest = Color(0xFF2C2A33),
    outline = Color(0xFF4A4349),
    outlineVariant = Color(0xFF262125),
    error = Color(0xFFFF5252),
)

/** Classic Teal: original Aare turquoise palette. */
private val ClassicTealColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF3DDBC6),
    onPrimary = Color(0xFF00201C),
    primaryContainer = Color(0xFF0E3B36),
    onPrimaryContainer = Color(0xFFB9F5EA),
    secondary = Color(0xFFB4CAC5),
    onSecondary = Color(0xFF1F3531),
    secondaryContainer = Color(0xFF263330),
    onSecondaryContainer = Color(0xFFD0E6E1),
    tertiary = Color(0xFFFFB86B),
    onTertiary = Color(0xFF2E1500),
    background = Color(0xFF0B0F0F),
    onBackground = Color(0xFFE3E9E7),
    surface = Color(0xFF0B0F0F),
    onSurface = Color(0xFFE3E9E7),
    surfaceVariant = Color(0xFF1A2120),
    onSurfaceVariant = Color(0xFFAFBAB7),
    surfaceContainerLowest = Color(0xFF060909),
    surfaceContainerLow = Color(0xFF121818),
    surfaceContainer = Color(0xFF161D1C),
    surfaceContainerHigh = Color(0xFF1D2524),
    surfaceContainerHighest = Color(0xFF26302E),
    outline = Color(0xFF5E6B68),
    outlineVariant = Color(0xFF2E3836),
    error = Color(0xFFFF8A80),
)

private val NightColors = ObsidianColors

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
fun CarTheme(night: Boolean, theme: String = "obsidian", content: @Composable () -> Unit) {
    val darkColors = when (theme.lowercase()) {
        "porsche" -> PorscheColors
        "amber" -> AmberColors
        "racing" -> RacingColors
        "classic" -> ClassicTealColors
        else -> ObsidianColors
    }
    MaterialTheme(
        colorScheme = if (night) darkColors else DayColors,
        typography = CarType,
        shapes = CarShapes,
        content = content,
    )
}

/** Phone UI theme: same palette, normal phone type scale. */
@Composable
fun PhoneTheme(dark: Boolean, theme: String = "obsidian", content: @Composable () -> Unit) {
    val darkColors = when (theme.lowercase()) {
        "porsche" -> PorscheColors
        "amber" -> AmberColors
        "racing" -> RacingColors
        "classic" -> ClassicTealColors
        else -> ObsidianColors
    }
    MaterialTheme(colorScheme = if (dark) darkColors else DayColors, content = content)
}

// ---------------------------------------------------------------- car-style accent (phone UI)

private val AuroraAccent = Color(0xFFFF5E9E) to Color(0xFF9C1C64)
private val ForestAccentPair = Color(0xFF5FD66C) to Color(0xFF1B5E20)
private val SportAccentPair = Color(0xFFFF4D3D) to Color(0xFFB71C1C)

/** The accent a car style reads as on the phone: (vivid, for dark mode) to (deep, for light mode). */
private fun accentPairFor(style: String): Pair<Color, Color> = when (style) {
    "aurora" -> AuroraAccent
    "forest" -> ForestAccentPair
    "sport" -> SportAccentPair
    else -> Color(0xFF00D1B2) to Color(0xFF005649)
}

private fun blend(a: Color, b: Color, t: Float) = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t,
)

private fun onColorFor(bg: Color) = if (bg.luminance() > 0.45f) Color(0xFF101418) else Color.White

/**
 * [scheme] with its brand accent swapped for the chosen car style's.
 */
fun styleAccentScheme(scheme: ColorScheme, dark: Boolean, style: String): ColorScheme {
    val (vivid, deep) = accentPairFor(style)
    val primary = if (dark) vivid else deep
    val container = blend(primary, scheme.surface, if (dark) 0.78f else 0.85f)
    return scheme.copy(
        primary = primary,
        onPrimary = onColorFor(primary),
        primaryContainer = container,
        onPrimaryContainer = onColorFor(container),
        tertiary = if (dark) vivid else deep,
    )
}

private val BrandPink = Color(0xFFFF4D63)
private val BrandPinkDeep = Color(0xFFB71C3E)

private val BrandNightBackground = Color(0xFF07090F)
private val BrandNightSurface = Color(0xFF0B0D12)
private val BrandNightSurfaceContainer = Color(0xFF131720)
private val BrandNightSurfaceContainerHigh = Color(0xFF1A202B)
private val BrandNightSurfaceContainerHighest = Color(0xFF232A39)
private val BrandNightSurfaceVariant = Color(0xFF151922)

/**
 * [scheme] with brand accent and dark mode palette for companion phone app.
 */
fun brandAccentScheme(scheme: ColorScheme, dark: Boolean): ColorScheme {
    val primary = if (dark) BrandPink else BrandPinkDeep
    val container = blend(primary, scheme.surface, if (dark) 0.78f else 0.85f)
    val base = if (dark) {
        scheme.copy(
            background = BrandNightBackground,
            surface = BrandNightSurface,
            surfaceVariant = BrandNightSurfaceVariant,
            surfaceContainer = BrandNightSurfaceContainer,
            surfaceContainerHigh = BrandNightSurfaceContainerHigh,
            surfaceContainerHighest = BrandNightSurfaceContainerHighest,
            surfaceContainerLow = BrandNightBackground,
            surfaceContainerLowest = BrandNightBackground,
        )
    } else scheme
    return base.copy(
        primary = primary,
        onPrimary = Color.White,
        primaryContainer = container,
        onPrimaryContainer = onColorFor(container),
        tertiary = primary,
        secondary = primary,
    )
}
