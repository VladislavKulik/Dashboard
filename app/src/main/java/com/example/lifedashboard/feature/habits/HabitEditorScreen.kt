package com.example.lifedashboard.feature.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.example.lifedashboard.ui.components.EditorBottomBar
import com.example.lifedashboard.ui.components.FilterPill
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.theme.AppColors

private enum class HabitFrequencyMode(val label: String) {
    DAILY("Каждый день"),
    BY_DAYS("По дням"),
    ONCE_A_WEEK("Раз в неделю")
}

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
        mutableStateOf(initialHabit?.reminderDays?.ifEmpty { (1..7).toSet() } ?: setOf(1, 3, 5))
    }
    var frequencyMode by remember(initialHabit?.id) {
        val count = initialHabit?.reminderDays?.size ?: 3
        mutableStateOf(
            when {
                count >= 7 -> HabitFrequencyMode.DAILY
                count == 1 -> HabitFrequencyMode.ONCE_A_WEEK
                else -> HabitFrequencyMode.BY_DAYS
            }
        )
    }
    var attemptedSubmit by rememberSaveable { mutableStateOf(false) }

    val timeIsValid = reminderTime.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d"))
    val effectiveDays = if (frequencyMode == HabitFrequencyMode.DAILY) (1..7).toSet() else selectedDays
    val canSave = title.isNotBlank() && effectiveDays.isNotEmpty() && (!reminderEnabled || timeIsValid)

    fun submit() {
        attemptedSubmit = true
        if (!canSave) return
        val reminderLabel = if (reminderEnabled) "${effectiveDays.size} дн. · $reminderTime" else "Не настроено"
        val habit = (initialHabit ?: DashboardHabit(id = "", title = title, color = selectedColor)).copy(
            title = title.trim(),
            color = selectedColor,
            reminderEnabled = reminderEnabled,
            reminderTime = if (timeIsValid) reminderTime else "09:00",
            reminderDays = effectiveDays,
            reminderLabel = reminderLabel
        )
        onSave(habit, onBack)
    }

    val weekdayShorts = listOf(1 to "П", 2 to "В", 3 to "С", 4 to "Ч", 5 to "П", 6 to "С", 7 to "В")

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
                    label = { Text("Название") },
                    placeholder = { Text("Например, прогулка 30 минут") },
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
                    Text("Частота", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HabitFrequencyMode.values().forEach { mode ->
                            FilterPill(
                                label = mode.label,
                                selected = frequencyMode == mode,
                                onClick = {
                                    frequencyMode = mode
                                    when (mode) {
                                        HabitFrequencyMode.DAILY -> selectedDays = (1..7).toSet()
                                        HabitFrequencyMode.ONCE_A_WEEK -> selectedDays = setOf(selectedDays.firstOrNull() ?: 1)
                                        HabitFrequencyMode.BY_DAYS -> if (selectedDays.size == 7) selectedDays = setOf(1, 3, 5)
                                    }
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekdayShorts.forEach { (day, label) ->
                            val selected = day in effectiveDays
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) AppColors.Ink else AppColors.White)
                                    .clickable {
                                        frequencyMode = HabitFrequencyMode.BY_DAYS
                                        selectedDays = if (selected) selectedDays - day else selectedDays + day
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (selected) AppColors.White else AppColors.Ink,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (effectiveDays.isEmpty()) {
                        Text("Выбери хотя бы один день", color = AppColors.Error, fontSize = 12.sp)
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Цвет карточки", color = AppColors.Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        HabitColor.values().forEach { color ->
                            val selected = color == selectedColor
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(habitColor(color))
                                    .then(if (selected) Modifier.border(2.dp, AppColors.Ink, CircleShape) else Modifier)
                                    .clickable { selectedColor = color },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Icon(Icons.Outlined.Check, contentDescription = "Выбрано", tint = AppColors.Ink, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                PastelCard(
                    color = AppColors.White,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Напоминание", color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)
                            Text(if (reminderEnabled) reminderTime else "Отключено", color = AppColors.Muted, fontSize = 12.sp)
                        }
                        Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                    }
                    if (reminderEnabled) {
                        OutlinedTextField(
                            value = reminderTime,
                            onValueChange = { reminderTime = it.filter { c -> c.isDigit() || c == ':' }.take(5) },
                            label = { Text("Время (ЧЧ:ММ)") },
                            singleLine = true,
                            isError = !timeIsValid,
                            supportingText = if (!timeIsValid) {
                                { Text("Формат ЧЧ:ММ, например 09:00", color = AppColors.Error) }
                            } else null,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
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
            IconButton(onClick = { reminderEnabled = !reminderEnabled }) {
                Icon(
                    Icons.Outlined.AccessTime,
                    contentDescription = "Напоминание",
                    tint = if (reminderEnabled) AppColors.Purple else AppColors.Ink
                )
            }
            IconButton(onClick = {
                frequencyMode = HabitFrequencyMode.DAILY
                selectedDays = (1..7).toSet()
            }) {
                Icon(Icons.Outlined.Repeat, contentDescription = "Каждый день", tint = AppColors.Ink)
            }
        }
    }
}