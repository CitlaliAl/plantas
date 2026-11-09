package com.upchiapas.plantas.plants.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.upchiapas.plantas.plants.domain.model.Plant
import com.upchiapas.plantas.ui.components.ErrorState
import com.upchiapas.plantas.ui.components.LoadingBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantsListScreen(
    viewModel: PlantsListViewModel,
    onPlantClick: (Int) -> Unit,
    onAddPlant: () -> Unit,
    onLoggedOut: () -> Unit,
    onSessionExpired: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state.loggedOut) { if (state.loggedOut) onLoggedOut() }
    LaunchedEffect(state.unauthorized) { if (state.unauthorized) onSessionExpired() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis plantas") },
                actions = {
                    IconButton(onClick = viewModel::logout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPlant) {
                Icon(Icons.Default.Add, contentDescription = "Agregar planta")
            }
        }
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when {
            state.isLoading -> LoadingBox(modifier)
            state.error != null && state.plants.isEmpty() ->
                ErrorState(state.error.orEmpty(), onRetry = viewModel::load, modifier = modifier)
            state.plants.isEmpty() -> Column(
                modifier = modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Aún no tienes plantas", style = MaterialTheme.typography.titleMedium)
                Text("Pulsa + para agregar la primera.")
            }
            else -> LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.plants, key = { it.id }) { plant ->
                    PlantCard(plant = plant, onClick = { onPlantClick(plant.id) })
                }
            }
        }
    }
}

@Composable
private fun PlantCard(plant: Plant, onClick: () -> Unit) {
    val status = plant.wateringStatus()
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(plant.name, style = MaterialTheme.typography.titleMedium)
            Text(plant.species, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = status.text,
                style = MaterialTheme.typography.bodyLarge,
                color = if (status.isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text("Próximo riego: ${plant.nextWateringDate().display()}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
