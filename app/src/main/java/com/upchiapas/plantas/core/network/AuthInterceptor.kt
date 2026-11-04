package com.upchiapas.plantas.core.network

import com.upchiapas.plantas.core.session.SessionStorage
import okhttp3.Interceptor
import okhttp3.Response

/** Agrega Authorization: Bearer <token> a cada petición cuando hay sesión. */
class AuthInterceptor(
    private val sessionStorage: SessionStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionStorage.getToken()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
