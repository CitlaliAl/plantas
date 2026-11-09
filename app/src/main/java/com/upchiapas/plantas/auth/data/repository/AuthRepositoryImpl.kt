package com.upchiapas.plantas.auth.data.repository

import com.upchiapas.plantas.auth.data.mapper.toDomain
import com.upchiapas.plantas.auth.data.remote.AuthApiService
import com.upchiapas.plantas.auth.data.remote.dto.AuthResponseDto
import com.upchiapas.plantas.auth.data.remote.dto.LoginRequestDto
import com.upchiapas.plantas.auth.data.remote.dto.RegisterRequestDto
import com.upchiapas.plantas.auth.domain.model.User
import com.upchiapas.plantas.auth.domain.repository.AuthRepository
import com.upchiapas.plantas.core.network.safeApiCall
import com.upchiapas.plantas.core.session.SessionStorage
import kotlinx.serialization.json.Json

class AuthRepositoryImpl(
    private val api: AuthApiService,
    private val session: SessionStorage,
    private val json: Json
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<User> =
        safeApiCall(json) { api.login(LoginRequestDto(email, password)).saveSession() }

    override suspend fun register(name: String, email: String, password: String): Result<User> =
        safeApiCall(json) { api.register(RegisterRequestDto(name, email, password)).saveSession() }

    override suspend fun logout() {
        // Si no hay red igual se cierra la sesión local; el token vence solo en 24 h.
        runCatching { api.logout() }
        session.clear()
    }

    override fun isLoggedIn(): Boolean = !session.getToken().isNullOrBlank()

    override fun clearSession() = session.clear()

    private fun AuthResponseDto.saveSession(): User {
        session.saveToken(accessToken)
        return user.toDomain()
    }
}
