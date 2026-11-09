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
fun LoginScreen(
    viewModel: LoginViewModel,
    sessionExpired: Boolean,
    onLoggedIn: () -> Unit,
    onGoToRegister: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) onLoggedIn()
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
            Text("Cuidado de Plantas", style = MaterialTheme.typography.headlineMedium)
            Text("Inicia sesión", style = MaterialTheme.typography.titleMedium)

            if (sessionExpired) {
                Text("Tu sesión expiró. Vuelve a iniciar sesión.", color = MaterialTheme.colorScheme.tertiary)
            }

            FormField(
                label = "Correo",
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                error = state.errors["email"],
                keyboardType = KeyboardType.Email
            )
            FormField(
                label = "Contraseña",
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                error = state.errors["password"],
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            state.generalError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            LoadingButton(text = "Entrar", isLoading = state.isLoading, onClick = viewModel::login)
            TextButton(onClick = onGoToRegister) { Text("¿No tienes cuenta? Regístrate") }
        }
    }
}
