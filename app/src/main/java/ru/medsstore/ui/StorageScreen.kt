package ru.medsstore.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.medsstore.data.MedEntity
import ru.medsstore.domain.Expiry
import ru.medsstore.domain.ExpiryState
import ru.medsstore.ui.theme.LocalIsDark
import ru.medsstore.ui.theme.LocalPalette
import java.time.LocalDate

/**
 * Список запаса. Порядок не «по добавлению», а по срочности: сверху то, что
 * просрочено и что скоро испортится, — иначе человек открывает приложение и
 * не видит проблемы, пока не долистает.
 */
@Composable
fun StorageScreen(vm: MainViewModel, contentPadding: PaddingValues) {
    val meds by vm.meds.collectAsState()
    val palette = LocalPalette.current
    val dark = LocalIsDark.current

    val surface = if (dark) palette.darkSurface else palette.lightSurface
    val ink = if (dark) palette.darkInk else palette.lightInk
    val muted = if (dark) palette.darkMuted else palette.lightMuted
    val line = if (dark) palette.darkLine else palette.lightLine
    val pad = if (vm.compact) 12.dp else 18.dp

    val today = vm.today
    val soon = vm.soonDays

    fun bucket(state: ExpiryState) = meds.filter { Expiry.state(it.expiresOn, today, soon) == state }

    val expired = bucket(ExpiryState.EXPIRED)
    val expiring = bucket(ExpiryState.SOON)
    val fresh = bucket(ExpiryState.OK)
    val noDate = bucket(ExpiryState.NONE)

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(if (vm.compact) 8.dp else 12.dp),
    ) {
        item {
            SummaryCard(
                total = meds.size,
                expired = expired.size,
                expiring = expiring.size,
                soonDays = soon,
                surface = surface, ink = ink, muted = muted, line = line,
                danger = palette.danger, warn = palette.warn, ok = palette.ok,
            )
        }

        if (meds.isEmpty()) {
            item { EmptyHint(surface, ink, muted, line) }
        }

        section("Просрочено", expired, vm, today, soon, surface, ink, muted, line, palette.danger, pad)
        section("Истекает скоро", expiring, vm, today, soon, surface, ink, muted, line, palette.warn, pad)
        section("В норме", fresh, vm, today, soon, surface, ink, muted, line, palette.ok, pad)
        section("Без срока", noDate, vm, today, soon, surface, ink, muted, line, muted, pad)

        item { DesignCard(vm, surface, ink, muted, line) }
        item { Disclaimer(muted) }
    }
}

private fun LazyListScope.section(
    title: String,
    rows: List<MedEntity>,
    vm: MainViewModel,
    today: LocalDate,
    soon: Int,
    surface: Color,
    ink: Color,
    muted: Color,
    line: Color,
    accentColor: Color,
    pad: Dp,
) {
    if (rows.isEmpty()) return
    item(key = "head-$title") {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            letterSpacing = 1.4.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
        )
    }
    items(rows, key = { it.id }) { med ->
        MedCard(
            med = med,
            today = today,
            soon = soon,
            surface = surface,
            ink = ink,
            muted = muted,
            line = line,
            stateColor = accentColor,
            pad = pad,
            onClick = { vm.open(med) },
        )
    }
}

@Composable
private fun SummaryCard(
    total: Int,
    expired: Int,
    expiring: Int,
    soonDays: Int,
    surface: Color,
    ink: Color,
    muted: Color,
    line: Color,
    danger: Color,
    warn: Color,
    ok: Color,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(surface, RoundedCornerShape(18.dp))
            .border(1.dp, line, RoundedCornerShape(18.dp))
            .padding(18.dp),
    ) {
        Text("В запасе", color = muted, style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(2.dp))
        Text(
            total.toString(),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold,
            color = ink,
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
            Counter(expired, "просрочено", danger)
            Counter(expiring, "до $soonDays дн.", warn)
            Counter(total - expired - expiring, "в норме", ok)
        }
    }
}

@Composable
private fun Counter(value: Int, label: String, color: Color) {
    Column {
        Text(
            value.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
        Text(label, color = color.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun EmptyHint(surface: Color, ink: Color, muted: Color, line: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(surface, RoundedCornerShape(18.dp))
            .border(1.dp, line, RoundedCornerShape(18.dp))
            .padding(20.dp),
    ) {
        Text("Пока пусто", color = ink, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            "Добавьте то, что лежит в аптечке: название, сколько осталось и срок " +
                "годности. Просроченное и подходящее к концу срока поднимутся наверх.",
            color = muted,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun MedCard(
    med: MedEntity,
    today: LocalDate,
    soon: Int,
    surface: Color,
    ink: Color,
    muted: Color,
    line: Color,
    stateColor: Color,
    pad: Dp,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(surface, RoundedCornerShape(16.dp))
            .border(1.dp, line, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(pad),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    med.name,
                    color = ink,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                )
                val sub = listOfNotNull(
                    med.inn.takeIf { it.isNotBlank() },
                    med.form.takeIf { it.isNotBlank() },
                    med.dose.takeIf { it.isNotBlank() },
                ).joinToString(" · ")
                if (sub.isNotBlank()) {
                    Text(
                        sub,
                        color = muted,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            if (med.count > 0) {
                Text(
                    "${med.count} ${med.unit}",
                    color = ink,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .background(stateColor.copy(alpha = 0.16f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    Expiry.label(med.expiresOn, today, soon),
                    color = stateColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
            if (med.expiresOn != null) {
                Spacer(Modifier.width(8.dp))
                Text(
                    "до ${Expiry.toHuman(med.expiresOn)}",
                    color = muted,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            if (med.storage.isNotBlank()) {
                Spacer(Modifier.width(8.dp))
                Text(
                    med.storage,
                    color = muted,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                )
            }
        }

        if (med.howToUse.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Как принимать: " + med.howToUse,
                color = muted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
            )
        }
        if (med.risks.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Риски: " + med.risks,
                color = muted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun Disclaimer(muted: Color) {
    Text(
        "Справочник в приложении общий и не заменяет инструкцию к препарату и врача. " +
            "Данные хранятся только на этом устройстве.",
        color = muted,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 4.dp),
    )
}