package com.upchiapas.plantas.core.network

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

/** Cuerpo de error de la API: { "message": "...", "errors": { "campo": "mensaje" } }. */
@Serializable
data class ErrorDto(
    val message: String? = null,
    val errors: Map<String, String>? = null
)

/** Error ya traducido para la app: código HTTP (null si no hubo respuesta), mensaje y errores por campo. */
class ApiException(
    val code: Int?,
    override val message: String,
    val fieldErrors: Map<String, String> = emptyMap()
) : Exception(message) {
    val isUnauthorized: Boolean get() = code == 401
    val isNotFound: Boolean get() = code == 404
}

fun Throwable.toApiException(json: Json): ApiException = when (this) {
    is ApiException -> this
    is HttpException -> {
        val body = runCatching { response()?.errorBody()?.string() }.getOrNull()
        val dto = body?.let { runCatching { json.decodeFromString<ErrorDto>(it) }.getOrNull() }
        ApiException(
            code = code(),
            message = dto?.message ?: "Error del servidor (${code()})",
            fieldErrors = dto?.errors.orEmpty()
        )
    }
    is IOException -> ApiException(null, "No hay conexión con el servidor. Revisa tu red e inténtalo de nuevo")
    else -> ApiException(null, message ?: "Ocurrió un error inesperado")
}

/** Ejecuta una llamada de red y devuelve Result; los errores salen como ApiException. */
suspend inline fun <T> safeApiCall(json: Json, block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e.toApiException(json))
    }
