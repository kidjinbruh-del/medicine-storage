package ru.medsstore.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Палитры. Их четыре, и у каждой свой светлый вариант, а не одна тема с
 * инверсией: на светлом фоне тёмно-янтарный акцент читается хуже, чем
 * жёлтый.
 */
data class Palette(
    val key: String,
    val title: String,
    val darkBg: Color,
    val darkSurface: Color,
    val darkLine: Color,
    val darkInk: Color,
    val darkMuted: Color,
    val darkAccent: Color,
    val lightBg: Color,
    val lightSurface: Color,
    val lightLine: Color,
    val lightInk: Color,
    val lightMuted: Color,
    val lightAccent: Color,
    val dark: Boolean = true,
) {
    /** Состояния срока годности не зависят от палитры: просроченное всегда
     *  красное, иначе человек привыкает, что «зелёное» — значит «в норме». */
    val danger: Color get() = if (dark) Color(0xFFE2606A) else Color(0xFFC0392B)
    val warn: Color get() = if (dark) Color(0xFFE3AC52) else Color(0xFFB07514)
    val ok: Color get() = if (dark) Color(0xFF62C0B2) else Color(0xFF0F7C70)
}

val palettes = listOf(
    Palette(
        key = "teal", title = "Бирюза",
        darkBg = Color(0xFF08181D), darkSurface = Color(0xFF0F242B), darkLine = Color(0xFF1B3540),
        darkInk = Color(0xFFE6F0F2), darkMuted = Color(0xFF8AA5AC), darkAccent = Color(0xFF4FB3A5),
        lightBg = Color(0xFFF2F7F7), lightSurface = Color(0xFFFFFFFF), lightLine = Color(0xFFDDE7E8),
        lightInk = Color(0xFF10262B), lightMuted = Color(0xFF5B7680), lightAccent = Color(0xFF0F7C70),
    ),
    Palette(
        key = "indigo", title = "Индиго",
        darkBg = Color(0xFF0B0F1E), darkSurface = Color(0xFF141A2E), darkLine = Color(0xFF232B45),
        darkInk = Color(0xFFE8EAF6), darkMuted = Color(0xFF939AB8), darkAccent = Color(0xFF8C97F8),
        lightBg = Color(0xFFF4F5FB), lightSurface = Color(0xFFFFFFFF), lightLine = Color(0xFFDFE1EF),
        lightInk = Color(0xFF171B33), lightMuted = Color(0xFF5C628A), lightAccent = Color(0xFF4451C8),
    ),
    Palette(
        key = "amber", title = "Янтарь",
        darkBg = Color(0xFF1A1408), darkSurface = Color(0xFF241C0D), darkLine = Color(0xFF3A2D14),
        darkInk = Color(0xFFF4ECDD), darkMuted = Color(0xFFB3A183), darkAccent = Color(0xFFE3AC52),
        lightBg = Color(0xFFFBF7EF), lightSurface = Color(0xFFFFFFFF), lightLine = Color(0xFFEDE3D0),
        lightInk = Color(0xFF2A2113), lightMuted = Color(0xFF7C6A48), lightAccent = Color(0xFFB07514),
    ),
    Palette(
        key = "graphite", title = "Графит",
        darkBg = Color(0xFF101214), darkSurface = Color(0xFF181B1E), darkLine = Color(0xFF2A2F34),
        darkInk = Color(0xFFE9ECEF), darkMuted = Color(0xFF98A2AC), darkAccent = Color(0xFFAEB9C3),
        lightBg = Color(0xFFF5F6F7), lightSurface = Color(0xFFFFFFFF), lightLine = Color(0xFFE1E4E7),
        lightInk = Color(0xFF1A1D20), lightMuted = Color(0xFF636C75), lightAccent = Color(0xFF4A5560),
    ),
)

fun paletteOf(key: String): Palette = palettes.firstOrNull { it.key == key } ?: palettes.first()

@Composable
fun MedsTheme(
    accent: String,
    themeMode: String,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        "light" -> false
        "system" -> systemDark
        else -> true
    }
    val p = paletteOf(accent)
    val scheme = if (dark) {
        darkColorScheme(
            primary = p.darkAccent,
            onPrimary = Color(0xFF06201E),
            background = p.darkBg,
            onBackground = p.darkInk,
            surface = p.darkSurface,
            onSurface = p.darkInk,
            surfaceVariant = p.darkSurface,
            onSurfaceVariant = p.darkMuted,
            outline = p.darkLine,
            error = p.danger,
        )
    } else {
        lightColorScheme(
            primary = p.lightAccent,
            onPrimary = Color(0xFFFFFFFF),
            background = p.lightBg,
            onBackground = p.lightInk,
            surface = p.lightSurface,
            onSurface = p.lightInk,
            surfaceVariant = p.lightSurface,
            onSurfaceVariant = p.lightMuted,
            outline = p.lightLine,
            error = p.danger,
        )
    }
    androidx.compose.runtime.CompositionLocalProvider(
        LocalPalette provides p.copy(dark = dark),
        LocalIsDark provides dark,
    ) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

val LocalPalette = androidx.compose.runtime.staticCompositionLocalOf { palettes.first() }
val LocalIsDark = androidx.compose.runtime.staticCompositionLocalOf { true }