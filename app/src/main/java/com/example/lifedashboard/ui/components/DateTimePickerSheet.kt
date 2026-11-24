package com.example.lifedashboard.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.ui.theme.AppColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerSheet(
    initialDate: LocalDate?,
    initialTime: String = "14:00",
    showTimeField: Boolean = false,
    allowClear: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate?, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val today = remember { LocalDate.now() }
    var selectedDate by remember(initialDate) { mutableStateOf(initialDate) }
    var time by remember(initialTime) { mutableStateOf(initialTime) }
    var monthOffset by remember(initialDate) {
        val base = initialDate ?: today
        val diff = (base.year - today.year) * 12 + (base.monthValue - today.monthValue)
        mutableIntStateOf(diff)
    }

    val displayedMonth = remember(monthOffset) { YearMonth.now().plusMonths(monthOffset.toLong()) }
    val monthTitle = remember(displayedMonth) {
        displayedMonth.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("ru")))
            .replaceFirstChar { it.uppercase(Locale.forLanguageTag("ru")) }
    }
    val cells = remember(displayedMonth) {
        val offset = displayedMonth.atDay(1).dayOfWeek.value - 1
        buildList<LocalDate?> {
            repeat(offset) { add(null) }
            for (d in 1..displayedMonth.lengthOfMonth()) add(displayedMonth.atDay(d))
            while (size % 7 != 0) add(null)
        }
    }
    val timeValid = !showTimeField || time.matches(Regex("(?:[01]\\d|2[0-3]):[0-5]\\d"))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Срок", style = MaterialTheme.typography.headlineMedium, color = AppColors.Ink)

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AppColors.Background)
                        .clickable { monthOffset -= 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.ChevronLeft, contentDescription = "Предыдущий месяц", tint = AppColors.Ink, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.Ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(AppColors.Background)
                        .clickable { monthOffset += 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.ChevronRight, contentDescription = "Следующий месяц", tint = AppColors.Ink, modifier = Modifier.size(18.dp))
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                    Text(day, modifier = Modifier.weight(1f), color = AppColors.Muted, fontSize = 11.sp, textAlign = TextAlign.Center)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                cells.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        week.forEach { date ->
                            Box(
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (date != null) {
                                    val isSelected = date == selectedDate
                                    val isToday = date == today
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .then(
                                                when {
                                                    isSelected -> Modifier.background(AppColors.Ink)
                                                    isToday -> Modifier.border(1.5.dp, AppColors.Ink, CircleShape)
                                                    else -> Modifier
                                                }
                                            )
                                            .clickable { selectedDate = date },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = date.dayOfMonth.toString(),
                                            color = if (isSelected) AppColors.White else AppColors.Ink,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickDateChip("Сегодня", selectedDate == today) {
                    selectedDate = today
                    monthOffset = 0
                }
                QuickDateChip("Завтра", selectedDate == today.plusDays(1)) {
                    val d = today.plusDays(1)
                    selectedDate = d
                    monthOffset = (d.year - today.year) * 12 + (d.monthValue - today.monthValue)
                }
                QuickDateChip("Через неделю", selectedDate == today.plusWeeks(1)) {
                    val d = today.plusWeeks(1)
                    selectedDate = d
                    monthOffset = (d.year - today.year) * 12 + (d.monthValue - today.monthValue)
                }
                if (allowClear && selectedDate != null) {
                    QuickDateChip("Без срока", false) {
                        selectedDate = null
                    }
                }
            }

            if (showTimeField) {
                OutlinedTextField(
                    value = time,
                    onValueChange = { value ->
                        time = value.filter { it.isDigit() || it == ':' }.take(5)
                    },
                    label = { Text("Время") },
                    trailingIcon = {
                        Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = AppColors.Ink)
                    },
                    isError = !timeValid,
                    supportingText = if (!timeValid) {
                        { Text("Формат ЧЧ:ММ, например 14:00", color = AppColors.Error) }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Ink,
                        unfocusedBorderColor = AppColors.Outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = { onConfirm(selectedDate, time) },
                enabled = timeValid && (allowClear || selectedDate != null),
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Ink, contentColor = AppColors.White),
                contentPadding = PaddingValues(vertical = 15.dp)
            ) {
                Text("Готово", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun QuickDateChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) AppColors.Ink else AppColors.Background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            text = label,
            color = if (selected) AppColors.White else AppColors.Ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}