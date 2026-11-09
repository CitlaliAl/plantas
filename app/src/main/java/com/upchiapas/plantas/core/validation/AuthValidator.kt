package com.upchiapas.plantas.core.validation

/** Validaciones de login y registro. Devuelven { campo -> mensaje }; vacío = todo correcto. */
object AuthValidator {

    private val EMAIL = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun validateLogin(email: String, password: String): Map<String, String> = buildMap {
        if (email.isBlank()) put("email", "Escribe tu correo")
        else if (!EMAIL.matches(email.trim())) put("email", "El correo no tiene un formato válido")
        if (password.isEmpty()) put("password", "Escribe tu contraseña")
    }

    fun validateRegister(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Map<String, String> = buildMap {
        if (name.isBlank() || name.trim().length > 60) put("name", "El nombre es obligatorio (máx. 60 caracteres)")
        if (!EMAIL.matches(email.trim())) put("email", "El correo no tiene un formato válido")
        if (password.length < 8) put("password", "La contraseña debe tener al menos 8 caracteres")
        if (confirmPassword != password) put("confirmPassword", "Las contraseñas no coinciden")
    }
}
