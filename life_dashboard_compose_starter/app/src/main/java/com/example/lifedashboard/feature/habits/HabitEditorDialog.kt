package com.example.lifedashboard.feature.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import com.example.lifedashboard.data.model.DashboardHabit
import com.example.lifedashboard.data.model.HabitColor
import com.example.lifedashboard.ui.theme.AppColors

private val weekdayOptions = listOf(
    1 to "Пн", 2 to "Вт", 3 to "Ср", 4 to "Чт", 5 to "Пт", 6 to "Сб", 7 to "Вс"
)

@Composable
fun HabitEditorDialog(
    initialHabit: DashboardHabit? = null,
    operationError: String? = null,
    onDismiss: () -> Unit,
    onSave: (DashboardHabit) -> Unit
) {
    var title by rememberSaveable(initialHabit?.id) { mutableStateOf(initialHabit?.title.orEmpty()) }
    var selectedColor by rememberSaveable(initialHabit?.id) { mutableStateOf(initialHabit?.color ?: HabitColor.PINK) }
    var reminderEnabled by rememberSaveable(initialHabit?.id) { mutableStateOf(initialHabit?.reminderEnabled ?: false) }
    var reminderTime by rememberSaveable(initialHabit?.id) { mutableStateOf(initialHabit?.reminderTime ?: "09:00") }
    var selectedDays by remember(initialHabit?.id) {
        mutableStateOf(initialHabit?.reminderDays?.ifEmpty { (1..7).toSet() } ?: (1..7).toSet())
    }
    val timeIsValid = reminderTime.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d"))
    val canSave = title.isNotBlank() && (!reminderEnabled || (timeIsValid && selectedDays.isNotEmpty()))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialHabit == null) "Новая привычка" else "Редактировать привычку",
                color = AppColors.Ink,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (operationError != null) {
                    Text(operationError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Например, прогулка 30 минут") },
                    shape = RoundedCornerShape(18.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = AppColors.Background,
                        unfocusedContainerColor = AppColors.Background,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Text("Цвет карточки", color = AppColors.Muted, style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HabitColor.values().forEach { color ->
                        val selected = color == selectedColor
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(habitColor(color))
                                .then(if (selected) Modifier.border(2.dp, AppColors.Ink, CircleShape) else Modifier)
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "Выбранный цвет",
                                    tint = AppColors.Ink,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Напоминание", color = AppColors.Ink, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (reminderEnabled) "Сохранить расписание для уведомлений" else "Отключено",
                            color = AppColors.Muted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }

                if (reminderEnabled) {
                    Text("Время", color = AppColors.Muted, style = MaterialTheme.typography.labelLarge)
                    TextField(
                        value = reminderTime,
                        onValueChange = { reminderTime = it.filter { char -> char.isDigit() || char == ':' }.take(5) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("09:00") },
                        isError = !timeIsValid,
                        supportingText = {
                            if (!timeIsValid) Text("Используй формат 24 часа, например 08:30")
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = AppColors.Background,
                            unfocusedContainerColor = AppColors.Background,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Text("Дни недели", color = AppColors.Muted, style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        weekdayOptions.forEach { (day, label) ->
                            val selected = day in selectedDays
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (selected) AppColors.Ink else AppColors.Background)
                                    .clickable {
                                        selectedDays = if (selected) selectedDays - day else selectedDays + day
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    color = if (selected) AppColors.White else AppColors.Muted,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (selectedDays.isEmpty()) {
                        Text("Выбери хотя бы один день", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        "Android может запросить разрешение на уведомления и точные будильники. Без точного доступа уведомление может прийти с задержкой.",
                        color = AppColors.Muted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    val reminderLabel = if (reminderEnabled) "${selectedDays.size} дн. · $reminderTime" else "Не настроено"
                    val habit = (initialHabit ?: DashboardHabit(id = "", title = title, color = selectedColor)).copy(
                        title = title.trim(),
                        color = selectedColor,
                        reminderEnabled = reminderEnabled,
                        reminderTime = if (timeIsValid) reminderTime else "09:00",
                        reminderDays = if (selectedDays.isEmpty()) (1..7).toSet() else selectedDays,
                        reminderLabel = reminderLabel
                    )
                    onSave(habit)
                }
            ) {
                Text(if (initialHabit == null) "Создать" else "Сохранить", color = AppColors.Ink, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = AppColors.Muted) }
        },
        containerColor = AppColors.White
    )
}
