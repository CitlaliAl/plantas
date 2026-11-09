package com.upchiapas.plantas.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.plantas.auth.domain.repository.AuthRepository
import com.upchiapas.plantas.core.network.ApiException
import com.upchiapas.plantas.core.validation.AuthValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errors: Map<String, String> = emptyMap(),
    val generalError: String? = null,
    val registered: Boolean = false
)

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) =
        _uiState.update { it.copy(name = value, errors = it.errors - "name", generalError = null) }

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, errors = it.errors - "email", generalError = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, errors = it.errors - "password", generalError = null) }

    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, errors = it.errors - "confirmPassword", generalError = null) }

    fun register() {
        val current = _uiState.value
        if (current.isLoading) return
        val errors = AuthValidator.validateRegister(
            current.name, current.email, current.password, current.confirmPassword
        )
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        _uiState.update { it.copy(isLoading = true, generalError = null) }
        viewModelScope.launch {
            authRepository.register(current.name.trim(), current.email.trim(), current.password).fold(
                onSuccess = { _uiState.update { s -> s.copy(isLoading = false, registered = true) } },
                onFailure = { e ->
                    val api = e as? ApiException
                    _uiState.update { s ->
                        s.copy(
                            isLoading = false,
                            errors = if (api?.code == 409) mapOf("email" to e.message.orEmpty()) else api?.fieldErrors.orEmpty(),
                            generalError = e.message
                        )
                    }
                }
            )
        }
    }
}
