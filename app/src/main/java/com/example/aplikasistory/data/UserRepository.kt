package com.example.aplikasistory.data

import androidx.datastore.core.DataStore
import com.example.aplikasistory.DataStoreHelper
import com.example.aplikasistory.data.api.ApiService
import com.example.aplikasistory.data.response.ErrorResponse
import com.example.aplikasistory.data.response.ListStoryItem
import com.example.aplikasistory.data.response.LoginResponse
import com.example.aplikasistory.data.response.RegisterResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import java.io.IOException
import java.util.prefs.Preferences

class UserRepository(
    private val apiService: ApiService,
    private val dataStoreHelper: DataStoreHelper // Gunakan DataStoreHelper
) {

    suspend fun saveToken(token: String) {
        dataStoreHelper.saveToken(token) // Panggil metode di DataStoreHelper
    }

    val token: Flow<String?> = dataStoreHelper.token // Gunakan token flow dari DataStoreHelper

    // Fungsi register
    fun register(name: String, email: String, password: String): Flow<Result<RegisterResponse>> {
        return flow {
            emit(Result.Loading) // Emit loading state
            try {
                val response = apiService.register(name, email, password)
                emit(Result.Success(response)) // Emit response sukses
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

    // Fungsi login
    fun login(email: String, password: String): Flow<Result<LoginResponse>> {
        return flow {
            emit(Result.Loading) // Emit loading state
            try {
                val response = apiService.login(email, password)
                response.loginResult?.token?.let { saveToken(it) } // Simpan token setelah login berhasil
                emit(Result.Success(response)) // Emit response sukses
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMessage = if (errorBody != null) {
                    Gson().fromJson(errorBody, ErrorResponse::class.java).message
                } else {
                    "Terjadi kesalahan tidak diketahui"
                }
                emit(Result.Error(Exception(errorMessage)))
            } catch (e: IOException) {
                emit(Result.Error(Exception("Koneksi gagal. Pastikan perangkat Anda terhubung ke internet.")))
            } catch (e: Exception) {
                emit(Result.Error(Exception("Error tidak diketahui: ${e.message}")))
            }
        }.flowOn(Dispatchers.IO)
    }

}