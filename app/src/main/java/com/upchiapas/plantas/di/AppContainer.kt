package com.upchiapas.plantas.di

import android.content.Context
import com.upchiapas.plantas.auth.data.remote.AuthApiService
import com.upchiapas.plantas.auth.data.repository.AuthRepositoryImpl
import com.upchiapas.plantas.auth.domain.repository.AuthRepository
import com.upchiapas.plantas.core.network.AuthInterceptor
import com.upchiapas.plantas.core.session.SessionStorage
import com.upchiapas.plantas.core.session.SharedPrefsSessionStorage
import com.upchiapas.plantas.plants.data.remote.PlantsApiService
import com.upchiapas.plantas.plants.data.repository.PlantsRepositoryImpl
import com.upchiapas.plantas.plants.domain.repository.PlantsRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Contenedor de dependencias manual. Se crea una sola vez en PlantasApplication.
 * Es el único lugar donde se instancian Retrofit, OkHttp, la sesión y los repositorios.
 */
class AppContainer(context: Context) {

    val sessionStorage: SessionStorage = SharedPrefsSessionStorage(context.applicationContext)

    private val json = Json { ignoreUnknownKeys = true }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessionStorage))
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    private val authApiService: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    private val plantsApiService: PlantsApiService by lazy { retrofit.create(PlantsApiService::class.java) }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authApiService, sessionStorage, json)
    }

    val plantsRepository: PlantsRepository by lazy {
        PlantsRepositoryImpl(plantsApiService, json)
    }

    companion object {
        /** API desplegada en AWS (HTTPS). Con el servidor local usa "http://10.0.2.2:3000/". */
        const val BASE_URL = "https://plantas-citlali.duckdns.org/"
    }
}
