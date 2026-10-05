package ru.medsstore.ui

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import ru.medsstore.ui.theme.LocalIsDark
import ru.medsstore.ui.theme.LocalPalette
import ru.medsstore.ui.theme.MedsTheme

@Composable
fun MedsApp(vm: MainViewModel) {
    MedsTheme(accent = vm.accent, themeMode = vm.themeMode) {
        val palette = LocalPalette.current
        val dark = LocalIsDark.current
        StatusBarIcons(dark)
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (dark) palette.darkBg else palette.lightBg,
        ) {
            Root(vm)
        }
    }
}

/**
 * Цвет значков в статус-баре и в панели навигации.
 *
 * `enableEdgeToEdge()` выбирает светлые или тёмные значки по теме системы, а не
 * по теме приложения. Приложение может быть тёмным при светлой системе — и в
 * первой версии значки в статус-баре были тёмными на тёмном фоне, то есть
 * время и заряд были почти не видны. Поэтому режим задаётся здесь, по выбору
 * пользователя, и меняется вместе с темой без перезапуска.
 */
@Composable
private fun StatusBarIcons(dark: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !dark
        controller.isAppearanceLightNavigationBars = !dark
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Root(vm: MainViewModel) {
    val palette = LocalPalette.current
    val dark = LocalIsDark.current
    val bg = if (dark) palette.darkBg else palette.lightBg
    val ink = if (dark) palette.darkInk else palette.lightInk
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(vm.notice) {
        vm.notice?.let {
            snack.showSnackbar(it)
            vm.consumeNotice()
        }
    }

    // Системная кнопка «назад» в редакторе возвращает к списку, а не закрывает
    // приложение вместе с несохранённой записью.
    BackHandler(enabled = vm.editing != null) { vm.closeEditor() }

    Scaffold(
        containerColor = bg,
        contentColor = ink,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (vm.editing == null) "Хранилище препаратов" else "Запись о препарате",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    // Стрелка появляется только в редакторе: из списка уходить
                    // некуда, а из редактора раньше можно было выйти только
                    // системной кнопкой «назад», то есть вовсе закрыть
                    // приложение вместе с половиной работы.
                    if (vm.editing != null) {
                        IconButton(onClick = { vm.closeEditor() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "назад к списку",
                                tint = ink,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bg,
                    titleContentColor = ink,
                ),
            )
        },
        floatingActionButton = {
            if (vm.editing == null) {
                ExtendedFloatingActionButton(
                    onClick = { vm.openNew() },
                    containerColor = if (dark) palette.darkAccent else palette.lightAccent,
                    contentColor = if (dark) Color(0xFF06201E) else Color.White,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text(" добавить", modifier = Modifier.padding(start = 6.dp))
                }
            }
        },
        snackbarHost = { SnackbarHost(snack) },
    ) { pad ->
        Box(modifier = Modifier.padding(pad)) {
            val med = vm.editing
            if (med == null) StorageScreen(vm, PaddingValues(bottom = 96.dp)) else MedEditor(vm, med)
        }
    }
}