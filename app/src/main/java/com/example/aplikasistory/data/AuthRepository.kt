package com.example.aplikasistory.data

import com.example.aplikasistory.data.api.ApiService
import com.example.aplikasistory.data.response.LoginResponse
import com.example.aplikasistory.data.response.RegisterResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AuthRepository(private val apiService: ApiService) {

    suspend fun register(name: String, email: String, password: String): Flow<RegisterResponse> {
        return flow {
            val response = apiService.register(name, email, password)
            emit(response)
        }
    }

    suspend fun login(email: String, password: String): Flow<LoginResponse> {
        return flow {
            val response = apiService.login(email, password)
            emit(response)
        }
    }
}