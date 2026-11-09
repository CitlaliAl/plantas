package com.upchiapas.plantas.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.upchiapas.plantas.ui.components.FormField
import com.upchiapas.plantas.ui.components.LoadingButton

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegistered: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.registered) {
        if (state.registered) onRegistered()
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)

            FormField("Nombre", state.name, viewModel::onNameChange, error = state.errors["name"])
            FormField(
                "Correo", state.email, viewModel::onEmailChange,
                error = state.errors["email"], keyboardType = KeyboardType.Email
            )
            FormField(
                "Contraseña", state.password, viewModel::onPasswordChange,
                error = state.errors["password"], keyboardType = KeyboardType.Password, isPassword = true
            )
            FormField(
                "Confirmar contraseña", state.confirmPassword, viewModel::onConfirmPasswordChange,
                error = state.errors["confirmPassword"], keyboardType = KeyboardType.Password, isPassword = true
            )

            state.generalError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            LoadingButton(text = "Registrarme", isLoading = state.isLoading, onClick = viewModel::register)
            TextButton(onClick = onBackToLogin) { Text("¿Ya tienes cuenta? Inicia sesión") }
        }
    }
}
