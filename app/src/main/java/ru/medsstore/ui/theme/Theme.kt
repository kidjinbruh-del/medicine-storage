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
    /**
     * Состояния срока годности.
     *
     * Значения — из палитры «Дневника давления», чтобы «просрочено» и «скоро»
     * выглядели одинаково в обоих приложениях. Просроченное всегда красное:
     * иначе человек привыкает, что «зелёное» — значит «в норме».
     */
    val danger: Color get() = if (dark) Color(0xFFF28F8F) else Color(0xFFB3261E)
    val warn: Color get() = if (dark) darkAccent else lightAccent
    val ok: Color get() = if (dark) Color(0xFF9FD9A0) else Color(0xFF3F6B32)
}

/**
 * Палитры — те же четыре, что в «Дневнике давления», с теми же цветами.
 *
 * Раньше здесь были свои четыре (Бирюза, Индиго, Янтарь, Графит). Названия
 * «Графит» совпадал, остальные нет, и рядом друг с другом приложения выглядели
 * как из разных семейств. Общего кода между приложениями нет и не будет: они
 * собираются отдельно, поэтому палитры просто повторяются числами. При
 * правке одной стороны правьте и другую.
 */
val palettes = listOf(
    Palette(
        key = "espresso", title = "Эспрессо",
        darkBg = Color(0xFF14100A), darkSurface = Color(0xFF1D1710), darkLine = Color(0xFF3A3126),
        darkInk = Color(0xFFF4EAD9), darkMuted = Color(0xFFCBB89F), darkAccent = Color(0xFFEFA94A),
        lightBg = Color(0xFFFAF4E8), lightSurface = Color(0xFFFFFDF8), lightLine = Color(0xFFDCCFB6),
        lightInk = Color(0xFF241A0F), lightMuted = Color(0xFF6B5A44), lightAccent = Color(0xFF8A5A17),
    ),
    Palette(
        key = "graphite", title = "Графит",
        darkBg = Color(0xFF0E1113), darkSurface = Color(0xFF171B1E), darkLine = Color(0xFF2E353C),
        darkInk = Color(0xFFE8EDF0), darkMuted = Color(0xFF9AA5AE), darkAccent = Color(0xFFA8D84A),
        lightBg = Color(0xFFF2F5F6), lightSurface = Color(0xFFFFFFFF), lightLine = Color(0xFFD3DADE),
        lightInk = Color(0xFF16191C), lightMuted = Color(0xFF55606A), lightAccent = Color(0xFF4E6B12),
    ),
    Palette(
        key = "tide", title = "Прилив",
        darkBg = Color(0xFF07161A), darkSurface = Color(0xFF0E2026), darkLine = Color(0xFF22383C),
        darkInk = Color(0xFFDCF2F2), darkMuted = Color(0xFF92B4B8), darkAccent = Color(0xFF4ED9C4),
        lightBg = Color(0xFFEFF7F7), lightSurface = Color(0xFFFFFFFF), lightLine = Color(0xFFC9DCDE),
        lightInk = Color(0xFF0B1F22), lightMuted = Color(0xFF46656A), lightAccent = Color(0xFF006A5E),
    ),
    Palette(
        key = "paper", title = "Бумага",
        darkBg = Color(0xFF1A1414), darkSurface = Color(0xFF241C1C), darkLine = Color(0xFF3E302E),
        darkInk = Color(0xFFF6EAE8), darkMuted = Color(0xFFC6ACA8), darkAccent = Color(0xFFE08A7A),
        lightBg = Color(0xFFFAF3F0), lightSurface = Color(0xFFFFFFFF), lightLine = Color(0xFFE7D5D0),
        lightInk = Color(0xFF211715), lightMuted = Color(0xFF6E5550), lightAccent = Color(0xFF9C3A2B),
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