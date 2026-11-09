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

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errors: Map<String, String> = emptyMap(),
    val generalError: String? = null,
    val loggedIn: Boolean = false
)

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, errors = it.errors - "email", generalError = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, errors = it.errors - "password", generalError = null) }

    fun login() {
        val current = _uiState.value
        if (current.isLoading) return
        val errors = AuthValidator.validateLogin(current.email, current.password)
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(errors = errors) }
            return
        }
        _uiState.update { it.copy(isLoading = true, generalError = null) }
        viewModelScope.launch {
            authRepository.login(current.email.trim(), current.password).fold(
                onSuccess = { _uiState.update { s -> s.copy(isLoading = false, loggedIn = true) } },
                onFailure = { e ->
                    _uiState.update { s ->
                        s.copy(
                            isLoading = false,
                            errors = (e as? ApiException)?.fieldErrors.orEmpty(),
                            generalError = e.message
                        )
                    }
                }
            )
        }
    }
}
