package com.upchiapas.plantas.core.session

/** Abstracción del almacenamiento de la sesión (token). */
interface SessionStorage {
    fun getToken(): String?
    fun saveToken(token: String)
    fun clear()
}
