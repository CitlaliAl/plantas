package com.upchiapas.plantas.plants.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.plantas.core.network.ApiException
import com.upchiapas.plantas.plants.domain.model.Plant
import com.upchiapas.plantas.plants.domain.repository.PlantsRepository
import com.upchiapas.plantas.plants.domain.validation.PlantFormInput
import com.upchiapas.plantas.plants.domain.validation.PlantFormValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class PlantFormUiState(
    val name: String = "",
    val species: String = "",
    val wateringIntervalDays: String = "",
    val plantingDate: String = LocalDate.now().toString(),
    val lastWateredDate: String = LocalDate.now().toString(),
    val needsSun: Boolean = false,
    val errors: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val generalError: String? = null,
    val saved: Boolean = false,
    val unauthorized: Boolean = false
)

/** plantId == null: crear. plantId != null: editar. */
class PlantFormViewModel(
    private val plantId: Int?,
    private val plantsRepository: PlantsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlantFormUiState(isLoading = plantId != null))
    val uiState: StateFlow<PlantFormUiState> = _uiState.asStateFlow()

    val isEditing: Boolean get() = plantId != null

    init {
        if (plantId != null) loadPlant(plantId)
    }

    private fun loadPlant(id: Int) {
        viewModelScope.launch {
            plantsRepository.getPlant(id).fold(
                onSuccess = { p ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = p.name,
                            species = p.species,
                            wateringIntervalDays = p.wateringIntervalDays.toString(),
                            plantingDate = p.plantingDate.toString(),
                            lastWateredDate = p.lastWateredDate.toString(),
                            needsSun = p.needsSun
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = e.message,
                            unauthorized = (e as? ApiException)?.isUnauthorized == true
                        )
                    }
                }
            )
        }
    }

    fun onNameChange(v: String) = edit("name") { it.copy(name = v) }
    fun onSpeciesChange(v: String) = edit("species") { it.copy(species = v) }
    fun onIntervalChange(v: String) = edit("wateringIntervalDays") { it.copy(wateringIntervalDays = v.filter(Char::isDigit)) }
    fun onPlantingDateChange(v: String) = edit("plantingDate") { it.copy(plantingDate = v) }
    fun onLastWateredChange(v: String) = edit("lastWateredDate") { it.copy(lastWateredDate = v) }
    fun onNeedsSunChange(v: Boolean) = edit("needsSun") { it.copy(needsSun = v) }

    private fun edit(field: String, change: (PlantFormUiState) -> PlantFormUiState) =
        _uiState.update { change(it).copy(errors = it.errors - field, generalError = null) }

    fun save() {
        val s = _uiState.value
        if (s.isSaving || s.isLoading) return
        val errors = PlantFormValidator.validate(
            PlantFormInput(s.name, s.species, s.wateringIntervalDays, s.plantingDate, s.lastWateredDate)
        )
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        val plant = Plant(
            id = plantId ?: 0,
            name = s.name.trim(),
            species = s.species.trim(),
            wateringIntervalDays = s.wateringIntervalDays.trim().toInt(),
            plantingDate = LocalDate.parse(s.plantingDate.trim()),
            lastWateredDate = LocalDate.parse(s.lastWateredDate.trim()),
            needsSun = s.needsSun
        )
        _uiState.update { it.copy(isSaving = true, generalError = null) }
        viewModelScope.launch {
            val result = if (plantId == null) plantsRepository.createPlant(plant) else plantsRepository.updatePlant(plant)
            result.fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, saved = true) } },
                onFailure = { e ->
                    val api = e as? ApiException
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errors = api?.fieldErrors.orEmpty(),
                            generalError = e.message,
                            unauthorized = api?.isUnauthorized == true
                        )
                    }
                }
            )
        }
    }
}
