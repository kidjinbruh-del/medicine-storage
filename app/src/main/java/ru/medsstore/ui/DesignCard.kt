package ru.medsstore.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.medsstore.ui.theme.palettes

/**
 * Оформление. Меняется на месте, без перезапуска: состояние лежит в
 * SharedPreferences, а список перерисовывается сам.
 */
@Composable
fun DesignCard(
    vm: MainViewModel,
    surface: Color,
    ink: Color,
    muted: Color,
    line: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(surface, RoundedCornerShape(18.dp))
            .border(1.dp, line, RoundedCornerShape(18.dp))
            .padding(18.dp),
    ) {
        Text("Оформление", color = ink, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        Text("тема", color = muted, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for ((key, title) in listOf("dark" to "тёмная", "light" to "светлая", "system" to "как в системе")) {
                FilterChip(
                    selected = vm.themeMode == key,
                    onClick = { vm.chooseTheme(key) },
                    label = { Text(title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ink.copy(alpha = 0.14f),
                        selectedLabelColor = ink,
                    ),
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Text("палитра", color = muted, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (p in palettes) {
                val selected = vm.accent == p.key
                Box(
                    modifier = Modifier
                        .clickable { vm.chooseAccent(p.key) }
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) p.darkAccent else line,
                            shape = RoundedCornerShape(12.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .background(p.darkAccent, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text("Aa", color = p.darkBg, style = MaterialTheme.typography.labelMedium)
                        }
                        Spacer(Modifier.padding(start = 8.dp))
                        Text(
                            p.title,
                            color = if (selected) ink else muted,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Text("за сколько дней считать «скоро истекает»: ${vm.soonDays}", color = muted, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (d in listOf(14, 30, 60, 90)) {
                FilterChip(
                    selected = vm.soonDays == d,
                    onClick = { vm.chooseSoon(d) },
                    label = { Text("$d дн.") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ink.copy(alpha = 0.14f),
                        selectedLabelColor = ink,
                    ),
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilterChip(
                selected = vm.compact,
                onClick = { vm.toggleCompact(!vm.compact) },
                label = { Text("компактно") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ink.copy(alpha = 0.14f),
                    selectedLabelColor = ink,
                ),
            )
        }
    }
}