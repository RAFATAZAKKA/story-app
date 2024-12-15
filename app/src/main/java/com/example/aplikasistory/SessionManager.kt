package com.example.aplikasistory

import android.content.Context

class SessionManager(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveLogin(token: String?) {
        sharedPreferences.edit()
            .putBoolean("is_logged_in", true)
            .putString("auth_token", token)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }

    fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun clearSession() {
        sharedPreferences.edit()
            .clear()
            .apply()
    }
}

