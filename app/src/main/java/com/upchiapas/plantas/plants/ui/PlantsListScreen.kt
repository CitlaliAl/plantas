package com.upchiapas.plantas.plants.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.upchiapas.plantas.plants.domain.model.Plant
import com.upchiapas.plantas.ui.components.ErrorState
import com.upchiapas.plantas.ui.components.HeroHeader
import com.upchiapas.plantas.ui.components.LoadingBox
import com.upchiapas.plantas.ui.components.PlantIllustration
import com.upchiapas.plantas.ui.components.PlantKind
import com.upchiapas.plantas.ui.components.plantKindOf

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

    val toWater = state.plants.count { it.needsWateringToday() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPlant,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar planta")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            HeroHeader {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Mis plantas",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = when {
                                state.plants.isEmpty() -> "Tu jardín está vacío"
                                toWater == 0 -> "${state.plants.size} plantas · todas al día"
                                toWater == 1 -> "${state.plants.size} plantas · 1 necesita riego hoy"
                                else -> "${state.plants.size} plantas · $toWater necesitan riego hoy"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = viewModel::logout) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            when {
                state.isLoading -> LoadingBox(Modifier.weight(1f))
                state.error != null && state.plants.isEmpty() ->
                    ErrorState(state.error.orEmpty(), onRetry = viewModel::load, modifier = Modifier.weight(1f))
                state.plants.isEmpty() -> Column(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PlantIllustration(PlantKind.SPROUT, Modifier.size(120.dp))
                    Text("Aún no tienes plantas", style = MaterialTheme.typography.titleMedium)
                    Text("Pulsa + para agregar la primera.")
                }
                else -> LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(state.plants, key = { it.id }) { plant ->
                        PlantCard(plant = plant, onClick = { onPlantClick(plant.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PlantCard(plant: Plant, onClick: () -> Unit) {
    val status = plant.wateringStatus()
    val accent = if (status.isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (status.isUrgent) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                PlantIllustration(plantKindOf(plant.name, plant.species), Modifier.size(66.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(plant.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    plant.species,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = status.text,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(accent.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                )
                Text("Próximo riego: ${plant.nextWateringDate().display()}", style = MaterialTheme.typography.bodySmall)
            }
            if (plant.needsSun) {
                Icon(
                    Icons.Filled.WbSunny,
                    contentDescription = "Necesita sol",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}
