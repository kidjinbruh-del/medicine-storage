package ru.medsstore.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.medsstore.data.AppContainer
import ru.medsstore.data.MedEntity
import java.time.LocalDate

class MainViewModel(private val container: AppContainer) : ViewModel() {

    private val dao = container.db.meds()
    private val prefs = container.settings

    val meds: StateFlow<List<MedEntity>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var themeMode by mutableStateOf(prefs.themeMode)
        private set
    var accent by mutableStateOf(prefs.accent)
        private set
    var compact by mutableStateOf(prefs.compact)
        private set
    var soonDays by mutableStateOf(prefs.soonDays)
        private set

    /** Что открыто в редакторе: null — список, иначе запись (новая — id = 0). */
    var editing by mutableStateOf<MedEntity?>(null)
        private set

    /** Короткое сообщение снизу после действия. */
    var notice by mutableStateOf<String?>(null)

    val today: LocalDate get() = LocalDate.now()

    fun chooseTheme(v: String) {
        themeMode = v
        prefs.themeMode = v
    }

    fun chooseAccent(v: String) {
        accent = v
        prefs.accent = v
    }

    fun toggleCompact(v: Boolean) {
        compact = v
        prefs.compact = v
    }

    fun chooseSoon(v: Int) {
        soonDays = v.coerceIn(7, 180)
        prefs.soonDays = soonDays
    }

    fun openNew() {
        editing = MedEntity(name = "", addedOn = ru.medsstore.domain.Expiry.toIso(today))
    }

    fun open(med: MedEntity) {
        editing = med
    }

    fun closeEditor() {
        editing = null
    }

    fun save(med: MedEntity) {
        viewModelScope.launch {
            val clean = med.copy(name = med.name.trim())
            if (med.id == 0L) dao.insert(clean) else dao.update(clean)
            editing = null
            notice = if (med.id == 0L) "Добавлено в хранилище" else "Сохранено"
        }
    }

    fun delete(med: MedEntity) {
        viewModelScope.launch {
            dao.delete(med)
            editing = null
            notice = "Удалено из хранилища"
        }
    }

    fun consumeNotice() {
        notice = null
    }
}