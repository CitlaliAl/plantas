package com.upchiapas.plantas.auth.domain.repository

import com.upchiapas.plantas.auth.domain.model.User

/** Contrato de autenticación. Guarda la sesión al iniciar y la borra al salir. */
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(name: String, email: String, password: String): Result<User>

    /** Revoca el token en el servidor (si hay red) y siempre borra la sesión local. */
    suspend fun logout()

    fun isLoggedIn(): Boolean

    /** Borra la sesión local sin llamar al servidor (por ejemplo, tras un 401). */
    fun clearSession()
}
