package com.example.lifedashboard.feature.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lifedashboard.ui.components.CircleIconButton
import com.example.lifedashboard.ui.components.PastelCard
import com.example.lifedashboard.ui.theme.AppColors

@Composable
fun PlaceholderScreen(
    title: String,
    description: String,
    onBack: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            CircleIconButton(
                icon = Icons.Outlined.ArrowBack,
                contentDescription = "Назад",
                onClick = onBack
            )
        }
        item {
            Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.headlineLarge,
                color = AppColors.Ink
            )
        }
        item {
            PastelCard(color = AppColors.White, modifier = Modifier.fillMaxWidth()) {
                Text(description, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge, color = AppColors.Muted)
                Text(
                    "Этот экран будет детально реализован на следующем этапе.",
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = AppColors.Ink
                )
            }
        }
    }
}
