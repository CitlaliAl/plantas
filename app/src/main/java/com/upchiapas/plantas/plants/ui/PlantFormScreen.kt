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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.upchiapas.plantas.ui.components.FormField
import com.upchiapas.plantas.ui.components.LoadingBox
import com.upchiapas.plantas.ui.components.LoadingButton
import com.upchiapas.plantas.ui.components.plantsTopBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantFormScreen(
    viewModel: PlantFormViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onSessionExpired: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) { if (state.saved) onSaved() }
    LaunchedEffect(state.unauthorized) { if (state.unauthorized) onSessionExpired() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Editar planta" else "Nueva planta", fontWeight = FontWeight.Bold) },
                colors = plantsTopBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingBox(Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FormField("Nombre", state.name, viewModel::onNameChange, error = state.errors["name"])
                FormField("Especie", state.species, viewModel::onSpeciesChange, error = state.errors["species"])
                FormField(
                    "Días entre riegos", state.wateringIntervalDays, viewModel::onIntervalChange,
                    error = state.errors["wateringIntervalDays"], keyboardType = KeyboardType.Number
                )
                FormField(
                    "Fecha de siembra", state.plantingDate, viewModel::onPlantingDateChange,
                    error = state.errors["plantingDate"], placeholder = "aaaa-mm-dd"
                )
                FormField(
                    "Último riego", state.lastWateredDate, viewModel::onLastWateredChange,
                    error = state.errors["lastWateredDate"], placeholder = "aaaa-mm-dd"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Necesita sol")
                    Switch(checked = state.needsSun, onCheckedChange = viewModel::onNeedsSunChange)
                }

                state.generalError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                LoadingButton(text = "Guardar", isLoading = state.isSaving, onClick = viewModel::save)
            }
        }
    }
}
