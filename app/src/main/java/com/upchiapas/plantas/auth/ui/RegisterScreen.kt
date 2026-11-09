package com.upchiapas.plantas.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.upchiapas.plantas.ui.components.FormField
import com.upchiapas.plantas.ui.components.HeroHeader
import com.upchiapas.plantas.ui.components.LoadingButton
import com.upchiapas.plantas.ui.components.PlantIllustration
import com.upchiapas.plantas.ui.components.PlantKind

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
    ) {
        HeroHeader {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                PlantIllustration(PlantKind.LEAFY, Modifier.size(74.dp))
            }
            Text(
                "Crea tu cuenta",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            )
        }

        Column(
            modifier = Modifier.padding(24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                }
            }

            TextButton(onClick = onBackToLogin) { Text("¿Ya tienes cuenta? Inicia sesión") }
        }
    }
}
