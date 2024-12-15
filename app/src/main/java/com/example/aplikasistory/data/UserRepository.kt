package com.example.aplikasistory.data

import com.example.aplikasistory.DataStoreHelper
import com.example.aplikasistory.data.api.ApiService
import com.example.aplikasistory.data.response.ErrorResponse
import com.example.aplikasistory.data.response.ListStoryItem
import com.example.aplikasistory.data.response.LoginResponse
import com.example.aplikasistory.data.response.RegisterResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException


class UserRepository(
    private val apiService: ApiService,
    private val dataStoreHelper: DataStoreHelper
) {

    suspend fun saveToken(token: String) {
        dataStoreHelper.saveToken(token)
    }

    val token: Flow<String?> = dataStoreHelper.token


    fun register(name: String, email: String, password: String): Flow<Result<RegisterResponse>> {
        return flow {
            emit(Result.Loading)
            try {
                val response = apiService.register(name, email, password)
                emit(Result.Success(response))
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = if (errorBody != null) {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } else {
                    "Unknown error occurred"
                }
                emit(Result.Error(Exception(errorMessage)))


            }
        }.flowOn(Dispatchers.IO)
    }


    fun login(email: String, password: String): Flow<Result<LoginResponse>> {
        return flow {
            emit(Result.Loading)
            try {
                val response = apiService.login(email, password)
                response.loginResult?.token?.let { saveToken(it) }
                emit(Result.Success(response))
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = errorBody?.let {
                    Gson().fromJson(it, ErrorResponse::class.java).message
                } ?: "Kesalahan tidak diketahui"
                emit(Result.Error(Exception(errorMessage)))
            } catch (e: IOException) {
                emit(Result.Error(Exception("Koneksi gagal. Pastikan perangkat Anda terhubung ke internet.")))
            }
        }.flowOn(Dispatchers.IO)
    }

    fun getStories(): Flow<Result<List<ListStoryItem>?>> {
        return flow {
            emit(Result.Loading)
            try {
                val token = dataStoreHelper.token.first() ?: ""
                val response = apiService.getStories("Bearer $token")
                val filteredStories = response.listStory?.filterNotNull()
                emit(Result.Success(filteredStories))
            } catch (e: HttpException) {
                emit(Result.Error(Exception(e.message())))
            } catch (e: IOException) {
                emit(Result.Error(Exception("Koneksi gagal")))
            }
        }.flowOn(Dispatchers.IO)
    }


    suspend fun clearSession() {
        dataStoreHelper.clearToken()
    }

}