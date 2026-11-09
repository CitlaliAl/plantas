package com.upchiapas.plantas.plants.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.plantas.core.network.ApiException
import com.upchiapas.plantas.plants.domain.model.Plant
import com.upchiapas.plantas.plants.domain.repository.PlantsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PlantDetailUiState(
    val isLoading: Boolean = true,
    val plant: Plant? = null,
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val deleted: Boolean = false,
    val isWatering: Boolean = false,
    val message: String? = null,
    val unauthorized: Boolean = false
)

class PlantDetailViewModel(
    private val plantId: Int,
    private val plantsRepository: PlantsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlantDetailUiState())
    val uiState: StateFlow<PlantDetailUiState> = _uiState.asStateFlow()

    /** Se llama al entrar, para refrescar después de editar. */
    fun load() {
        _uiState.update { it.copy(isLoading = it.plant == null, error = null) }
        viewModelScope.launch {
            plantsRepository.getPlant(plantId).fold(
                onSuccess = { plant -> _uiState.update { it.copy(isLoading = false, plant = plant) } },
                onFailure = { e -> _uiState.update { it.withError(e) } }
            )
        }
    }

    /** Marca la planta como regada hoy: actualiza lastWateredDate y recalcula el próximo riego. */
    fun waterToday() {
        val plant = _uiState.value.plant ?: return
        if (_uiState.value.isWatering) return
        _uiState.update { it.copy(isWatering = true, error = null, message = null) }
        viewModelScope.launch {
            plantsRepository.updatePlant(plant.copy(lastWateredDate = LocalDate.now())).fold(
                onSuccess = { updated ->
                    _uiState.update { it.copy(isWatering = false, plant = updated, message = "¡Planta regada hoy!") }
                },
                onFailure = { e -> _uiState.update { it.withError(e).copy(isWatering = false) } }
            )
        }
    }

    fun askDelete() = _uiState.update { it.copy(showDeleteDialog = true) }

    fun dismissDelete() = _uiState.update { it.copy(showDeleteDialog = false) }

    fun confirmDelete() {
        _uiState.update { it.copy(showDeleteDialog = false, isDeleting = true) }
        viewModelScope.launch {
            plantsRepository.deletePlant(plantId).fold(
                onSuccess = { _uiState.update { it.copy(isDeleting = false, deleted = true) } },
                onFailure = { e ->
                    val api = e as? ApiException
                    // Si ya no existía (404) se considera eliminada y se vuelve a la lista.
                    if (api?.isNotFound == true) _uiState.update { it.copy(isDeleting = false, deleted = true) }
                    else _uiState.update { it.withError(e).copy(isDeleting = false) }
                }
            )
        }
    }

    private fun PlantDetailUiState.withError(e: Throwable) = copy(
        isLoading = false,
        error = e.message,
        unauthorized = (e as? ApiException)?.isUnauthorized == true
    )
}
