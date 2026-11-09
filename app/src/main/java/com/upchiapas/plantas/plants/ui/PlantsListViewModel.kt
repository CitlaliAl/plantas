package com.upchiapas.plantas.plants.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.plantas.auth.domain.repository.AuthRepository
import com.upchiapas.plantas.core.network.ApiException
import com.upchiapas.plantas.plants.domain.model.Plant
import com.upchiapas.plantas.plants.domain.repository.PlantsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlantsListUiState(
    val isLoading: Boolean = true,
    val plants: List<Plant> = emptyList(),
    val error: String? = null,
    val unauthorized: Boolean = false,
    val loggedOut: Boolean = false
)

class PlantsListViewModel(
    private val plantsRepository: PlantsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlantsListUiState())
    val uiState: StateFlow<PlantsListUiState> = _uiState.asStateFlow()

    /** Se llama al entrar a la pantalla (también al volver del detalle o del formulario). */
    fun load() {
        _uiState.update { it.copy(isLoading = it.plants.isEmpty(), error = null) }
        viewModelScope.launch {
            plantsRepository.getPlants().fold(
                onSuccess = { list ->
                    _uiState.update { it.copy(isLoading = false, plants = list.sortedBy { p -> p.nextWateringDate() }) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message,
                            unauthorized = (e as? ApiException)?.isUnauthorized == true
                        )
                    }
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(loggedOut = true) }
        }
    }
}
