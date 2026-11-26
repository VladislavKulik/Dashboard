package com.example.lifedashboard.feature.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.HabitColor
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.ColorPaletteSheet
import com.example.lifedashboard.ui.components.EditorBottomBar
import com.example.lifedashboard.ui.components.EditorPropertyChip
import com.example.lifedashboard.ui.components.EditorToolButton
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PaletteColorOption
import com.example.lifedashboard.ui.theme.AppColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabitEditorScreen(
    initialHabit: DashboardHabit?,
    onBack: () -> Unit,
    onSave: (DashboardHabit, (() -> Unit)?) -> Unit,
    operationError: String? = null
) {
    var title by rememberSaveable(initialHabit?.id) { mutableStateOf(initialHabit?.title.orEmpty()) }
    var selectedColor by rememberSaveable(initialHabit?.id) { mutableStateOf(initialHabit?.color ?: HabitColor.PINK) }
    var reminderEnabled by rememberSaveable(initialHabit?.id) { mutableStateOf(initialHabit?.reminderEnabled ?: false) }
    var reminderTime by rememberSaveable(initialHabit?.id) { mutableStateOf(initialHabit?.reminderTime ?: "09:00") }
    var selectedDays by remember(initialHabit?.id) {
        mutableStateOf(initialHabit?.reminderDays?.ifEmpty { (1..7).toSet() } ?: (1..7).toSet())
    }
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }

    var showScheduleAndReminderSheet by remember { mutableStateOf(false) }
    var showPaletteSheet by remember { mutableStateOf(false) }

    val colorOptions = listOf(
        PaletteColorOption(HabitColor.PINK, "Розовый", AppColors.Pink),
        PaletteColorOption(HabitColor.MINT, "Мятный", AppColors.Mint),
        PaletteColorOption(HabitColor.BLUE, "Голубой", AppColors.Blue),
        PaletteColorOption(HabitColor.YELLOW, "Жёлтый", AppColors.Yellow),
        PaletteColorOption(HabitColor.LAVENDER, "Лавандовый", AppColors.Lavender)
    )

    val timeIsValid = reminderTime.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d"))
    val canSave = title.isNotBlank() && selectedDays.isNotEmpty() && (!reminderEnabled || timeIsValid)

    val scheduleSummary = remember(selectedDays) {
        when (selectedDays) {
            (1..7).toSet() -> "Каждый день"
            (1..5).toSet() -> "Будни (Пн–Пт)"
            setOf(6, 7) -> "Выходные (Сб–Вс)"
            else -> {
                val names = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                selectedDays.sorted().mapNotNull { names.getOrNull(it - 1) }.joinToString(" ")
            }
        }
    }

    fun submit() {
        attemptedSubmit = true
        if (!canSave) return
        val reminderLabel = if (reminderEnabled) "${selectedDays.size} дн. · $reminderTime" else "Не настроено"
        val habit = (initialHabit ?: DashboardHabit(id = "", title = title, color = selectedColor)).copy(
            title = title.trim(),
            color = selectedColor,
            reminderEnabled = reminderEnabled,
            reminderTime = if (timeIsValid) reminderTime else "09:00",
            reminderDays = selectedDays,
            reminderLabel = reminderLabel
        )
        onSave(habit, onBack)
    }

    val weekdayShorts = listOf(1 to "Пн", 2 to "Вт", 3 to "Ср", 4 to "Чт", 5 to "Пт", 6 to "Сб", 7 to "Вс")

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    CircleIconButton(icon = Icons.Outlined.Close, contentDescription = "Закрыть", onClick = onBack)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = if (initialHabit == null) "Новая привычка" else "Редактировать привычку",
                        style = MaterialTheme.typography.headlineLarge,
                        color = AppColors.Ink
                    )
                }
            }

            if (!operationError.isNullOrBlank()) {
                item { Text(operationError, color = AppColors.Error, fontSize = 12.sp) }
            }

            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Название привычки") },
                    placeholder = { Text("Например: Прогулка 30 минут") },
                    isError = attemptedSubmit && title.isBlank(),
                    supportingText = if (attemptedSubmit && title.isBlank()) {
                        { Text("Введите название привычки", color = AppColors.Error) }
                    } else null,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.White,
                        unfocusedContainerColor = AppColors.White,
                        errorContainerColor = AppColors.White,
                        focusedBorderColor = AppColors.Ink,
                        unfocusedBorderColor = Color.Transparent,
                        errorBorderColor = AppColors.Error
                    )
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Дни недели", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekdayShorts.forEach { (day, label) ->
                            val selected = day in selectedDays
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) AppColors.Ink else AppColors.White)
                                    .clickable {
                                        selectedDays = if (selected) {
                                            (selectedDays - day).ifEmpty { setOf(day) }
                                        } else {
                                            selectedDays + day
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) AppColors.White else AppColors.Ink,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EditorPropertyChip(
                        icon = Icons.Outlined.Palette,
                        label = colorOptions.firstOrNull { it.value == selectedColor }?.label ?: "Цвет",
                        backgroundColor = habitColor(selectedColor),
                        onClick = { showPaletteSheet = true }
                    )
                    EditorPropertyChip(
                        icon = Icons.Outlined.Repeat,
                        label = scheduleSummary,
                        backgroundColor = AppColors.Mint.copy(alpha = 0.65f),
                        onClick = { showScheduleAndReminderSheet = true }
                    )
                    if (reminderEnabled) {
                        EditorPropertyChip(
                            icon = Icons.Outlined.NotificationsNone,
                            label = "Напомнить в $reminderTime",
                            backgroundColor = AppColors.Yellow,
                            onClick = { showScheduleAndReminderSheet = true },
                            onClear = { reminderEnabled = false }
                        )
                    }
                }
            }
        }

        EditorBottomBar(
            confirmEnabled = canSave,
            onConfirm = ::submit,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            // Unified Schedule + Reminder button (combines Days & Notification Time)
            EditorToolButton(
                icon = Icons.Outlined.AccessTime,
                contentDescription = "Расписание и напоминание",
                isActive = reminderEnabled || selectedDays.size < 7,
                onClick = { showScheduleAndReminderSheet = true }
            )
            EditorToolButton(
                icon = Icons.Outlined.Palette,
                contentDescription = "Цвет карточки",
                onClick = { showPaletteSheet = true }
            )
        }
    }

    if (showScheduleAndReminderSheet) {
        HabitScheduleAndReminderSheet(
            selectedDays = selectedDays,
            reminderEnabled = reminderEnabled,
            reminderTime = reminderTime,
            onUpdateDays = { selectedDays = it },
            onUpdateReminder = { enabled, time ->
                reminderEnabled = enabled
                reminderTime = time
            },
            onDismiss = { showScheduleAndReminderSheet = false }
        )
    }

    if (showPaletteSheet) {
        ColorPaletteSheet(
            title = "Цвет привычки",
            options = colorOptions,
            selected = selectedColor,
            onSelect = { selectedColor = it },
            onDismiss = { showPaletteSheet = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun HabitScheduleAndReminderSheet(
    selectedDays: Set<Int>,
    reminderEnabled: Boolean,
    reminderTime: String,
    onUpdateDays: (Set<Int>) -> Unit,
    onUpdateReminder: (Boolean, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var enabled by remember(reminderEnabled) { mutableStateOf(reminderEnabled) }
    var time by remember(reminderTime) { mutableStateOf(reminderTime) }
    val timeValid = time.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d"))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.Background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Расписание и напоминание", style = MaterialTheme.typography.headlineMedium, color = AppColors.Ink)

            Text("Дни повторения", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterPill("Каждый день", selectedDays == (1..7).toSet(), { onUpdateDays((1..7).toSet()) })
                FilterPill("Будни", selectedDays == (1..5).toSet(), { onUpdateDays((1..5).toSet()) })
                FilterPill("Выходные", selectedDays == setOf(6, 7), { onUpdateDays(setOf(6, 7)) })
                FilterPill("Пн · Ср · Пт", selectedDays == setOf(1, 3, 5), { onUpdateDays(setOf(1, 3, 5)) })
            }

            HorizontalDivider(color = AppColors.Outline.copy(alpha = 0.55f))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Напоминание", style = MaterialTheme.typography.titleMedium, color = AppColors.Ink)
                    Text("Уведомление в выбранные дни", color = AppColors.Muted, fontSize = 12.sp)
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        enabled = it
                        onUpdateReminder(it, if (timeValid) time else "09:00")
                    }
                )
            }

            if (enabled) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "08:00" to "Утро · 08:00",
                        "13:00" to "День · 13:00",
                        "19:00" to "Вечер · 19:00",
                        "21:30" to "21:30"
                    ).forEach { (presetTime, label) ->
                        FilterPill(
                            label = label,
                            selected = time == presetTime,
                            onClick = {
                                time = presetTime
                                onUpdateReminder(true, presetTime)
                            }
                        )
                    }
                }

                OutlinedTextField(
                    value = time,
                    onValueChange = {
                        time = it.filter { ch -> ch.isDigit() || ch == ':' }.take(5)
                        if (time.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d"))) {
                            onUpdateReminder(true, time)
                        }
                    },
                    label = { Text("Время (ЧЧ:ММ)") },
                    singleLine = true,
                    isError = !timeValid,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppColors.White,
                        unfocusedContainerColor = AppColors.White,
                        focusedBorderColor = AppColors.Ink,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = onDismiss,
                enabled = !enabled || timeValid,
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Ink, contentColor = AppColors.White),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("Готово", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}