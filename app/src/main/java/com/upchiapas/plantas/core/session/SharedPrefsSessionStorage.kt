package com.upchiapas.plantas.core.session

import android.content.Context
import androidx.core.content.edit

class SharedPrefsSessionStorage(context: Context) : SessionStorage {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    override fun saveToken(token: String) {
        prefs.edit { putString(KEY_TOKEN, token) }
    }

    override fun clear() {
        prefs.edit { remove(KEY_TOKEN) }
    }

    private companion object {
        const val PREFS_NAME = "session"
        const val KEY_TOKEN = "token"
    }
}
