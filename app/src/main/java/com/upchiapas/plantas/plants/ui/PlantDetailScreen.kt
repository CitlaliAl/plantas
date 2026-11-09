package com.upchiapas.plantas.plants.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.upchiapas.plantas.plants.domain.model.Plant
import com.upchiapas.plantas.ui.components.ErrorState
import com.upchiapas.plantas.ui.components.HeroHeader
import com.upchiapas.plantas.ui.components.LoadingBox
import com.upchiapas.plantas.ui.components.PlantIllustration
import com.upchiapas.plantas.ui.components.plantKindOf

@Composable
fun PlantDetailScreen(
    viewModel: PlantDetailViewModel,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onSessionExpired: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.deleted) { if (state.deleted) onBack() }
    LaunchedEffect(state.unauthorized) { if (state.unauthorized) onSessionExpired() }

    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDelete,
            title = { Text("¿Eliminar ${state.plant?.name.orEmpty()}?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = viewModel::dismissDelete) { Text("Cancelar") } }
        )
    }

    Scaffold { padding ->
        val plant = state.plant
        Column(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            HeroHeader {
                Row(modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                if (plant != null) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        PlantIllustration(plantKindOf(plant.name, plant.species), Modifier.size(120.dp))
                    }
                    Text(
                        plant.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        plant.species,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            when {
                state.isLoading -> LoadingBox(Modifier.weight(1f))
                plant == null -> ErrorState(
                    state.error ?: "No se encontró la planta",
                    onRetry = viewModel::load,
                    modifier = Modifier.weight(1f)
                )
                else -> Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatusCard(plant)

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        InfoTile(Icons.Filled.WaterDrop, "Riego cada", "${plant.wateringIntervalDays} días", Modifier.weight(1f))
                        InfoTile(Icons.Filled.WbSunny, "Sol", if (plant.needsSun) "Sí" else "No", Modifier.weight(1f))
                        InfoTile(Icons.Filled.Event, "Siembra", plant.plantingDate.display(), Modifier.weight(1f))
                    }

                    state.message?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                    state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                    Button(
                        onClick = viewModel::waterToday,
                        enabled = !state.isWatering,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.WaterDrop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(if (state.isWatering) "  Regando..." else "  Regar hoy")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { onEdit(plant.id) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("  Editar")
                        }
                        OutlinedButton(
                            onClick = viewModel::askDelete,
                            enabled = !state.isDeleting,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("  Eliminar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(plant: Plant) {
    val status = plant.wateringStatus()
    val accent = if (status.isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (status.isUrgent) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(52.dp).clip(CircleShape).background(accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.surface)
            }
            Column {
                Text(status.text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accent)
                Text("Próximo riego: ${plant.nextWateringDate().display()}", style = MaterialTheme.typography.bodyMedium)
                Text("Último riego: ${plant.lastWateredDate.display()}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun InfoTile(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}
