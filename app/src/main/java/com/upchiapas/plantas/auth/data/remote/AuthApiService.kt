package com.upchiapas.plantas.auth.data.remote

import com.upchiapas.plantas.auth.data.remote.dto.AuthResponseDto
import com.upchiapas.plantas.auth.data.remote.dto.LoginRequestDto
import com.upchiapas.plantas.auth.data.remote.dto.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("login")
    suspend fun login(@Body body: LoginRequestDto): AuthResponseDto

    @POST("register")
    suspend fun register(@Body body: RegisterRequestDto): AuthResponseDto

    /** Revoca el token actual (el AuthInterceptor agrega el Bearer). */
    @POST("logout")
    suspend fun logout()
}
