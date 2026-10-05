package ru.medsstore.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.medsstore.data.MedEntity
import ru.medsstore.domain.Expiry
import ru.medsstore.domain.Reference
import ru.medsstore.ui.theme.LocalIsDark
import ru.medsstore.ui.theme.LocalPalette
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val FORMS = listOf("таблетки", "капсулы", "капли", "сироп", "мазь", "крем", "раствор", "свечи", "пластырь", "масло")
private val UNITS = listOf("шт", "уп", "мл", "г")
private val STORAGE_HINTS = listOf("2–8 °C", "в темноте", "комнатная t°", "в холодильнике")

/**
 * Редактор записи. Ключевое здесь — поле «как принимать» и «риски»: они есть
 * у каждой позиции. Если МНН совпал со справочником, текст подставляется, но
 * только в пустые поля: подсказка справочника не должна затирать то, что
 * человек написал сам.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedEditor(vm: MainViewModel, med: MedEntity) {
    val palette = LocalPalette.current
    val dark = LocalIsDark.current
    val ink = if (dark) palette.darkInk else palette.lightInk
    val muted = if (dark) palette.darkMuted else palette.lightMuted
    val line = if (dark) palette.darkLine else palette.lightLine
    val accent = if (dark) palette.darkAccent else palette.lightAccent

    var name by remember(med.id) { mutableStateOf(med.name) }
    var inn by remember(med.id) { mutableStateOf(med.inn) }
    var form by remember(med.id) { mutableStateOf(med.form) }
    var dose by remember(med.id) { mutableStateOf(med.dose) }
    var count by remember(med.id) { mutableStateOf(if (med.count > 0) med.count.toString() else "") }
    var unit by remember(med.id) { mutableStateOf(med.unit.ifBlank { "шт" }) }
    var expires by remember(med.id) { mutableStateOf(med.expiresOn) }
    var storage by remember(med.id) { mutableStateOf(med.storage) }
    var how by remember(med.id) { mutableStateOf(med.howToUse) }
    var risks by remember(med.id) { mutableStateOf(med.risks) }
    var note by remember(med.id) { mutableStateOf(med.note) }
    var showPicker by remember { mutableStateOf(false) }

    val suggestions = remember(inn) {
        if (inn.isBlank()) Reference.suggest("").take(0) else Reference.suggest(inn)
    }
    val matched = remember(inn) { Reference.find(inn) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Field("название", name, ink, { name = it }, placeholder = "например, амоксициллин 500 мг")
        Field("МНН", inn, ink, { inn = it }, placeholder = "международное название")

        if (suggestions.isNotEmpty()) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (s in suggestions) {
                    FilterChip(
                        selected = false,
                        onClick = {
                            inn = s.inn
                            if (how.isBlank()) how = s.how
                            if (risks.isBlank()) risks = s.risks
                        },
                        label = { Text(s.inn) },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = accent,
                            selectedContainerColor = accent.copy(alpha = 0.2f),
                        ),
                    )
                }
            }
        }
        if (matched != null) {
            Text(
                "Как принимать и риски подставлены из справочника — их можно править.",
                color = muted,
                style = MaterialTheme.typography.labelMedium,
            )
        }

        Field("форма выпуска", form, ink, { form = it }, placeholder = "таблетки, сироп…")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (f in FORMS) {
                FilterChip(
                    selected = form == f,
                    onClick = { form = if (form == f) "" else f },
                    label = { Text(f) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accent.copy(alpha = 0.22f),
                        selectedLabelColor = accent,
                    ),
                )
            }
        }

        Field("дозировка", dose, ink, { dose = it }, placeholder = "500 мг, 1 %, 20 капель")
        Field("сколько осталось", count, ink, { count = it.filter { ch -> ch.isDigit() } }, placeholder = "0")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (u in UNITS) {
                FilterChip(
                    selected = unit == u,
                    onClick = { unit = u },
                    label = { Text(u) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accent.copy(alpha = 0.22f),
                        selectedLabelColor = accent,
                    ),
                )
            }
        }

        // Срок годности: выбор даты, а не свободный текст — иначе в списке
        // появляются «через месяц» и «12.26», которые невозможно сравнить.
        Column {
            Text("срок годности", color = muted, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { showPicker = true }) {
                    Text(if (expires == null) "выбрать дату" else Expiry.toHuman(expires))
                }
                if (expires != null) {
                    TextButton(onClick = { expires = null }) { Text("убрать", color = muted) }
                }
            }
            if (expires != null) {
                val state = Expiry.state(expires, vm.today, vm.soonDays)
                val color = when (state) {
                    ru.medsstore.domain.ExpiryState.EXPIRED -> palette.danger
                    ru.medsstore.domain.ExpiryState.SOON -> palette.warn
                    ru.medsstore.domain.ExpiryState.OK -> palette.ok
                    ru.medsstore.domain.ExpiryState.NONE -> muted
                }
                Text(
                    Expiry.label(expires, vm.today, vm.soonDays),
                    color = color,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        Field("условия хранения", storage, ink, { storage = it }, placeholder = "например, в шкафу")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (h in STORAGE_HINTS) {
                FilterChip(
                    selected = storage == h,
                    onClick = { storage = if (storage == h) "" else h },
                    label = { Text(h) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accent.copy(alpha = 0.22f),
                        selectedLabelColor = accent,
                    ),
                )
            }
        }

        Field("как принимать", how, ink, { how = it }, multiline = true, placeholder = Reference.GENERIC_HOW)
        Field("риски и противопоказания", risks, ink, { risks = it }, multiline = true, placeholder = Reference.GENERIC_RISKS)
        Field("заметка", note, ink, { note = it }, multiline = true, placeholder = "для чего, кто выписал")

        Spacer(Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    vm.save(
                        med.copy(
                            name = name,
                            inn = inn.trim(),
                            form = form,
                            dose = dose.trim(),
                            count = count.toIntOrNull() ?: 0,
                            unit = unit,
                            expiresOn = expires,
                            storage = storage.trim(),
                            howToUse = how.trim(),
                            risks = risks.trim(),
                            note = note.trim(),
                        )
                    )
                },
                enabled = name.isNotBlank(),
            ) { Text("сохранить") }
            OutlinedButton(onClick = { vm.closeEditor() }) { Text("отмена") }
        }
        if (med.id != 0L) {
            TextButton(onClick = { vm.delete(med) }) {
                Text("удалить из хранилища", color = palette.danger)
            }
        }
        Spacer(Modifier.height(80.dp))
    }

    if (showPicker) {
        val initial = initialPickerDate(expires, vm.today)
        val state = rememberDatePickerState(
            initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        expires = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                    }
                    showPicker = false
                }) { Text("готово") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("отмена") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

private fun initialPickerDate(iso: String?, today: LocalDate): LocalDate = Expiry.parse(iso) ?: today

@Composable
private fun Field(
    label: String,
    value: String,
    ink: androidx.compose.ui.graphics.Color,
    onChange: (String) -> Unit,
    multiline: Boolean = false,
    placeholder: String = "",
) {
    Column {
        Text(label, color = ink.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { if (placeholder.isNotBlank()) Text(placeholder, color = ink.copy(alpha = 0.4f)) },
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = !multiline,
            minLines = if (multiline) 3 else 1,
            shape = RoundedCornerShape(12.dp),
        )
    }
}