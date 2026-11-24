package com.example.lifedashboard.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifedashboard.data.model.DashboardSettings
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.components.ScreenTopBar
import com.example.lifedashboard.ui.theme.AppColors

@Composable
fun ProfileScreen(
    settings: DashboardSettings,
    projectCount: Int,
    noteCount: Int,
    bestHabitStreak: Int,
    onBack: () -> Unit,
    onSaveProfile: (String, String, Int) -> Unit
) {
    var editing by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable(settings.profileName) { mutableStateOf(settings.profileName) }
    var email by rememberSaveable(settings.profileEmail) { mutableStateOf(settings.profileEmail) }
    var dailyGoal by rememberSaveable(settings.dailyTaskGoal) { mutableIntStateOf(settings.dailyTaskGoal) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenTopBar(
            title = "Профиль",
            actionIcon = if (editing) Icons.Outlined.Check else Icons.Outlined.Edit,
            actionDescription = if (editing) "Сохранить" else "Редактировать",
            onActionClick = {
                if (editing) {
                    onSaveProfile(name, email, dailyGoal)
                }
                editing = !editing
            },
            onBackClick = {
                if (editing) onSaveProfile(name, email, dailyGoal)
                onBack()
            }
        )

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(AppColors.Lavender),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.PersonOutline, contentDescription = null, tint = AppColors.Ink, modifier = Modifier.size(36.dp))
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                editing = true
            },
            enabled = true,
            singleLine = true,
            label = { Text("Имя") },
            placeholder = { Text("Ваше имя") },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AppColors.White,
                unfocusedContainerColor = AppColors.White,
                focusedBorderColor = AppColors.Ink,
                unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                editing = true
            },
            enabled = true,
            singleLine = true,
            label = { Text("Почта") },
            placeholder = { Text("name@example.com") },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AppColors.White,
                unfocusedContainerColor = AppColors.White,
                focusedBorderColor = AppColors.Ink,
                unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileStatCard(value = projectCount.toString(), label = "проекта", modifier = Modifier.weight(1f))
            ProfileStatCard(value = noteCount.toString(), label = "заметки", modifier = Modifier.weight(1f))
            ProfileStatCard(value = bestHabitStreak.toString(), label = "дней подряд", modifier = Modifier.weight(1f))
        }

        PastelCard(
            color = AppColors.White,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Дневная цель", color = AppColors.Ink, style = MaterialTheme.typography.titleMedium)
                    Text("Задач в день", color = AppColors.Muted, fontSize = 12.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AppColors.Background)
                            .clickable(enabled = dailyGoal > 1) {
                                dailyGoal = (dailyGoal - 1).coerceAtLeast(1)
                                onSaveProfile(name, email, dailyGoal)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Remove, contentDescription = "Уменьшить", tint = AppColors.Ink, modifier = Modifier.size(18.dp))
                    }
                    Text(dailyGoal.toString(), color = AppColors.Ink, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AppColors.Ink)
                            .clickable(enabled = dailyGoal < 50) {
                                dailyGoal = (dailyGoal + 1).coerceAtMost(50)
                                onSaveProfile(name, email, dailyGoal)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Add, contentDescription = "Увеличить", tint = AppColors.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatCard(value: String, label: String, modifier: Modifier = Modifier) {
    PastelCard(
        modifier = modifier,
        color = AppColors.White,
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, color = AppColors.Ink, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = AppColors.Muted, fontSize = 12.sp, maxLines = 1)
    }
}