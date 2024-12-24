package com.example.aplikasistory.data

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.core.DataStore


data class User(val token: String)

class UserPreference private constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val tokenKey = stringPreferencesKey("token")

    // Mendapatkan token pengguna
    fun getUser(): Flow<User> {
        return dataStore.data.map { preferences ->
            val token = preferences[tokenKey] ?: ""
            User(token)
        }
    }

    // Menyimpan token pengguna
    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    // Menghapus session atau token pengguna
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var instance: UserPreference? = null

        // Mendapatkan instance UserPreference
        fun getInstance(dataStore: DataStore<Preferences>): UserPreference =
            instance ?: synchronized(this) {
                instance ?: UserPreference(dataStore).also { instance = it }
            }
    }
}


//data class User(val token: String)
//
//class UserPreference private constructor(
//    private val dataStore: androidx.datastore.core.DataStore<Preferences>
//) {
//    fun getUser(): Flow<User> {
//        val tokenKey = stringPreferencesKey("token")
//        return dataStore.data.map { preferences ->
//            val token = preferences[tokenKey] ?: ""
//            User(token)
//        }
//    }
//
//    companion object {
//        @Volatile
//        private var instance: UserPreference? = null
//
//        fun getInstance(dataStore: androidx.datastore.core.DataStore<Preferences>): UserPreference =
//            instance ?: synchronized(this) {
//                instance ?: UserPreference(dataStore)
//            }
//    }
//
//    suspend fun clearSession() {
//        val tokenKey = stringPreferencesKey("token")
//        dataStore.edit { preferences ->
//            preferences.clear()
//        }
//    }