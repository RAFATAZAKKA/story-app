package com.example.aplikasistory

import android.content.Context
import android.content.SharedPreferences


class SessionManager(context: Context) {

    private var prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val USER_TOKEN = "USER_TOKEN"
    }


    fun saveLogin(token: String) {
        prefs.edit().putString(USER_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}


