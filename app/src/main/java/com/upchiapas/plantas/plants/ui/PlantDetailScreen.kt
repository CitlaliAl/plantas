package com.upchiapas.plantas.plants.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.upchiapas.plantas.plants.domain.model.Plant
import com.upchiapas.plantas.ui.components.ErrorState
import com.upchiapas.plantas.ui.components.LoadingBox

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.plant?.name ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        val modifier = Modifier.padding(padding)
        val plant = state.plant
        when {
            state.isLoading -> LoadingBox(modifier)
            plant == null -> ErrorState(state.error ?: "No se encontró la planta", onRetry = viewModel::load, modifier = modifier)
            else -> Column(
                modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailContent(plant)
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { onEdit(plant.id) }, modifier = Modifier.weight(1f)) { Text("Editar") }
                    Button(
                        onClick = viewModel::askDelete,
                        enabled = !state.isDeleting,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) { Text("Eliminar") }
                }
            }
        }
    }
}

@Composable
private fun DetailContent(plant: Plant) {
    val status = plant.wateringStatus()
    Text(
        text = status.text,
        style = MaterialTheme.typography.titleLarge,
        color = if (status.isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    )
    DetailRow("Especie", plant.species)
    DetailRow("Riego cada", "${plant.wateringIntervalDays} días")
    DetailRow("Fecha de siembra", plant.plantingDate.display())
    DetailRow("Último riego", plant.lastWateredDate.display())
    DetailRow("Necesita sol", if (plant.needsSun) "Sí" else "No")
    DetailRow("Próximo riego", plant.nextWateringDate().display())
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
